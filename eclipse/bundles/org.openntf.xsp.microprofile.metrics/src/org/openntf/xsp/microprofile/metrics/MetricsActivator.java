/**
 * Copyright (c) 2018-2025 Contributors to the XPages Jakarta EE Support Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openntf.xsp.microprofile.metrics;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import io.micrometer.core.instrument.Metrics;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import io.smallrye.metrics.MPPrometheusConfig;
import io.smallrye.metrics.SharedMetricRegistries;

public class MetricsActivator implements BundleActivator {

	@Override
	public void start(final BundleContext context) throws Exception {
		// Ensure that this class is initialized, which sets a registry
		SharedMetricRegistries.getAppNameResolver();

		PrometheusMeterRegistry existing = Metrics.globalRegistry.getRegistries().stream()
	        .filter(PrometheusMeterRegistry.class::isInstance)
	        .map(PrometheusMeterRegistry.class::cast)
	        .findFirst()
	        .get();
		Metrics.globalRegistry.remove(existing);
		Metrics.globalRegistry.add(new DelegatingPrometheusMeterRegistry(new MPPrometheusConfig()));
	}

	@Override
	public void stop(final BundleContext context) throws Exception {
		// TODO Auto-generated method stub

	}

}
