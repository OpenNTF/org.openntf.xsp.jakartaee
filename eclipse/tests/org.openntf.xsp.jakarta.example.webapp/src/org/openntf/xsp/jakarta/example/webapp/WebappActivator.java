package org.openntf.xsp.jakarta.example.webapp;

import org.openntf.xsp.cdi.provider.DominoCDIProvider;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import jakarta.enterprise.inject.spi.CDI;

public class WebappActivator implements BundleActivator {

	@Override
	public void start(BundleContext context) throws Exception {
		CDI.setCDIProvider(new DominoCDIProvider());
	}

	@Override
	public void stop(BundleContext context) throws Exception {

	}

}
