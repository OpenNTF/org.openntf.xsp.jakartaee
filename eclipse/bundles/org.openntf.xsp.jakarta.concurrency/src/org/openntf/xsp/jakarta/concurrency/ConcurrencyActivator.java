package org.openntf.xsp.jakarta.concurrency;

import java.lang.reflect.Method;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import lotus.domino.NotesThread;

/**
 * This activator is used to try to ensure that all spawned executors
 * are terminated at HTTP stop, even if they were violently flushed out
 * of context by design changes or if incoming HTTP requests are still
 * held up by blocked threads.
 * 
 * <p>This class works reflectively to access
 * {@code lotus.notes.internal.MessageQueue} because Domino's OSGi stack
 * may prevent normal access to it.</p>
 * 
 * @author Jesse Gallagher
 * @since 2.10.0
 */
public class ConcurrencyActivator implements BundleActivator {
	
	private ScheduledExecutorService executor;
	private Class<?> mqClass;
	private Method mqOpen;
	private Method isQuitPending;
	private Method mqClose;

	@Override
	public void start(BundleContext bundleContext) throws Exception {
		ClassLoader cl = ClassLoader.getSystemClassLoader();
		while(cl.getParent() != null) {
			cl = cl.getParent();
		}
		this.mqClass = Class.forName("lotus.notes.internal.MessageQueue", true, cl); //$NON-NLS-1$
		this.mqOpen = this.mqClass.getMethod("open", String.class, int.class); //$NON-NLS-1$
		this.isQuitPending = this.mqClass.getMethod("isQuitPending"); //$NON-NLS-1$
		this.mqClose = this.mqClass.getMethod("close", int.class); //$NON-NLS-1$
		
		this.executor = Executors.newScheduledThreadPool(10, NotesThread::new);
		this.executor.scheduleAtFixedRate(() -> {
			if(isHttpQuitting()) {
				ExecutorHolder.INSTANCE.termAll();
			}
		}, 0, 10, TimeUnit.SECONDS);
	}

	@Override
	public void stop(BundleContext bundleContext) throws Exception {
		ExecutorHolder.INSTANCE.termAll();
		if(!(executor.isShutdown() || executor.isTerminated())) {
			try {
				executor.shutdownNow();
				executor.awaitTermination(5, TimeUnit.MINUTES);
			} catch (Exception e) {
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
