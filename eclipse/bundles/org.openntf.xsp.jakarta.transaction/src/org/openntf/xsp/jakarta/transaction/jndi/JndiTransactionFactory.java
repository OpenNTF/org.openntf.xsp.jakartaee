package org.openntf.xsp.jakarta.transaction.jndi;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.spi.ObjectFactory;

import org.openntf.xsp.jakarta.transaction.DominoTransactionSynchronizationRegistry;
import org.openntf.xsp.jakarta.transaction.cdi.DominoTransactionManager;

import jakarta.enterprise.inject.spi.CDI;

/**
 * @since 3.7.0
 */
public class JndiTransactionFactory implements ObjectFactory {
	
	@Override
	public Object getObjectInstance(Object obj, Name name, Context nameCtx, Hashtable<?, ?> environment)
			throws Exception {
		switch(String.valueOf(name)) {
		case "java:comp/TransactionManager": //$NON-NLS-1$
			return CDI.current().select(DominoTransactionManager.class).get();
		case "java:comp/TransactionSynchronizationRegistry": //$NON-NLS-1$
			return DominoTransactionSynchronizationRegistry.INSTANCE;
		default:
			return null;
		}
	}

}
