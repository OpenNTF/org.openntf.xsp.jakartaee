package org.openntf.xsp.jakarta.transaction.cdi;

import java.util.concurrent.atomic.AtomicReference;

import org.openntf.xsp.jakarta.transaction.DominoUserTransaction;

import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.transaction.UserTransaction;

/**
 * CDI bean that produces a {@link UserTransaction} object for the current
 * thread, which is here considered largely synonymous with the request scope.
 * 
 * @author Jesse Gallagher
 *
 */
@RequestScoped
public class UserTransactionProducer {

	private AtomicReference<DominoUserTransaction> transaction = new AtomicReference<>();
	
	@Produces
	public UserTransaction produceTransaction() {
		return this.transaction.updateAndGet(existing -> existing == null ? new DominoUserTransaction() : existing);
	}
}
