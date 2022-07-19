package org.openntf.xsp.jakarta.transaction.concurrency;

import org.glassfish.enterprise.concurrent.spi.ContextHandle;
import org.openntf.xsp.jakarta.concurrency.AttributedContextHandle;
import org.openntf.xsp.jakarta.concurrency.ContextSetupParticipant;
import org.openntf.xsp.jakarta.transaction.DominoTransaction;
import org.openntf.xsp.jakarta.transaction.cdi.DominoTransactionProducer;

import jakarta.annotation.Priority;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.spi.CDI;

/**
 * This {@link ContextSetupParticipant} handles moving an active {@link DominoTransaction}
 * instance into the new thread
 * 
 * @author Jesse Gallagher
 * @since 2.7.0
 */
@Priority(0)
public class TransactionContextSetupParticipant implements ContextSetupParticipant {
	private static final String PROP_TRANSACTION = TransactionContextSetupParticipant.class.getName() + "_transaction"; //$NON-NLS-1$
	
	@Override
	public void saveContext(ContextHandle contextHandle) {
		if(contextHandle instanceof AttributedContextHandle) {
			Instance<DominoTransactionProducer> producer = CDI.current().select(DominoTransactionProducer.class);
			if(producer.isResolvable()) {
				DominoTransaction transaction = producer.get().peekTransaction();
				((AttributedContextHandle)contextHandle).setAttribute(PROP_TRANSACTION, transaction);
			}
		}
	}

	@Override
	public void setup(ContextHandle contextHandle) throws IllegalStateException {
		if(contextHandle instanceof AttributedContextHandle) {
			Instance<DominoTransactionProducer> producer = CDI.current().select(DominoTransactionProducer.class);
			if(producer.isResolvable()) {
				DominoTransaction transaction = ((AttributedContextHandle)contextHandle).getAttribute(PROP_TRANSACTION);
				producer.get().setTransaction(transaction);
			}
		}
	}

	@Override
	public void reset(ContextHandle contextHandle) {
		Instance<DominoTransactionProducer> producer = CDI.current().select(DominoTransactionProducer.class);
		if(producer.isResolvable()) {
			producer.get().clearTransaction();
		}
	}

}
