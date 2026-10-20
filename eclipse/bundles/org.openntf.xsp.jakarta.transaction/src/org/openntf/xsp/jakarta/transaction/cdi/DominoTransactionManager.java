/*
 * Copyright (c) 2018-2026 Contributors to the XPages Jakarta EE Support Project
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

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.ArrayDeque;
import java.util.Deque;

import org.openntf.xsp.jakarta.transaction.DominoTransaction;

import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.transaction.HeuristicMixedException;
import jakarta.transaction.HeuristicRollbackException;
import jakarta.transaction.InvalidTransactionException;
import jakarta.transaction.NotSupportedException;
import jakarta.transaction.RollbackException;
import jakarta.transaction.Status;
import jakarta.transaction.SystemException;
import jakarta.transaction.Transaction;
import jakarta.transaction.UserTransaction;

/**
 * @author Jesse Gallagher
 * @since 2.7.0
 */
@RequestScoped
public class DominoTransactionManager implements jakarta.transaction.TransactionManager {
	private static final Logger log = System.getLogger(DominoTransactionManager.class.getPackageName());

	/** Thread-local stack of suspended transactions */
	private static final ThreadLocal<Deque<DominoTransaction>> SUSPENSION_STACK =
		ThreadLocal.withInitial(ArrayDeque::new);

	@Override
	public void begin() throws NotSupportedException, SystemException {
		CDI.current().select(UserTransaction.class).get().begin();
	}

	@Override
	public void commit() throws RollbackException, HeuristicMixedException, HeuristicRollbackException,
			SecurityException, IllegalStateException, SystemException {
		CDI.current().select(Transaction.class).get().commit();
	}

	@Override
	public int getStatus() throws SystemException {
		return CDI.current().select(Transaction.class).get().getStatus();
	}

	@Override
	public Transaction getTransaction() throws SystemException {
		return CDI.current().select(DominoTransactionProducer.class).get().peekTransaction();
	}

	@Override
	public void setRollbackOnly() throws IllegalStateException, SystemException {
		CDI.current().select(UserTransaction.class).get().setRollbackOnly();
	}

	@Override
	public void setTransactionTimeout(final int seconds) throws SystemException {
		CDI.current().select(UserTransaction.class).get().setTransactionTimeout(seconds);
	}

	@Override
	public void rollback() throws IllegalStateException, SecurityException, SystemException {
		CDI.current().select(UserTransaction.class).get().rollback();
	}

	@Override
	public Transaction suspend() throws SystemException {
		DominoTransactionProducer producer = CDI.current().select(DominoTransactionProducer.class).get();
		DominoTransaction current = producer.peekTransaction();

		if (current == null || current.getStatus() == Status.STATUS_NO_TRANSACTION) {
			// No active transaction — per JTA spec, return null
			return null;
		}

		producer.clearTransaction();

		SUSPENSION_STACK.get().push(current);

		return current;
	}

	@Override
	public void resume(final Transaction tobj) throws InvalidTransactionException, IllegalStateException, SystemException {
		Deque<DominoTransaction> stack = SUSPENSION_STACK.get();

		// Must not have an active transaction when resuming
		if (stack.isEmpty()) {
			throw new InvalidTransactionException("Resume called with no matching suspended transaction");
		}

		DominoTransaction suspended = stack.pop();
		if (suspended == null || suspended != tobj) {
			// Put it back and throw — per JTA spec, the transaction must match
			// the most recently suspended one (LIFO discipline)
			stack.push(suspended);
			throw new InvalidTransactionException("Transaction to resume does not match the most recently suspended transaction");
		}

		// Check the transaction is in a resumable state
		int status = suspended.getStatus();
		if (status != Status.STATUS_ACTIVE && status != Status.STATUS_MARKED_ROLLBACK) {
			stack.push(suspended);
			throw new InvalidTransactionException("Transaction is not in an active or marked-rollback state");
		}

		// Re-attach as the active transaction
		DominoTransactionProducer producer = CDI.current().select(DominoTransactionProducer.class).get();
		producer.setTransaction(suspended);
	}

	/**
	 * Cleans up any suspended transactions when the application scope is destroyed.
	 * Rolls back any transactions that were suspended but never resumed.
	 */
	@PreDestroy
	public void preDestroy() {
		Deque<DominoTransaction> stack = SUSPENSION_STACK.get();
		DominoTransaction txn;
		while ((txn = stack.pollFirst()) != null) {
			try {
				if (txn.getStatus() == Status.STATUS_ACTIVE || txn.getStatus() == Status.STATUS_MARKED_ROLLBACK) {
					txn.rollback();
				}
			} catch (IllegalStateException | SecurityException | SystemException e) {
				log.log(Level.WARNING, "Encountered exception clearing thread transaction stack", e);
			}
		}
	}

}
