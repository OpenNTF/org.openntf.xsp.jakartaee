/**
 * Copyright © 2018-2020 Jesse Gallagher
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
package org.openntf.xsp.cdi.util;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;

import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.CDI;
import javax.enterprise.inject.spi.Extension;

import org.eclipse.core.runtime.Platform;
import org.eclipse.osgi.util.ManifestElement;
import org.jboss.weld.config.ConfigurationKey;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.resources.ClassLoaderResourceLoader;
import org.jboss.weld.resources.spi.ResourceLoader;
import org.jboss.weld.util.ForwardingBeanManager;
import org.openntf.xsp.cdi.CDILibrary;
import org.openntf.xsp.cdi.discovery.OSGiServletBeanArchiveHandler;
import org.openntf.xsp.cdi.discovery.WeldBeanClassContributor;
import org.openntf.xsp.jakartaee.LibraryUtil;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleException;
import org.osgi.framework.wiring.BundleWiring;

import com.ibm.commons.extension.ExtensionManager;
import com.ibm.commons.util.StringUtil;
import com.ibm.designer.domino.napi.NotesAPIException;
import com.ibm.designer.domino.napi.NotesDatabase;
import com.ibm.designer.runtime.domino.adapter.ComponentModule;
import com.ibm.domino.xsp.module.nsf.NotesContext;
import com.ibm.xsp.application.ApplicationEx;

/**
 * Utility methods for working with Weld containers based on a given XPages application
 * or running environment.
 * 
 * @author Jesse Gallagher
 * @since 1.0.0
 */
public enum ContainerUtil {
	;
	
	/**
	 * @since 1.2.0
	 */
	private static final ThreadLocal<String> threadContextDatabasePath = new ThreadLocal<>();

	/**
	 * Gets or created a {@link WeldContainer} instance for the provided Application.
	 * 
	 * @param application the active {@link ApplicationEx}
	 * @return an existing or new {@link WeldContainer}
	 */
	@SuppressWarnings("unchecked")
	public static synchronized CDI<Object> getContainer(ApplicationEx application) {
		if(LibraryUtil.usesLibrary(CDILibrary.LIBRARY_ID, application)) {
			String bundleId = getApplicationCDIBundle(application);
			if(StringUtil.isNotEmpty(bundleId)) {
				Bundle bundle = Platform.getBundle(bundleId);
				if(bundle != null) {
					return getContainer(bundle);
				}
			}
			
			WeldContainer instance = WeldContainer.instance(application.getApplicationId());
			if(instance == null) {
				Weld weld = new Weld()
					.containerId(application.getApplicationId())
					.property(Weld.SCAN_CLASSPATH_ENTRIES_SYSTEM_PROPERTY, true)
					// Disable concurrent deployment to avoid Notes thread init trouble
					.property(ConfigurationKey.CONCURRENT_DEPLOYMENT.get(), false)
					.setResourceLoader(new ModuleContextResourceLoader(NotesContext.getCurrent().getModule()));
				
				for(WeldBeanClassContributor service : (List<WeldBeanClassContributor>)application.findServices(WeldBeanClassContributor.EXTENSION_POINT)) {
					Collection<Class<?>> beanClasses = service.getBeanClasses();
					if(beanClasses != null) {
						weld.addBeanClasses(beanClasses.toArray(new Class<?>[beanClasses.size()]));
					}
					Collection<Extension> extensions = service.getExtensions();
					if(extensions != null) {
						weld.addExtensions(extensions.toArray(new Extension[extensions.size()]));
					}
				}
				
				instance = weld.initialize();
			}
			return instance;
		} else {
			return null;
		}
	}
	
	/**
	 * Retrieves or creates a CDI container specific to the provided OSGi bundle.
	 * 
	 * <p>The container is registered as an OSGi service.</p>
	 * 
	 * @param bundle the bundle to find the container for
	 * @return the existing container or a newly-registered one if one did not exist
	 * @since 1.1.0
	 */
	public static synchronized CDI<Object> getContainer(Bundle bundle) {
		String id = bundle.getSymbolicName();

		WeldContainer instance = WeldContainer.instance(id);
		if(instance == null) {
			try {
				// Register a new one
				Weld weld = new Weld()
					.containerId(id)
					.scanClasspathEntries()
					.property(Weld.SCAN_CLASSPATH_ENTRIES_SYSTEM_PROPERTY, true)
					// Disable concurrent deployment to avoid Notes thread init trouble
					.property(ConfigurationKey.CONCURRENT_DEPLOYMENT.get(), false)
					.setResourceLoader(new BundleDependencyResourceLoader(bundle));
				
				OSGiServletBeanArchiveHandler.PROCESSING_BUNDLE.set(bundle);
				try {
					instance = weld.initialize();
				} finally {
					OSGiServletBeanArchiveHandler.PROCESSING_BUNDLE.set(null);
				}
				
				bundle.getBundleContext().addBundleListener(e -> {
					if(e.getType() == BundleEvent.STOPPING) {
						@SuppressWarnings("rawtypes")
						CDI cdi = WeldContainer.instance(id);
						if(cdi instanceof WeldContainer) {
							try(WeldContainer c = (WeldContainer)cdi) {
								c.shutdown();
							}
						}
					}
				});
				
			} catch(IllegalStateException e) {
				System.err.println("Encountered exception while initializing CDI container for " + bundle.getSymbolicName());
				if(e.getMessage().contains("Class path entry does not exist or cannot be read")) {
					String classpath = AccessController.doPrivileged((PrivilegedAction<String>)() -> System.getProperty("java.class.path"));
					System.err.println("Current class path: " + classpath);
				}
				e.printStackTrace();
				return null;
			}
		}
		return instance;
	}
	
	/**
	 * 
	 * @param database the database to open
	 * @return an existing or new {@link WeldContainer}, or {@code null} if the application does not use CDI
	 * @throws NotesAPIException if there is a problem reading the database
	 * @throws IOException if there is a problem parsing the database configuration
	 */
	public static CDI<Object> getContainer(NotesDatabase database) throws NotesAPIException, IOException {
		if(LibraryUtil.usesLibrary(CDILibrary.LIBRARY_ID, database)) {
			String bundleId = getApplicationCDIBundle(database);
			if(StringUtil.isNotEmpty(bundleId)) {
				Bundle bundle = Platform.getBundle(bundleId);
				if(bundle != null) {
					return getContainer(bundle);
				}
			}
			
			String id = database.getDatabasePath().replace('\\', '/');
			WeldContainer instance = WeldContainer.instance(id);
			if(instance == null) {
				Weld weld = new Weld()
					.containerId(id)
					.property(Weld.SCAN_CLASSPATH_ENTRIES_SYSTEM_PROPERTY, true)
					// Disable concurrent deployment to avoid Notes thread init trouble
					.property(ConfigurationKey.CONCURRENT_DEPLOYMENT.get(), false)
					.setResourceLoader(new ModuleContextResourceLoader(NotesContext.getCurrent().getModule()));
				
				for(WeldBeanClassContributor service : ExtensionManager.findServices(null, Thread.currentThread().getContextClassLoader(), WeldBeanClassContributor.EXTENSION_POINT, WeldBeanClassContributor.class)) {
					Collection<Class<?>> beanClasses = service.getBeanClasses();
					if(beanClasses != null) {
						weld.addBeanClasses(beanClasses.toArray(new Class<?>[beanClasses.size()]));
					}
					Collection<Extension> extensions = service.getExtensions();
					if(extensions != null) {
						weld.addExtensions(extensions.toArray(new Extension[extensions.size()]));
					}
				}
				
				instance = weld.initialize();
			}
			return instance;
		} else {
			return null;
		}
	}
	
	/**
	 * Returns the configured override bundle ID for the application, if any.
	 * 
	 * @param application the application to check
	 * @return the name of the bundle to bind the container to, or <code>null</code>
	 *   if not specified
	 * @since 1.1.0
	 */
	public static String getApplicationCDIBundle(ApplicationEx application) {
		return application.getProperty(CDILibrary.LIBRARY_ID + ".cdibundle", null); //$NON-NLS-1$
	}
	
	/**
	 * Returns the configured override bundle ID for the application, if any.
	 * 
	 * @param database the application to check
	 * @return the name of the bundle to bind the container to, or <code>null</code>
	 *   if not specified
	 * @since 1.2.0
	 * @throws IOException if there is a problem reading the xsp.properties file in the module
	 * @throws NotesAPIException if there is a problem reading the xsp.properties file in the module
	 */
	public static String getApplicationCDIBundle(NotesDatabase database) throws NotesAPIException, IOException {
		Properties props = LibraryUtil.getXspProperties(database);
		return props.getProperty(CDILibrary.LIBRARY_ID + ".cdibundle", null); //$NON-NLS-1$
	}

	public static BeanManagerImpl getBeanManager(ApplicationEx application) {
		CDI<Object> container = getContainer(application);
		BeanManager manager = container.getBeanManager();
		if(manager instanceof BeanManagerImpl) {
			return (BeanManagerImpl)manager;
		} else if(manager instanceof ForwardingBeanManager) {
			return (BeanManagerImpl) ((ForwardingBeanManager)manager).delegate();
		} else {
			throw new IllegalStateException("Cannot find BeanManagerImpl in " + manager); //$NON-NLS-1$
		}
	}
	
	/**
	 * @since 1.2.0
	 */
	public static BeanManagerImpl getBeanManager(CDI<Object> container) {
		BeanManager manager = container.getBeanManager();
		if(manager instanceof BeanManagerImpl) {
			return (BeanManagerImpl)manager;
		} else if(manager instanceof ForwardingBeanManager) {
			return (BeanManagerImpl) ((ForwardingBeanManager)manager).delegate();
		} else {
			throw new IllegalStateException("Cannot find BeanManagerImpl in " + manager); //$NON-NLS-1$
		}
	}
	
	/**
	 * Sets the database path to use when determining the active container for dynamic lookups. When
	 * set to a non-empty value, this value takes priority over XPages or OSGi servlet contextual
	 * information.
	 * 
	 * @param databasePath the path of the contextual database to use when looking up containers;
	 * 		may be {@code null} to disable an override
	 * @since 1.2.0
	 */
	public static void setThreadContextDatabasePath(String databasePath) {
		threadContextDatabasePath.set(databasePath);
	}
	
	/**
	 * Retrieves the value set by {@linlk #setThreadContextDatabasePath(String)} for an overridden
	 * contextual database path for containers.
	 * 
	 * @return the path of the set contextual database to use when looking up containers; may be
	 * 		{@code null} or empty
	 * @since 1.2.0
	 */
	public static String getThreadContextDatabasePath() {
		return threadContextDatabasePath.get();
	}
	
	// *******************************************************************************
	// * Internal utilities
	// *******************************************************************************

	private static class ModuleContextResourceLoader extends ClassLoaderResourceLoader {
		private final ComponentModule module;

		public ModuleContextResourceLoader(ComponentModule module) {
			super(module.getModuleClassLoader());
			this.module = module;
		}
		
		@Override
		public Collection<URL> getResources(String name) {
			Collection<URL> result = new HashSet<>(super.getResources(name));
			try {
				URL moduleRes = module.getResource(name);
				if(moduleRes != null) {
					result.add(moduleRes);
				}
			} catch (MalformedURLException e) {
				// Ignore
			}
			return result;
		}
	}
	
	private static class BundleDependencyResourceLoader implements ResourceLoader {
		private final Bundle bundle;
		
		public BundleDependencyResourceLoader(Bundle bundle) {
			this.bundle = bundle;
		}

		@Override
		public Class<?> classForName(String name) {
			try {
				return Thread.currentThread().getContextClassLoader().loadClass(name);
			} catch (ClassNotFoundException e) {
				// Couldn't find it here
			}
			try {
				return bundle.adapt(BundleWiring.class).getClassLoader().loadClass(name);
			} catch (ClassNotFoundException e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public URL getResource(String name) {
			return bundle.adapt(BundleWiring.class).getClassLoader().getResource(name);
		}

		@Override
		public Collection<URL> getResources(String name) {
			Collection<URL> result = new HashSet<>();
			Collection<String> bundleNames = new HashSet<>(); 
			addBundleResources(name, bundle, result, bundleNames);
			return result;
		}

		@Override
		public void cleanup() {
			
		}
		
		private void addBundleResources(String name, Bundle bundle, Collection<URL> result, Collection<String> bundleNames) {
			if(bundleNames.contains(bundle.getSymbolicName())) {
				return;
			}
			bundleNames.add(bundle.getSymbolicName());
			
			try {
				Enumeration<URL> urls = bundle.getResources(name);
				if(urls != null) {
					result.addAll(Collections.list(urls));
				}
				
				if(StringUtil.isEmpty(bundle.getHeaders().get("Fragment-Host"))) { //$NON-NLS-1$
					Bundle[] fragments = Platform.getFragments(bundle);
					if(fragments != null) {
						for(Bundle fragment : fragments) {
							addBundleResources(name, fragment, result, bundleNames);
						}
					}
				}
				
				String requireBundle = bundle.getHeaders().get("Require-Bundle"); //$NON-NLS-1$
				if(StringUtil.isNotEmpty(requireBundle)) {
					ManifestElement[] elements = ManifestElement.parseHeader("Require-Bundle", requireBundle); //$NON-NLS-1$
					for(ManifestElement el : elements) {
						String bundleName = el.getValue();
						if(StringUtil.isNotEmpty(bundleName)) {
							Bundle dependency = Platform.getBundle(bundleName);
							if(dependency != null) {
								addBundleResources(name, dependency, result, bundleNames);
							}
						}
					}
				}
			} catch (IOException | BundleException e) {
				throw new RuntimeException(e);
			}
		}
	}
}
