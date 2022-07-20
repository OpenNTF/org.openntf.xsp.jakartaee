/**
 * Copyright © 2018-2022 Contributors to the XPages Jakarta EE Support Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openntf.xsp.jakarta.transaction.cdi;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.openntf.xsp.cdi.context.BasicScopeContextHolder;
import org.openntf.xsp.cdi.context.BasicScopeContextHolder.BasicScopeInstance;

import jakarta.enterprise.context.spi.Context;
import jakarta.enterprise.context.spi.Contextual;
import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.transaction.RollbackException;
import jakarta.transaction.Status;
import jakarta.transaction.Synchronization;
import jakarta.transaction.SystemException;
import jakarta.transaction.Transaction;
import jakarta.transaction.TransactionScoped;
import jakarta.transaction.UserTransaction;

/**
 * Basic implementation of {@link TransactionScoped} backed by a
 * thread-local {@link Map}.
 * 
 * @author Jesse Gallagher
 * @since 2.7.0
 */
public class TransactionScopeContext implements Context, Serializable {
	private static final long serialVersionUID = 1L;
	
	private static final ThreadLocal<BasicScopeContextHolder> storage = ThreadLocal.withInitial(BasicScopeContextHolder::new);
	private static final ThreadLocal<Boolean> registeredSynchronization = ThreadLocal.withInitial(() -> false);
	
	public static BasicScopeContextHolder peekStorage() {
		return storage.get();
	}
	public static void pushStorage(BasicScopeContextHolder s) {
		storage.set(s);
	}

	@Override
	public Class<? extends Annotation> getScope() {
		return TransactionScoped.class;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T get(Contextual<T> contextual, CreationalContext<T> creationalContext) {
		registerSync();
		
		Bean<T> bean = (Bean<T>) contextual;
		return (T) storage.get().getBeans().computeIfAbsent(bean.getBeanClass().getName(), className -> {
			BasicScopeInstance<T> instance = new BasicScopeInstance<>();
			instance.setBeanClass(className);
			instance.setCtx(creationalContext);
			instance.setInstance(bean.create(creationalContext));
			return instance;
		}).getInstance();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T get(Contextual<T> contextual) {
		registerSync();
		
		Bean<T> bean = (Bean<T>) contextual;
		BasicScopeContextHolder holder = storage.get();
		if(holder.getBeans().containsKey(bean.getBeanClass().getName())) {
			return (T)holder.getBean(bean.getBeanClass().getName()).getInstance();
		} else {
			return null;
		}
	}

	@Override
	public boolean isActive() {
		UserTransaction transaction = CDI.current().select(UserTransaction.class).get();
		try {
			switch(transaction.getStatus()) {
			case Status.STATUS_ACTIVE:
			case Status.STATUS_MARKED_ROLLBACK:
			case Status.STATUS_PREPARED:
			case Status.STATUS_UNKNOWN:
			case Status.STATUS_PREPARING:
			case Status.STATUS_COMMITTING:
			case Status.STATUS_ROLLING_BACK:
				return true;
			default:
				return false;
			}
		} catch (SystemException e) {
			return false;
		}
	}
	
	private void registerSync() {
		// On access, add a Synchronization to clear the context on commit or rollback
		Transaction transaction = CDI.current().select(Transaction.class).get();
		if(!registeredSynchronization.get()) {
			try {
				transaction.registerSynchronization(new Synchronization() {

					@Override
					public void beforeCompletion() {
						// NOP
					}

					@Override
					public void afterCompletion(int status) {
						Collection<BasicScopeInstance<?>> beans = new ArrayList<>(storage.get().getBeans().values());
						beans.forEach(storage.get()::destroyBean);
						registeredSynchronization.set(false);
					}
					
				});
			} catch (IllegalStateException | RollbackException | SystemException e) {
				// Ignore
			}
		}
	}

}
