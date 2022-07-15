package org.openntf.xsp.jakarta.transaction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import jakarta.transaction.HeuristicMixedException;
import jakarta.transaction.HeuristicRollbackException;
import jakarta.transaction.NotSupportedException;
import jakarta.transaction.RollbackException;
import jakarta.transaction.Status;
import jakarta.transaction.Synchronization;
import jakarta.transaction.SystemException;
import jakarta.transaction.Transaction;
import jakarta.transaction.UserTransaction;

/**
 * Domino implementation of {@link UserTransaction}, which uses an IBM Commons
 * extension point for {@link TransactionParticipant} to hook into the
 * transaction process.
 * 
 * @author Jesse Gallagher
 * @since 2.7.0
 */
public class DominoUserTransaction implements UserTransaction, Transaction {
	private int status = Status.STATUS_NO_TRANSACTION;
	private boolean rollbackOnly = false;
	
	private final Collection<XAResource> resources;
	private final Collection<Synchronization> synchronizations;
	private final Xid id;
	
	public DominoUserTransaction(Xid id) {
		this.resources = Collections.synchronizedList(new ArrayList<>());
		this.synchronizations = Collections.synchronizedList(new ArrayList<>());
		this.id = id;
	}

	@Override
	public void begin() throws NotSupportedException, SystemException {
		if(status != Status.STATUS_NO_TRANSACTION) {
			throw new NotSupportedException("Transaction is already active");
		}
		for(XAResource res : new ArrayList<>(this.resources)) {
			try {
				res.start(this.id, XAResource.TMNOFLAGS);
			} catch (XAException e) {
				throw new RuntimeException(e);
			}
		}
		status = Status.STATUS_ACTIVE;
	}

	@Override
	public void commit() throws RollbackException, HeuristicMixedException, HeuristicRollbackException,
			SecurityException, IllegalStateException, SystemException {
		if(status == Status.STATUS_NO_TRANSACTION) {
			throw new IllegalStateException("Transaction was not begun");
		}
		if(rollbackOnly) {
			rollback();
			throw new RollbackException("Transaction was marked as rollback-only and rolled back");
		}
		
		status = Status.STATUS_COMMITTING;
		for(Synchronization sync : new ArrayList<>(this.synchronizations)) {
			sync.beforeCompletion();
		}
		try {
			for(XAResource res : new ArrayList<>(this.resources)) {
				try {
					res.commit(this.id, true);
				} catch (XAException e) {
					throw new RuntimeException(e);
				}
			}
			status = Status.STATUS_COMMITTED;
		} finally {
			for(Synchronization sync : new ArrayList<>(this.synchronizations)) {
				sync.afterCompletion(status);
			}
		}
	}

	@Override
	public void rollback() throws IllegalStateException, SecurityException, SystemException {
		if(status == Status.STATUS_NO_TRANSACTION) {
			throw new IllegalStateException("Transaction was not begun");
		}
		status = Status.STATUS_ROLLING_BACK;
		for(XAResource res : new ArrayList<>(this.resources)) {
			try {
				res.rollback(this.id);
			} catch (XAException e) {
				throw new RuntimeException(e);
			}
		}
		status = Status.STATUS_ROLLEDBACK;
	}

	@Override
	public void setRollbackOnly() throws IllegalStateException, SystemException {
		if(status == Status.STATUS_NO_TRANSACTION) {
			throw new IllegalStateException("Transaction was not begun");
		}
		this.rollbackOnly = true;
	}

	@Override
	public int getStatus() {
		return status;
	}

	@Override
	public void setTransactionTimeout(int seconds) throws SystemException {
		// TODO decide if this can be applicable
		for(XAResource res : new ArrayList<>(this.resources)) {
			try {
				res.setTransactionTimeout(seconds);
			} catch (XAException e) {
				throw new RuntimeException(e);
			}
		}
	}

	@Override
	public boolean delistResource(XAResource xaRes, int flag) throws IllegalStateException, SystemException {
		return this.resources.remove(xaRes);
	}

	@Override
	public boolean enlistResource(XAResource xaRes) throws RollbackException, IllegalStateException, SystemException {
		this.resources.add(xaRes);
		return true;
	}

	@Override
	public void registerSynchronization(Synchronization sync)
			throws RollbackException, IllegalStateException, SystemException {
		this.synchronizations.add(sync);
	}

}
