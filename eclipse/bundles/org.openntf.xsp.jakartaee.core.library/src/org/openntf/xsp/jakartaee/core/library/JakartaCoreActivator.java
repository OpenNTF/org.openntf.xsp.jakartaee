package org.openntf.xsp.jakartaee.core.library;

import org.openntf.xsp.cdi.CDIActivator;
import org.openntf.xsp.jakarta.concurrency.ConcurrencyActivator;
import org.openntf.xsp.jakarta.persistence.PersistenceActivator;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class JakartaCoreActivator implements BundleActivator {

	@Override
	public void start(BundleContext context) throws Exception {
		// Make sure some important activators are run
		@SuppressWarnings("unused")
		Class<?>[] c = new Class<?>[] {
			PersistenceActivator.class,
			CDIActivator.class,
			ConcurrencyActivator.class
		};
	}

	@Override
	public void stop(BundleContext context) throws Exception {

	}

}
