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

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.transaction.HeuristicMixedException;
import jakarta.transaction.HeuristicRollbackException;
import jakarta.transaction.InvalidTransactionException;
import jakarta.transaction.NotSupportedException;
import jakarta.transaction.RollbackException;
import jakarta.transaction.SystemException;
import jakarta.transaction.Transaction;
import jakarta.transaction.UserTransaction;

/**
 * 
 * @author Jesse Gallagher
 * @since 2.7.0
 */
@ApplicationScoped
public class DominoTransactionManager implements jakarta.transaction.TransactionManager {

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
	public void setTransactionTimeout(int seconds) throws SystemException {
		CDI.current().select(UserTransaction.class).get().setTransactionTimeout(seconds);
	}

	@Override
	public void rollback() throws IllegalStateException, SecurityException, SystemException {
		CDI.current().select(UserTransaction.class).get().rollback();
	}

	@Override
	public Transaction suspend() throws SystemException {
		// TODO implement stack in the manager for this
		throw new UnsupportedOperationException();
	}

	@Override
	public void resume(Transaction tobj) throws InvalidTransactionException, IllegalStateException, SystemException {
		// TODO implement stack in the manager for this
		throw new UnsupportedOperationException();
	}

}
