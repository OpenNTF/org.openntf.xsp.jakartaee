package org.openntf.xsp.jaxrs.weld;

import java.util.Arrays;
import java.util.Collection;

import javax.enterprise.inject.spi.Extension;

import org.glassfish.jersey.inject.cdi.se.CdiRequestScope;
import org.glassfish.jersey.server.internal.process.RequestProcessingContextReference;
import org.openntf.xsp.cdi.discovery.WeldBeanClassContributor;

public class JerseyBeanClassContributor implements WeldBeanClassContributor {

	@Override
	public Collection<Class<?>> getBeanClasses() {
		return Arrays.asList(
			RequestProcessingContextReference.class,
			CdiRequestScope.class,
			NoopExternalRequestScope.class
//			RefImpl.class
		);
	}
	
	@Override
	public Collection<Extension> getExtensions() {
		return Arrays.asList(
			new JerseyBeanRegisterExtension(WeldContextInjectionManager.instance.getBindings())
		);
	}

}
