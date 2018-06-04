/**
 * Copyright © 2018 Martin Pradny and Jesse Gallagher
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
package org.openntf.xsp.jaxrs;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedExceptionAction;

import org.eclipse.core.runtime.Plugin;
import org.glassfish.hk2.osgiresourcelocator.ServiceLoader;
import org.glassfish.jersey.inject.hk2.Hk2InjectionManagerFactory;
import org.glassfish.jersey.internal.OsgiRegistry;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;

public class Activator extends Plugin {
	private static Activator instance;
	
	public static Activator getDefault() {
		return instance;
	}

	@Override
	public void start(BundleContext bundleContext) throws Exception {
		instance = this;
		
		System.setProperty(Hk2InjectionManagerFactory.HK2_INJECTION_MANAGER_STRATEGY, "delayed"); //$NON-NLS-1$
		
		// Sneak in an OsgiRegistry instance
		AccessController.doPrivileged((PrivilegedExceptionAction<Void>)() -> {
			Class<OsgiRegistry> clazz = OsgiRegistry.class;
			@SuppressWarnings("unchecked")
			Constructor<OsgiRegistry> cons = (Constructor<OsgiRegistry>) clazz.getDeclaredConstructors()[0];
			cons.setAccessible(true);
			OsgiRegistry reg = cons.newInstance(bundleContext);
			Field instance = clazz.getDeclaredField("instance"); //$NON-NLS-1$
			instance.setAccessible(true);
			instance.set(null, reg);
			return null;
		});
		
		try {
			//ServiceLoader.initialize(new DominoServiceLoader(bundleContext));
			FrameworkUtil.getBundle(ServiceLoader.class).start();
		} catch(Throwable t) {
			t.printStackTrace();
		}
	}

	@Override
	public void stop(BundleContext bundleContext) throws Exception {
	}

}
