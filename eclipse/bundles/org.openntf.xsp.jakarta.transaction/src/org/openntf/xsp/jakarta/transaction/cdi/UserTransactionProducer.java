package org.openntf.xsp.jakarta.transaction.cdi;

import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.transaction.xa.Xid;

import org.openntf.xsp.jakarta.transaction.DominoUserTransaction;
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
public class UserTransactionProducer {
	private final Logger log = Logger.getLogger(UserTransactionProducer.class.getName());

	private AtomicReference<DominoUserTransaction> transaction;
	
	@PostConstruct
	public void postConstruct() {
		this.transaction = new AtomicReference<>();
	}
	
	@Produces
	public UserTransaction produceUserTransaction() {
		return getTransaction();
	}
	
	@Produces
	public Transaction produceTransaction() {
		return getTransaction();
	}
	
	public void terminateTransaction() {
		this.transaction.set(null);
	}
	
	private DominoUserTransaction getTransaction() {
		return this.transaction.updateAndGet(existing -> existing == null ? createTransaction() : existing);
	}
	
	private DominoUserTransaction createTransaction() {
		Xid id = new DominoXid();
		DominoUserTransaction result = new DominoUserTransaction(id);
		try {
			result.registerSynchronization(new Synchronization() {
				@Override
				public void beforeCompletion() {
					// NOP
				}
				
				@Override
				public void afterCompletion(int status) {
					UserTransactionProducer.this.transaction.set(null);
				}
			});
		} catch (IllegalStateException | RollbackException | SystemException e) {
			// Ignore in this case
		}
		return result;
	}
	
	@PreDestroy
	public void preDestroy() {
		DominoUserTransaction transaction = this.transaction.get();
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