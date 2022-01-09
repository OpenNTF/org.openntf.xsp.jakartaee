package org.openntf.xsp.microprofile.metrics;

import java.util.Collection;
import java.util.Collections;

import org.openntf.xsp.cdi.discovery.WeldBeanClassContributor;

import io.smallrye.metrics.setup.MetricCdiInjectionExtension;
import jakarta.enterprise.inject.spi.Extension;

public class MetricsExtensionContributor implements WeldBeanClassContributor {

	@Override
	public Collection<Class<?>> getBeanClasses() {
		return Collections.emptyList();
	}

	@Override
	public Collection<Extension> getExtensions() {
		// TODO add opt-in for NSFs
		return Collections.singleton(new MetricCdiInjectionExtension());
	}

}
