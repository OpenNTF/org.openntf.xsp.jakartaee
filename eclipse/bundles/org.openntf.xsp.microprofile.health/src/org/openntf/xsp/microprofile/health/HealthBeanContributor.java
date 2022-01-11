package org.openntf.xsp.microprofile.health;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

import org.openntf.xsp.cdi.discovery.WeldBeanClassContributor;
import org.openntf.xsp.cdi.util.DiscoveryUtil;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.osgi.framework.FrameworkUtil;

import io.smallrye.health.ResponseProvider;
import jakarta.enterprise.inject.spi.Extension;

/**
 * @author Jesse Gallagher
 * @since 2.2.0
 */
public class HealthBeanContributor implements WeldBeanClassContributor {

	@Override
	public Collection<Class<?>> getBeanClasses() {
		// Look for annotated beans in io.smallrye.health
		Bundle bundle = FrameworkUtil.getBundle(ResponseProvider.class);
		try {
			return DiscoveryUtil.findExportedClasses(bundle)
				.map(t -> {
					try {
						return bundle.loadClass(t);
					} catch (ClassNotFoundException e) {
						throw new RuntimeException(e);
					}
				})
				// TODO filter to only annotated
				.collect(Collectors.toList());
		} catch (BundleException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Collection<Extension> getExtensions() {
		return Collections.emptyList();
	}

}
