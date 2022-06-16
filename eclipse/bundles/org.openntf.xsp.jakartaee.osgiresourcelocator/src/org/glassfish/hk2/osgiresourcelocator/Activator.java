package org.glassfish.hk2.osgiresourcelocator;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {
	
	@Override
	public void start(BundleContext bundleContext) throws Exception {
		ServiceLoader.init(bundleContext);
	}

	@Override
	public void stop(BundleContext bundleContext) throws Exception {
		
	}

}
