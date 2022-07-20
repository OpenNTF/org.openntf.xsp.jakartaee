package org.openntf.xsp.jakarta.transaction.servlet;

import jakarta.servlet.ServletRequestEvent;
import jakarta.servlet.ServletRequestListener;

/**
 * @author Jesse Gallagher
 * @since 2.7.0
 */
public class TransactionRequestListener implements ServletRequestListener, AbstractTransactionJndiConfigurator {

	@Override
	public void requestInitialized(ServletRequestEvent sre) {
		pushTransaction();
	}
	
	@Override
	public void requestDestroyed(ServletRequestEvent sre) {
		popTransaction();
	}

}
