package org.openntf.xsp.jakarta.concurrency.servlet;

import jakarta.servlet.ServletRequestEvent;
import jakarta.servlet.ServletRequestListener;

/**
 * 
 * @author Jesse Gallagher
 * @since 2.7.0
 */
public class ConcurrencyRequestListener implements AbstractServletJndiConfigurator, ServletRequestListener {
	
	@Override
	public void requestInitialized(ServletRequestEvent sre) {
		pushExecutors(sre.getServletContext());
	}
	
	@Override
	public void requestDestroyed(ServletRequestEvent sre) {
		popExecutors(sre.getServletContext());
	}
}
