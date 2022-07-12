package org.openntf.xsp.cdi.concurrency;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import jakarta.enterprise.concurrent.ManagedExecutorService;
import jakarta.enterprise.concurrent.ManagedScheduledExecutorService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

/**
 * Provides {@link ManagedExecutorService} and {@link ManagedScheduledExecutorService}
 * instances to the running application via CDI.
 * 
 * @author Jesse Gallagher
 * @since 2.7.0
 */
@ApplicationScoped
public class ConcurrencyBean {

	@Produces
	public ManagedExecutorService produceExecutorService() {
		try {
			return InitialContext.doLookup("java:comp/DefaultManagedExecutorService"); //$NON-NLS-1$
		} catch (NamingException e) {
			throw new RuntimeException("Encountered exception looking up ManagedExecutorService");
		}
	}
	
	@Produces
	public ManagedScheduledExecutorService produceScheduledExecutorService() {
		try {
			return InitialContext.doLookup("java:comp/DefaultManagedScheduledExecutorService"); //$NON-NLS-1$
		} catch (NamingException e) {
			throw new RuntimeException("Encountered exception looking up ManagedExecutorService");
		}
	}

}
