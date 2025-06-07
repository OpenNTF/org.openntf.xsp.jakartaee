/**
 * Copyright (c) 2018-2025 Contributors to the XPages Jakarta EE Support Project
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
package org.openntf.xsp.jakarta.transaction.interceptor;

import java.util.Arrays;
import java.util.Objects;

import jakarta.enterprise.inject.spi.CDI;
import jakarta.interceptor.InvocationContext;
import jakarta.transaction.SystemException;
import jakarta.transaction.TransactionManager;
import jakarta.transaction.Transactional;

/**
 * Basic CDI implementation of the {@link Transactional @Transactional} annotation.
 *
 * @author Jesse Gallagher
 * @since 2.7.0
 */
public abstract class AbstractTransactionalInterceptor {

	public Object doWrapMethod(final InvocationContext ctx) throws Exception {
		Transactional transactional = ctx.getMethod().getAnnotation(Transactional.class);
		// If it's not on the method, find it on superclasses
		if(transactional == null) {
			transactional = findAnnotation(ctx.getTarget().getClass());
		}
		Objects.requireNonNull(transactional, "Unable to find @Transactional annotation");

		Class<?>[] rollbackOn = transactional.rollbackOn();
		Class<?>[] dontRollbackOn = transactional.dontRollbackOn();

		try {
			return ctx.proceed();
		} catch(RuntimeException | Error e) {
			// Note: spec makes no mention of thrown Errors. Though it may be too late at this
			//   point, I think it makes sense to treat them as RuntimeException. In Domino, it's
			//   uncharacteristically likely to hit NoClassDefFoundError specifically

			// See if it's explicitly ignored
			if(Arrays.stream(dontRollbackOn).anyMatch(c -> c.isAssignableFrom(e.getClass()))) {
				// Skip the rollback
				throw e;
			}

			// Default for unchecked exceptions is to roll back
			markRollback();
			throw e;
		} catch(Exception e) {
			// Check to see if we should ignore this
			if(Arrays.stream(rollbackOn).anyMatch(c -> c.isAssignableFrom(e.getClass()))) {
				// Roll back
				markRollback();
				throw e;
			}

			// Default for checked exceptions is to not roll back
			throw e;
		}
	}

	private Transactional findAnnotation(final Class<?> clazz) {
		Transactional transactional = clazz.getAnnotation(Transactional.class);
		if(transactional != null) {
			return transactional;
		} else {
			Class<?> sup = clazz.getSuperclass();
			if(sup != null) {
				return findAnnotation(sup);
			} else {
				return null;
			}
		}
	}

	private void markRollback() throws IllegalStateException, SystemException {
		TransactionManager man = CDI.current().select(TransactionManager.class).get();
		if(man.getTransaction() != null) {
			man.setRollbackOnly();
		}
	}
}
