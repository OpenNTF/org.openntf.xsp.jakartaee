package org.openntf.xsp.jakarta.concurrency.jndi;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.spi.ObjectFactory;

import org.openntf.xsp.jakarta.concurrency.ConcurrencyActivator;

/**
 * @since 3.7.0
 */
public class ManagedExecutorServiceFactory implements ObjectFactory {

	@Override
	public Object getObjectInstance(Object obj, Name name, Context nameCtx, Hashtable<?, ?> environment) {
		switch(String.valueOf(name)) {
			case ConcurrencyActivator.JNDI_EXECUTORSERVICE:
				return DelegatingManagedExecutorService.INSTANCE;
			case ConcurrencyActivator.JNDI_SCHEDULEDEXECUTORSERVICE:
				return DelegatingManagedScheduledExecutorService.INSTANCE;
			default:
				return null;
		}
	}

}
