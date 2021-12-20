package org.openntf.xsp.cdi.bean;

import java.util.Collection;
import java.util.Collections;

import org.openntf.xsp.cdi.discovery.WeldBeanClassContributor;

import jakarta.enterprise.inject.spi.Extension;

/**
 * Provides implicit beans from this bundle to the new Weld containers.
 * 
 * @author Jesse Gallagher
 * @since 2.1.0
 */
public class BuiltinBeanClassContributor implements WeldBeanClassContributor {

	@Override
	public Collection<Class<?>> getBeanClasses() {
		return Collections.singleton(DominoFacesImplicitObjectProvider.class);
	}

	@Override
	public Collection<Extension> getExtensions() {
		return Collections.emptyList();
	}

}
