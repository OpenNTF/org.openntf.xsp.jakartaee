package org.openntf.xsp.jakarta.transaction;

import jakarta.enterprise.inject.spi.CDI;
import jakarta.transaction.Synchronization;
import jakarta.transaction.SystemException;
import jakarta.transaction.Transaction;
import jakarta.transaction.TransactionSynchronizationRegistry;

/**
 * @since 3.7.0
 */
public class DominoTransactionSynchronizationRegistry implements TransactionSynchronizationRegistry {

	@Override
	public Object getTransactionKey() {
		return CDI.current().select(Transaction.class).get();
	}

	@Override
	public void putResource(Object key, Object value) {
		System.out.println("putting resource " + key + "=" + value);
		if(CDI.current().select(Transaction.class).get() instanceof DominoTransaction dt) {
			dt.putResource(key, value);
		}
	}

	@Override
	public Object getResource(Object key) {
		System.out.println("getting resource " + key);
		if(CDI.current().select(Transaction.class).get() instanceof DominoTransaction dt) {
			return dt.getResource(key);
		} else {
			return null;
		}
	}

	@Override
	public void registerInterposedSynchronization(Synchronization sync) {
		// TODO See if this makes sense
	}

	@Override
	public int getTransactionStatus() {
		try {
			return CDI.current().select(Transaction.class).get().getStatus();
		} catch (SystemException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setRollbackOnly() {
		try {
			CDI.current().select(Transaction.class).get().setRollbackOnly();
		} catch (IllegalStateException | SystemException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean getRollbackOnly() {
		if(CDI.current().select(Transaction.class).get() instanceof DominoTransaction dt) {
			return dt.isRollbackOnly();
		} else {
			return false;
		}
	}

}
