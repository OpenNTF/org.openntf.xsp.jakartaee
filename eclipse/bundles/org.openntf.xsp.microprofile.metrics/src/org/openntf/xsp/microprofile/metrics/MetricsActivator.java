package org.openntf.xsp.microprofile.metrics;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import io.micrometer.core.instrument.Metrics;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import io.smallrye.metrics.MPPrometheusConfig;
import io.smallrye.metrics.SharedMetricRegistries;

public class MetricsActivator implements BundleActivator {

	@Override
	public void start(BundleContext context) throws Exception {
		// Ensure that this class is initialized, which sets a registry
		SharedMetricRegistries.getAppNameResolver();
		
		PrometheusMeterRegistry existing = Metrics.globalRegistry.getRegistries().stream()
	        .filter(registry -> registry instanceof PrometheusMeterRegistry)
	        .map(PrometheusMeterRegistry.class::cast)
	        .findFirst()
	        .get();
		Metrics.globalRegistry.remove(existing);
		Metrics.globalRegistry.add(new DelegatingPrometheusMeterRegistry(new MPPrometheusConfig()));
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		// TODO Auto-generated method stub

	}

}
