package org.openntf.xsp.jaxrs.spi;

import org.osgi.framework.BundleActivator;

/**
 * Extension point to register a service that will be called during activation
 * of the main JAX-RS bundle.
 * 
 * @since 2.16.0
 */
public interface JAXRSActivationParticipant extends BundleActivator {

}
