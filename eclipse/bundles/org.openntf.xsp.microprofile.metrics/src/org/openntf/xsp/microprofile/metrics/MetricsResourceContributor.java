package org.openntf.xsp.microprofile.metrics;

import java.util.Arrays;
import java.util.Collection;

import org.openntf.xsp.jaxrs.JAXRSClassContributor;
import org.openntf.xsp.microprofile.metrics.jaxrs.MetricsResource;

import io.smallrye.metrics.jaxrs.JaxRsMetricsFilter;

public class MetricsResourceContributor implements JAXRSClassContributor {

	@Override
	public Collection<Class<?>> getClasses() {
		return Arrays.asList(
			JaxRsMetricsFilter.class,
			MetricsResource.class
		);
	}

}
