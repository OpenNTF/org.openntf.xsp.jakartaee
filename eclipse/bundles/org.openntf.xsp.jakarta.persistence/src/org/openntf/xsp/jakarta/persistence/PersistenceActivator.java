package org.openntf.xsp.jakarta.persistence;

import java.security.AccessController;
import java.security.PrivilegedAction;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import jakarta.persistence.spi.PersistenceProviderResolverHolder;

public class PersistenceActivator implements BundleActivator {

	@Override
	public void start(BundleContext bundleContext) throws Exception {
		AccessController.doPrivileged((PrivilegedAction<Void>)() -> {
			System.setProperty("eclipselink.logging.logger", "JavaLogger"); //$NON-NLS-1$ //$NON-NLS-2$
			return null;
		});
		PersistenceProviderResolverHolder.setPersistenceProviderResolver(new EclipseLinkResolver());
	}

	@Override
	public void stop(BundleContext bundleContext) throws Exception {

	}

}
