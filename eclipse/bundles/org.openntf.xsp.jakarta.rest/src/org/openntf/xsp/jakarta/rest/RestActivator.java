package org.openntf.xsp.jakarta.rest;

import org.openntf.xsp.jakarta.rest.spi.RestActivationParticipant;
import org.openntf.xsp.jakartaee.util.LibraryUtil;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * @since 2.16.0
 */
public class RestActivator implements BundleActivator {

	@Override
	public void start(BundleContext context) throws Exception {
		for(RestActivationParticipant p : LibraryUtil.findExtensions(RestActivationParticipant.class)) {
			p.start(context);
		}
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		for(RestActivationParticipant p : LibraryUtil.findExtensions(RestActivationParticipant.class)) {
			p.stop(context);
		}
	}

}
