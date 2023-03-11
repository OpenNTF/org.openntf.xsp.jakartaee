/**
 * Copyright (c) 2018-2023 Contributors to the XPages Jakarta EE Support Project
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

import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.transaction.xa.Xid;

import org.openntf.xsp.jakarta.transaction.DominoTransaction;
import org.openntf.xsp.jakarta.transaction.DominoXid;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.transaction.RollbackException;
import jakarta.transaction.Status;
import jakarta.transaction.Synchronization;
import jakarta.transaction.SystemException;
import jakarta.transaction.Transaction;
import jakarta.transaction.UserTransaction;

/**
 * CDI bean that produces a {@link UserTransaction} object for the current
 * thread, which is here considered largely synonymous with the request scope.
 * 
 * @author Jesse Gallagher
 * @since 2.7.0
 */
@RequestScoped
public class DominoTransactionProducer {
	private final Logger log = Logger.getLogger(DominoTransactionProducer.class.getName());

	private AtomicReference<DominoTransaction> transaction;
	
	@PostConstruct
	public void postConstruct() {
		this.transaction = new AtomicReference<>();
	}
	
	@Produces
	public Transaction produceTransaction() {
		return getTransaction();
	}
	
	public DominoTransaction peekTransaction() {
		return this.transaction.get();
	}
	
	public void setTransaction(DominoTransaction transaction) {
		this.transaction.set(transaction);
	}
	
	public void clearTransaction() {
		setTransaction(null);
	}
	
	private DominoTransaction getTransaction() {
		return this.transaction.updateAndGet(existing -> existing == null ? createTransaction() : existing);
	}
	
	private DominoTransaction createTransaction() {
		Xid id = new DominoXid();
		DominoTransaction result = new DominoTransaction(id);
		try {
			result.registerSynchronization(new Synchronization() {
				@Override
				public void beforeCompletion() {
					// NOP
				}
				
				@Override
				public void afterCompletion(int status) {
					DominoTransactionProducer.this.transaction.set(null);
				}
			});
		} catch (IllegalStateException | RollbackException | SystemException e) {
			// Ignore in this case
		}
		return result;
	}
	
	@PreDestroy
	public void preDestroy() {
		DominoTransaction transaction = this.transaction.get();
		if(transaction != null) {
			int status = transaction.getStatus();
			if(status == Status.STATUS_ACTIVE) {
				try {
					transaction.rollback();
				} catch (IllegalStateException | SecurityException | SystemException e) {
					if(log.isLoggable(Level.SEVERE)) {
						log.log(Level.SEVERE, "Encountered exception rolling back unclosed transaction", e);
					}
				}
			}
		}
	}
}
