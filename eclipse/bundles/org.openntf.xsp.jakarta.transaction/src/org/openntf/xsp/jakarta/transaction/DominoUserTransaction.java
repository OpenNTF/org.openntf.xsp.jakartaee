package org.openntf.xsp.jakarta.transaction;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

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
public class DominoUserTransaction implements UserTransaction {
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

}
