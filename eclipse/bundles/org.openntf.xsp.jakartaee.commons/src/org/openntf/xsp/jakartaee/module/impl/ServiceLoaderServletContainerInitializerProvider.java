package org.openntf.xsp.jakartaee.module.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.ServiceLoader;

import org.openntf.xsp.jakartaee.module.ServletContainerInitializerProvider;

import com.ibm.designer.runtime.domino.adapter.ComponentModule;

import jakarta.servlet.ServletContainerInitializer;

/**
 * This implementation of {@link ServletContainerInitializerProvider} loads initializers
 * using the META-INF/services mechanism.
 */
public class ServiceLoaderServletContainerInitializerProvider implements ServletContainerInitializerProvider {

	@Override
	public Collection<ServletContainerInitializer> provide(ComponentModule module) {
		ClassLoader cl = module.getModuleClassLoader();
		if(cl != null) {
			return ServiceLoader.load(ServletContainerInitializer.class, cl).stream()
				.map(ServiceLoader.Provider::get)
				.toList();
		} else {
			return Collections.emptySet();
		}
	}

}
