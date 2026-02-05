package org.openntf.xsp.jakarta.transaction;

import javax.naming.InitialContext;

import org.openntf.xsp.jakarta.transaction.cdi.DominoTransactionManager;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * @since 3.7.0
 */
public class TransactionActivator implements BundleActivator {
	@Override
	public void start(BundleContext context) throws Exception {
		var jndi = new InitialContext();
		jndi.bind("java:comp/TransactionManager", new DominoTransactionManager()); //$NON-NLS-1$
		jndi.bind("java:comp/TransactionSynchronizationRegistry", new DominoTransactionSynchronizationRegistry()); //$NON-NLS-1$
	}

	@Override
	public void stop(BundleContext context) throws Exception {

	}

}
