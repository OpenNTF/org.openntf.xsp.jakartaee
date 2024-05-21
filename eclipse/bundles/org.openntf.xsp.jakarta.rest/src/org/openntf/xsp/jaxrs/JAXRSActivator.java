package org.openntf.xsp.jaxrs;

import org.openntf.xsp.jakartaee.util.LibraryUtil;
import org.openntf.xsp.jaxrs.spi.JAXRSActivationParticipant;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * @since 2.16.0
 */
public class JAXRSActivator implements BundleActivator {

	@Override
	public void start(BundleContext context) throws Exception {
		for(JAXRSActivationParticipant p : LibraryUtil.findExtensions(JAXRSActivationParticipant.class)) {
			p.start(context);
		}
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		for(JAXRSActivationParticipant p : LibraryUtil.findExtensions(JAXRSActivationParticipant.class)) {
			p.stop(context);
		}
	}

}
