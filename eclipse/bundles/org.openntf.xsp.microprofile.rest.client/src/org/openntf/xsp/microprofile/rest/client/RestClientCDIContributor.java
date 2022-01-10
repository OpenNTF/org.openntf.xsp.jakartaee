package org.openntf.xsp.microprofile.rest.client;

import java.util.Collection;
import java.util.Collections;

import org.jboss.resteasy.microprofile.client.RestClientExtension;
import org.openntf.xsp.cdi.discovery.WeldBeanClassContributor;

import jakarta.enterprise.inject.spi.Extension;

/**
 * @author Jesse Gallagher
 * @since 2.2.0
 */
public class RestClientCDIContributor implements WeldBeanClassContributor {

	@Override
	public Collection<Class<?>> getBeanClasses() {
		return Collections.emptyList();
	}

	@Override
	public Collection<Extension> getExtensions() {
		return Collections.singleton(new RestClientExtension());
	}

}
