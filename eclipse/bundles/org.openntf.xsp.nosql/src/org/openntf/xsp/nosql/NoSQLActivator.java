package org.openntf.xsp.nosql;

import java.util.ServiceLoader;
import java.util.stream.StreamSupport;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import jakarta.nosql.ServiceLoaderProvider;

/**
 * @author Jesse Gallagher
 * @since 2.3.0
 */
public class NoSQLActivator implements BundleActivator {

	@Override
	public void start(BundleContext bundleContext) throws Exception {
		try {
			ServiceLoaderProvider.setLoader(c -> {
				return StreamSupport.stream(ServiceLoader.load(c, c.getClassLoader()).spliterator(), false);
			});
		} catch(Throwable t) {
			t.printStackTrace();
		}
	}

	@Override
	public void stop(BundleContext bundleContext) throws Exception {
	}

}
