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
package org.openntf.xsp.jakarta.transaction;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.Referenceable;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;

import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.transaction.HeuristicMixedException;
import jakarta.transaction.HeuristicRollbackException;
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
@RequestScoped
public class DominoUserTransaction implements UserTransaction, Serializable, Referenceable {
	private static final long serialVersionUID = 1L;
	private final Logger log = Logger.getLogger(DominoUserTransaction.class.getName());

	@Override
	public void begin() throws NotSupportedException, SystemException {
		Transaction transaction = CDI.current().select(Transaction.class).get();
		if(transaction instanceof DominoTransaction) {
			((DominoTransaction)transaction).begin();
		}
	}

	@Override
	public void commit() throws RollbackException, HeuristicMixedException, HeuristicRollbackException,
			SecurityException, IllegalStateException, SystemException {
		Transaction transaction = CDI.current().select(Transaction.class).get();
		transaction.commit();
	}

	@Override
	public void rollback() throws IllegalStateException, SecurityException, SystemException {
		Transaction transaction = CDI.current().select(Transaction.class).get();
		transaction.rollback();
	}

	@Override
	public void setRollbackOnly() throws IllegalStateException, SystemException {
		Transaction transaction = CDI.current().select(Transaction.class).get();
		transaction.setRollbackOnly();
	}

	@Override
	public int getStatus() throws SystemException {
		Transaction transaction = CDI.current().select(Transaction.class).get();
		return transaction.getStatus();
	}

	@Override
	public void setTransactionTimeout(int seconds) throws SystemException {
		Transaction transaction = CDI.current().select(Transaction.class).get();
		if(transaction instanceof DominoTransaction) {
			Collection<XAResource> resources = ((DominoTransaction)transaction).getResources();
			for(XAResource res : new ArrayList<>(resources)) {
				try {
					res.setTransactionTimeout(seconds);
				} catch (XAException e) {
					if(log.isLoggable(Level.SEVERE)) {
						log.log(Level.SEVERE, MessageFormat.format("Encountered exception setting transaction timeout on resource: {0}", res), e);
					}
				}
			}
		}
	}

	@Override
	public Reference getReference() throws NamingException {
		// TODO determine if this should return a better value
		return new Reference(getClass().getName());
	}

}
