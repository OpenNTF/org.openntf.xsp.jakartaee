package org.openntf.xsp.cdi.discovery;

import java.util.Collection;

import javax.enterprise.inject.spi.Extension;

public interface WeldBeanClassContributor {
	public static final String EXTENSION_POINT = WeldBeanClassContributor.class.getName();
	
	Collection<Class<?>> getBeanClasses();
	
	Collection<Extension> getExtensions();
}
