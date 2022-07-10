package org.openntf.xsp.cdi.concurrency;

import jakarta.annotation.Priority;
import org.openntf.xsp.cdi.ext.CDIContainerLocator;

/**
 * Provides the CDI environment from an active ServletContext, if available.
 * 
 * @author Jesse Gallagher
 * @since 2.7.0
 */
@Priority(100)
public class ConcurrencyCDIContainerLocator implements CDIContainerLocator {
	private static final ThreadLocal<Object> THREAD_CDI = new ThreadLocal<>();
	
	public static void setCdi(Object cdi) {
		THREAD_CDI.set(cdi);
	}
	
	@Override
	public Object getContainer() {
		return THREAD_CDI.get();
	}

}
