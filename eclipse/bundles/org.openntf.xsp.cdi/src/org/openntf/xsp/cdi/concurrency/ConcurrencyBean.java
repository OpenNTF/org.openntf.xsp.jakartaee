package org.openntf.xsp.cdi.concurrency;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.openntf.xsp.jakarta.concurrency.servlet.ConcurrencyRequestListener;
import jakarta.enterprise.concurrent.ManagedExecutorService;
import jakarta.enterprise.concurrent.ManagedScheduledExecutorService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Named;

/**
 * Provides {@link ManagedExecutorService} and {@link ManagedScheduledExecutorService}
 * instances to the running application via CDI.
 * 
 * @author Jesse Gallagher
 * @since 2.7.0
 */
@ApplicationScoped
public class ConcurrencyBean {

	@Produces @Named(ConcurrencyRequestListener.JNDI_EXECUTORSERVICE)
	public ManagedExecutorService produceExecutorService() {
		try {
			return InitialContext.doLookup(ConcurrencyRequestListener.JNDI_EXECUTORSERVICE);
		} catch (NamingException e) {
			throw new RuntimeException("Encountered exception looking up ManagedExecutorService");
		}
	}
	
	@Produces @Named(ConcurrencyRequestListener.JNDI_SCHEDULEDEXECUTORSERVICE)
	public ManagedScheduledExecutorService produceScheduledExecutorService() {
		try {
			return InitialContext.doLookup(ConcurrencyRequestListener.JNDI_SCHEDULEDEXECUTORSERVICE);
		} catch (NamingException e) {
			throw new RuntimeException("Encountered exception looking up ManagedExecutorService");
		}
	}

}
