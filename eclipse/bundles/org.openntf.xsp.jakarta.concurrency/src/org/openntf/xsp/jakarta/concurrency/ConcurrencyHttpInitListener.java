package org.openntf.xsp.jakarta.concurrency;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.openntf.xsp.jakarta.concurrency.jndi.DelegatingManagedExecutorService;
import org.openntf.xsp.jakarta.concurrency.jndi.DelegatingManagedScheduledExecutorService;
import org.openntf.xsp.jakartaee.events.JakartaHttpInitListener;
import org.openntf.xsp.jakartaee.util.LibraryUtil;
import org.osgi.framework.FrameworkUtil;

import jakarta.annotation.Priority;

@Priority(1)
public class ConcurrencyHttpInitListener implements JakartaHttpInitListener {
	private static final Logger log = Logger.getLogger(ConcurrencyActivator.class.getPackage().getName());

	private Class<?> mqClass;
	private Method mqOpen;
	private Method isQuitPending;
	private Method mqClose;
	
	@Override
	public void httpInit() throws Exception {
		// Make sure the core JNDI provider is set up
		try {
			FrameworkUtil.getBundle(com.ibm.pvc.jndi.provider.java.InitialContextFactory.class).start();
		} catch(Exception e) {
			// Ignore if it's already started or otherwise trouble
		}

		String jvmVersion = LibraryUtil.getSystemProperty("java.specification.version"); //$NON-NLS-1$

		if("1.8".equals(jvmVersion)) { //$NON-NLS-1$
			ClassLoader cl = ClassLoader.getSystemClassLoader();
			while(cl.getParent() != null) {
				cl = cl.getParent();
			}
			this.mqClass = Class.forName("lotus.notes.internal.MessageQueue", true, cl); //$NON-NLS-1$
			this.mqOpen = this.mqClass.getMethod("open", String.class, int.class); //$NON-NLS-1$
			this.isQuitPending = this.mqClass.getMethod("isQuitPending"); //$NON-NLS-1$
			this.mqClose = this.mqClass.getMethod("close", int.class); //$NON-NLS-1$

			ExecutorHolder.INSTANCE.getGlobalExecutor().scheduleAtFixedRate(() -> {
				if(isHttpQuitting()) {
					ExecutorHolder.INSTANCE.termAll();
				}
			}, 0, 10, TimeUnit.SECONDS);
		}

		InitialContext jndi = new InitialContext();
		try {
			jndi.rebind(ConcurrencyActivator.JNDI_EXECUTORSERVICE, new DelegatingManagedExecutorService());
		} catch(NamingException e) {
			if(log.isLoggable(Level.SEVERE)) {
				log.log(Level.SEVERE, "Encountered exception binding ManagedExecutorService in JNDI", e);
			}
		}
		try {
			jndi.rebind(ConcurrencyActivator.JNDI_SCHEDULEDEXECUTORSERVICE, new DelegatingManagedScheduledExecutorService());
		} catch(NamingException e) {
			if(log.isLoggable(Level.SEVERE)) {
				log.log(Level.SEVERE, "Encountered exception binding ManagedScheduledExecutorService in JNDI", e);
			}
		}
	}
	
	private boolean isHttpQuitting() {
		try {
			ClassLoader cl = ClassLoader.getSystemClassLoader();
			while(cl.getParent() != null) {
				cl = cl.getParent();
			}
			Class<?> mqClass = Class.forName("lotus.notes.internal.MessageQueue", true, cl); //$NON-NLS-1$
			Object mq = mqClass.getConstructor().newInstance();
			mqOpen.invoke(mq, "MQ$HTTP", 0); //$NON-NLS-1$
			try {
				if((Boolean)isQuitPending.invoke(mq)) {
					return true;
				}
			} finally {
				mqClose.invoke(mq, 0);
			}
		} catch(Throwable t) {
			// Ignore
		}
		return false;
	}
}
