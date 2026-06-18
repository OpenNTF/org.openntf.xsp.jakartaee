package org.openntf.xsp.jakarta.transaction.servlet;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;

/**
 * @since 3.7.0
 */
public class TransactionServletContextListener implements ServletContextListener {

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		sce.getServletContext().addListener(TransactionRequestListener.class);
	}
	
}
