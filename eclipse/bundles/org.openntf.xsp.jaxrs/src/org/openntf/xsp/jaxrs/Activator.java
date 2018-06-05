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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.ws.rs.ProcessingException;

import org.eclipse.core.runtime.Plugin;
import org.glassfish.jersey.inject.cdi.se.CdiSeInjectionManagerFactory;
import org.glassfish.jersey.internal.OsgiRegistry;
import org.glassfish.jersey.internal.ServiceFinder;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

public class Activator extends Plugin {
	private static Activator instance;
	
	public static Activator getDefault() {
		return instance;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void start(BundleContext bundleContext) throws Exception {
		instance = this;
		
		// Sneak in an OsgiRegistry instance
		AccessController.doPrivileged((PrivilegedExceptionAction<Void>)() -> {
			Class<OsgiRegistry> clazz = OsgiRegistry.class;
			Constructor<OsgiRegistry> cons = (Constructor<OsgiRegistry>) clazz.getDeclaredConstructors()[0];
			cons.setAccessible(true);
			OsgiRegistry reg = cons.newInstance(bundleContext);
			Field instance = clazz.getDeclaredField("instance"); //$NON-NLS-1$
			instance.setAccessible(true);
			instance.set(null, reg);
			
			Method hookUp = clazz.getDeclaredMethod("hookUp"); //$NON-NLS-1$
			hookUp.setAccessible(true);
			hookUp.invoke(reg);
			
			// The OsgiRegistry in 2.27 is broken and attempts to load classes with comment lines - patch over that
			try {
				@SuppressWarnings("unused")
				Class<?> serviceFinder = ServiceFinder.class; // Kick off the static initializer
				Field factories = clazz.getDeclaredField("factories"); //$NON-NLS-1$
				factories.setAccessible(true);
				Map<Long, Map<String, Callable<List<Class<?>>>>> facs = (Map<Long, Map<String, Callable<List<Class<?>>>>>)factories.get(reg);
				for(Map<String, Callable<List<Class<?>>>> mapVal : facs.values()) {
					for(Map.Entry<String, Callable<List<Class<?>>>> entry : mapVal.entrySet()) {
						entry.setValue(convertCallable(entry.getValue()));
					}
				}
			} catch(Throwable t) {
				t.printStackTrace();
			}
			
			return null;
		});
	}

	@Override
	public void stop(BundleContext bundleContext) throws Exception {
	}
	
	// Will be BundleSpiProvidersLoader
	private static Class<?> callableClazz;
	private static Field callableSpi; // String
	private static Field callableSpiRegistryUrl; // URL
	private static Field callableBundle; // Bundle
	
	private static Callable<List<Class<?>>> convertCallable(Callable<List<Class<?>>> callable) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		if(callableClazz == null) {
			callableClazz = callable.getClass();
			callableSpi = callableClazz.getDeclaredField("spi"); //$NON-NLS-1$
			callableSpi.setAccessible(true);
			callableSpiRegistryUrl = callableClazz.getDeclaredField("spiRegistryUrl"); //$NON-NLS-1$
			callableSpiRegistryUrl.setAccessible(true);
			callableBundle = callableClazz.getDeclaredField("bundle"); //$NON-NLS-1$
			callableBundle.setAccessible(true);
		}
		
		String spi = (String)callableSpi.get(callable);
		URL spiRegistryUrl = (URL)callableSpiRegistryUrl.get(callable);
		Bundle bundle = (Bundle)callableBundle.get(callable);
		
		return new PatchedBundleSpiProvidersLoader(spi, spiRegistryUrl, bundle);
	}

	private static class PatchedBundleSpiProvidersLoader implements Callable<List<Class<?>>> {

        @SuppressWarnings("unused")
		private final String spi;
        private final URL spiRegistryUrl;
        private final String spiRegistryUrlString;
        private final Bundle bundle;

        PatchedBundleSpiProvidersLoader(final String spi, final URL spiRegistryUrl, final Bundle bundle) {
            this.spi = spi;
            this.spiRegistryUrl = spiRegistryUrl;
            this.spiRegistryUrlString = spiRegistryUrl.toExternalForm();
            this.bundle = bundle;
        }

        @Override
        public List<Class<?>> call() throws Exception {
            try(BufferedReader reader = new BufferedReader(new InputStreamReader(spiRegistryUrl.openStream(), "UTF-8"))) { //$NON-NLS-1$
                String providerClassName;

                final List<Class<?>> providerClasses = new ArrayList<Class<?>>();
                while ((providerClassName = reader.readLine()) != null) {
                    if (providerClassName.trim().length() == 0) {
                        continue;
                    }
                    if(providerClassName.startsWith("#")) { //$NON-NLS-1$
                    	continue;
                    }
                    
                    if(providerClassName.equals(CdiSeInjectionManagerFactory.class.getName())) {
                    	continue;
                    }
                    
                    providerClasses.add(loadClass(bundle, providerClassName));
                }

                return providerClasses;
            } catch (final Exception | Error e) {
            	e.printStackTrace();
                throw e;
            }
        }

        @Override
        public String toString() {
            return spiRegistryUrlString;
        }

        @Override
        public int hashCode() {
            return spiRegistryUrlString.hashCode();
        }

        @Override
        public boolean equals(final Object obj) {
            if (obj instanceof PatchedBundleSpiProvidersLoader) {
                return spiRegistryUrlString.equals(((PatchedBundleSpiProvidersLoader) obj).spiRegistryUrlString);
            } else {
                return false;
            }
        }
    }
	
	private static Class<?> loadClass(final Bundle bundle, final String className) throws ClassNotFoundException {
        try {
            return AccessController.doPrivileged(new PrivilegedExceptionAction<Class<?>>() {
                @Override
                public Class<?> run() throws ClassNotFoundException {
                    return bundle.loadClass(className);
                }
            });
        } catch (final PrivilegedActionException ex) {
            final Exception originalException = ex.getException();
            if (originalException instanceof ClassNotFoundException) {
                throw (ClassNotFoundException) originalException;
            } else if (originalException instanceof RuntimeException) {
                throw (RuntimeException) originalException;
            } else {
                throw new ProcessingException(originalException);
            }
        }
    }
}
