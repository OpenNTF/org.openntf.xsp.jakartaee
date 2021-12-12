package org.openntf.xsp.jaxrs.weld;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.jboss.resteasy.cdi.ResteasyCdiExtension;
import org.openntf.xsp.cdi.discovery.WeldBeanClassContributor;

import jakarta.enterprise.inject.spi.Extension;

public class ResteasyBeanClassContributor implements WeldBeanClassContributor {

	@Override
	public Collection<Class<?>> getBeanClasses() {
		return Collections.emptyList();
	}

	@Override
	public Collection<Extension> getExtensions() {
		return Arrays.asList(
			new ResteasyCdiExtension()
		);
	}

}
