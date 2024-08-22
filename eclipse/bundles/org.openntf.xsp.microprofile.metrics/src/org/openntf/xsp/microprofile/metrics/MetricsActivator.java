package org.openntf.xsp.microprofile.metrics;

import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Map;

import org.eclipse.microprofile.metrics.MetricRegistry;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import io.smallrye.metrics.MetricRegistries;

public class MetricsActivator implements BundleActivator {

	@SuppressWarnings("unchecked")
	@Override
	public void start(BundleContext context) throws Exception {
		// Fill MetricsRegistries with delegating implementations
		//   to keep things per-app
		
		AccessController.doPrivileged((PrivilegedAction<Void>)() -> {
			try {
				Class<MetricRegistries> registriesClass = MetricRegistries.class;
				Field registriesField = registriesClass.getDeclaredField("registries"); //$NON-NLS-1$
				registriesField.setAccessible(true);
				
				Map<MetricRegistry.Type, MetricRegistry> registries = (Map<MetricRegistry.Type, MetricRegistry>)registriesField.get(null);
				
				for(MetricRegistry.Type type : MetricRegistry.Type.values()) {
					registries.put(type, new DelegatingMetricRegistry(type));
				}
			} catch(Exception e) {
				e.printStackTrace();
			}
			return null;
		});
	}

	@Override
	public void stop(BundleContext context) throws Exception {

	}

}
