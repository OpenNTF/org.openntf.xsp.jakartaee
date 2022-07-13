package org.openntf.xsp.jakarta.transaction;

import jakarta.transaction.HeuristicMixedException;
import jakarta.transaction.HeuristicRollbackException;
import jakarta.transaction.NotSupportedException;
import jakarta.transaction.RollbackException;
import jakarta.transaction.Status;
import jakarta.transaction.SystemException;
import jakarta.transaction.UserTransaction;

public class DominoUserTransaction implements UserTransaction {
	private int status = Status.STATUS_NO_TRANSACTION;
	private boolean rollbackOnly = false;

	@Override
	public void begin() throws NotSupportedException, SystemException {
		if(status != Status.STATUS_NO_TRANSACTION) {
			throw new NotSupportedException("Transaction is already active");
		}
		System.out.println("transaction begun!");
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
		// TODO commit
		status = Status.STATUS_COMMITTED;
		System.out.println("Transaction committed!");
	}

	@Override
	public void rollback() throws IllegalStateException, SecurityException, SystemException {
		if(status == Status.STATUS_NO_TRANSACTION) {
			throw new IllegalStateException("Transaction was not begun");
		}
		status = Status.STATUS_ROLLING_BACK;
		// TODO roll back
		status = Status.STATUS_ROLLEDBACK;
		System.out.println("Transaction rolled back!");
	}

	@Override
	public void setRollbackOnly() throws IllegalStateException, SystemException {
		if(status == Status.STATUS_NO_TRANSACTION) {
			throw new IllegalStateException("Transaction was not begun");
		}
		this.rollbackOnly = true;
	}

	@Override
	public int getStatus() throws SystemException {
		return status;
	}

	@Override
	public void setTransactionTimeout(int seconds) throws SystemException {
		// TODO decide if this can be applicable
	}

}
