/**
 * Copyright (c) 2018-2024 Contributors to the XPages Jakarta EE Support Project
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
	public void saveContext(final ContextHandle contextHandle) {
		if(contextHandle instanceof AttributedContextHandle) {
			Instance<DominoTransactionProducer> producer = CDI.current().select(DominoTransactionProducer.class);
			if(producer.isResolvable()) {
				DominoTransaction transaction = producer.get().peekTransaction();
				((AttributedContextHandle)contextHandle).setAttribute(PROP_TRANSACTION, transaction);
			}
		}
	}

	@Override
	public void setup(final ContextHandle contextHandle) throws IllegalStateException {
		if(contextHandle instanceof AttributedContextHandle) {
			Instance<DominoTransactionProducer> producer = CDI.current().select(DominoTransactionProducer.class);
			if(producer.isResolvable()) {
				DominoTransaction transaction = ((AttributedContextHandle)contextHandle).getAttribute(PROP_TRANSACTION);
				producer.get().setTransaction(transaction);
			}
		}
	}

	@Override
	public void reset(final ContextHandle contextHandle) {
		try {
			CDI<Object> cdi = CDI.current();
			if(cdi != null) {
				Instance<DominoTransactionProducer> producer = cdi.select(DominoTransactionProducer.class);
				if(producer.isResolvable()) {
					producer.get().clearTransaction();
				}
			}
		} catch(IllegalStateException e) {
			// Will almost definitely be "Invalid disposed application ClassLoader", which occurs
			//   during active development of an NSF - ignore
			// https://github.com/OpenNTF/org.openntf.xsp.jakartaee/issues/362
		}
	}

}
