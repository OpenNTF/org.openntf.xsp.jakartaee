package org.openntf.xsp.jakarta.persistence;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import jakarta.persistence.spi.PersistenceProviderResolverHolder;

public class PersistenceActivator implements BundleActivator {

	@Override
	public void start(BundleContext bundleContext) throws Exception {
		PersistenceProviderResolverHolder.setPersistenceProviderResolver(new EclipseLinkResolver());
	}

	@Override
	public void stop(BundleContext bundleContext) throws Exception {

	}

}
