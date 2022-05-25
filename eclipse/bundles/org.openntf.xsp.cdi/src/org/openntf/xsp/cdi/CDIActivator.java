package org.openntf.xsp.cdi;

import org.openntf.xsp.cdi.provider.NSFCDIProvider;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import jakarta.enterprise.inject.spi.CDI;

/**
 * @author Jesse Gallagher
 * @since 2.5.0
 */
public class CDIActivator implements BundleActivator {

	@Override
	public void start(BundleContext context) throws Exception {
		CDI.setCDIProvider(new NSFCDIProvider());
	}

	@Override
	public void stop(BundleContext context) throws Exception {

	}

}
