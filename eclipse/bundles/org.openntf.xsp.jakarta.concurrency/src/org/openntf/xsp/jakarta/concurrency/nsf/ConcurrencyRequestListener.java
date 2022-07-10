package org.openntf.xsp.jakarta.concurrency.nsf;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import jakarta.enterprise.concurrent.ManagedExecutorService;
import jakarta.enterprise.concurrent.ManagedScheduledExecutorService;
import jakarta.servlet.ServletRequestEvent;
import jakarta.servlet.ServletRequestListener;

public class ConcurrencyRequestListener implements ServletRequestListener {
	public static final String JNDI_EXECUTORSERVICE = "java:comp/DefaultManagedExecutorService"; //$NON-NLS-1$
	public static final String JNDI_SCHEDULEDEXECUTORSERVICE = "java:comp/DefaultManagedScheduledExecutorService"; //$NON-NLS-1$
	
	@Override
	public void requestInitialized(ServletRequestEvent sre) {
		try {
			InitialContext jndi = new InitialContext();
			
			ManagedExecutorService exec = (ManagedExecutorService)sre.getServletContext().getAttribute(ConcurrencyApplicationListener.ATTR_EXECUTORSERVICE);
			jndi.bind(JNDI_EXECUTORSERVICE, exec);
			
			ManagedScheduledExecutorService scheduler = (ManagedScheduledExecutorService)sre.getServletContext().getAttribute(ConcurrencyApplicationListener.ATTR_SCHEDULEDEXECUTORSERVICE);
			jndi.bind(JNDI_SCHEDULEDEXECUTORSERVICE, scheduler);
		} catch (NamingException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public void requestDestroyed(ServletRequestEvent sre) {
		try {
			InitialContext jndi = new InitialContext();
			
			jndi.unbind(JNDI_EXECUTORSERVICE);
			jndi.unbind(JNDI_SCHEDULEDEXECUTORSERVICE);
		} catch (NamingException e) {
			throw new RuntimeException(e);
		}
	}
}
