package org.openntf.xsp.jakarta.concurrency.servlet;

import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;

import org.openntf.xsp.jakarta.concurrency.nsf.ConcurrencyApplicationListener;

import jakarta.enterprise.concurrent.ManagedExecutorService;
import jakarta.enterprise.concurrent.ManagedScheduledExecutorService;
import jakarta.servlet.ServletContext;

/**
 * 
 * @author Jesse Gallagher
 * @since 2.7.0
 */
public interface AbstractServletJndiConfigurator {
	
	public static final String JNDI_EXECUTORSERVICE = "java:comp/DefaultManagedExecutorService"; //$NON-NLS-1$
	public static final String JNDI_SCHEDULEDEXECUTORSERVICE = "java:comp/DefaultManagedScheduledExecutorService"; //$NON-NLS-1$

	public static final String ATTR_EXECUTORSERVICE = ConcurrencyApplicationListener.class.getName() + "_exec"; //$NON-NLS-1$
	public static final String ATTR_SCHEDULEDEXECUTORSERVICE = ConcurrencyApplicationListener.class.getName() + "_scheduledExec"; //$NON-NLS-1$
	

	default void pushExecutors(ServletContext servletContext) {
		try {
			InitialContext jndi = new InitialContext();
			
			ManagedExecutorService exec = (ManagedExecutorService)servletContext.getAttribute(ATTR_EXECUTORSERVICE);
			jndi.rebind(JNDI_EXECUTORSERVICE, exec);
			
			ManagedScheduledExecutorService scheduler = (ManagedScheduledExecutorService)servletContext.getAttribute(ATTR_SCHEDULEDEXECUTORSERVICE);
			jndi.rebind(JNDI_SCHEDULEDEXECUTORSERVICE, scheduler);
		} catch (NamingException e) {
			throw new RuntimeException(e);
		}
	}
	
	default void popExecutors(ServletContext servletContext) {
		try {
			InitialContext jndi = new InitialContext();
			
			jndi.unbind(JNDI_EXECUTORSERVICE);
			jndi.unbind(JNDI_SCHEDULEDEXECUTORSERVICE);
		} catch(NameNotFoundException e) {
			// Ignore - that's fine
		} catch (NamingException e) {
			throw new RuntimeException(e);
		}
	}

}
