/**
 * Copyright © 2018-2022 Jesse Gallagher
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
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.function.Supplier;

import org.eclipse.core.runtime.Platform;
import org.eclipse.osgi.util.ManifestElement;
import org.jboss.weld.config.ConfigurationKey;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.module.ExpressionLanguageSupport;
import org.jboss.weld.module.web.el.WeldELResolver;
import org.jboss.weld.module.web.el.WeldExpressionFactory;
import org.jboss.weld.resources.ClassLoaderResourceLoader;
import org.jboss.weld.resources.spi.ResourceLoader;
import org.jboss.weld.util.ForwardingBeanManager;
import org.openntf.xsp.cdi.CDILibrary;
import org.openntf.xsp.cdi.context.CDIScopesExtension;
import org.openntf.xsp.cdi.discovery.OSGiServletBeanArchiveHandler;
import org.openntf.xsp.cdi.discovery.WeldBeanClassContributor;
import org.openntf.xsp.jakartaee.util.LibraryUtil;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.osgi.framework.wiring.BundleWiring;

import com.ibm.commons.util.StringUtil;
import com.ibm.designer.domino.napi.NotesAPIException;
import com.ibm.designer.domino.napi.NotesDatabase;
import com.ibm.designer.runtime.domino.adapter.ComponentModule;
import com.ibm.domino.xsp.module.nsf.NotesContext;
import com.ibm.xsp.application.ApplicationEx;

import jakarta.el.ELResolver;
import jakarta.el.ExpressionFactory;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.enterprise.inject.spi.Extension;

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
	 * The xsp.properties key used to determine an OSGi bundle to fully delegate CDI responsibilities
	 * to. When this is set, the NSF will use the same CDI container returned by
	 * {@link #getContainer(Bundle)}.
	 * @since 1.1.0
	 */
	public static final String PROP_CDIBUNDLE = CDILibrary.LIBRARY_ID + ".cdibundle"; //$NON-NLS-1$
	/**
	 * The xsp.properties key used to determine an OSGi bundle to use as a baseline for CDI
	 * beans for an NSF. When this is set, CDI will pull all classes and resources from the named
	 * OSGi bundle, but will use a separate CDI container for each NSF.
	 * @since 1.2.0
	 */
	public static final String PROP_CDIBUNDLEBASE = CDILibrary.LIBRARY_ID + ".cdibundlebase"; //$NON-NLS-1$
	
	/**
	 * Keeps track of the Application IDs associated with a DB replica ID, to allow for invalidating
	 * CDI containers when the app expires after being spawned by JAX-RS.
	 */
	private static final Map<String, String> REPLICAID_APPID_CACHE = new HashMap<>();
	
	/**
	 * Keeps locks for initializing containers by ID, to reduce problems from multiple calls to
	 * `getContainer` for the same location from stepping on each other.
	 */
	// Note: the use of a Map here still leaves small windows for multiple threads to enter
	//   the same init, and thus it would be preferable to find a better solution
	private static final Map<String, Object> CONTAINER_INIT_LOCKS = Collections.synchronizedMap(new HashMap<>());
	
	/**
	 * Gets or creates a {@link WeldContainer} instance for the provided Application.
	 * 
	 * @param application the active {@link ApplicationEx}
	 * @return an existing or new {@link CDI}
	 */
	@SuppressWarnings("unchecked")
	public static CDI<Object> getContainer(ApplicationEx application) {
		if(LibraryUtil.usesLibrary(CDILibrary.LIBRARY_ID, application)) {
			String bundleId = getApplicationCDIBundle(application);
			if(StringUtil.isNotEmpty(bundleId)) {
				Optional<Bundle> bundle = LibraryUtil.getBundle(bundleId);
				if(bundle.isPresent()) {
					return getContainer(bundle.get());
				}
			}
			
			// Look for the database so we can share the replica ID
			String id;
			try {
				id = NotesContext.getCurrent().getNotesDatabase().getReplicaID();
			} catch (NotesAPIException e) {
				throw new RuntimeException(e);
			}
			
			return withLock(id, () -> {
				WeldContainer instance = WeldContainer.instance(id);
				
				// Check the app ID to see if we have to invalidate it
				String existingMapping = REPLICAID_APPID_CACHE.get(id);
				if(existingMapping != null && !existingMapping.equals(application.getApplicationId())) {
					if(instance != null && instance.isRunning()) {
						// Then it's outdated - invalidate
						instance.shutdown();
					}
				}
				REPLICAID_APPID_CACHE.put(id, application.getApplicationId());
				
				if(instance == null || !instance.isRunning()) {
					Weld weld = constructWeld(id)
						.property(Weld.SCAN_CLASSPATH_ENTRIES_SYSTEM_PROPERTY, true);
	
					String baseBundleId = getApplicationCDIBundleBase(application);
					if(StringUtil.isNotEmpty(baseBundleId)) {
						Optional<Bundle> bundle = LibraryUtil.getBundle(baseBundleId);
						if(bundle.isPresent()) {
							weld.setResourceLoader(new BundleDependencyResourceLoader(bundle.get()));
						}
					} else {
						weld.setResourceLoader(new ModuleContextResourceLoader(NotesContext.getCurrent().getModule()));
					}
					
					instance = AccessController.doPrivileged((PrivilegedAction<WeldContainer>)() -> {
						for(Extension extension : (List<Extension>)application.findServices(Extension.class.getName())) {
							weld.addExtension(extension);
						}
						
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
						
						return weld.initialize();
					});
				}
				return instance;
			});
		} else {
			return null;
		}
	}
	
	/**
	 * Gets a {@link WeldContainer} instance for the provided Application, but does not
	 * create one if none has been initialized.
	 * 
	 * @param application the active {@link ApplicationEx}
	 * @return an existing {@link CDI}, or {@code null} if none has been initialized
	 * @since 2.5.0
	 */
	public static CDI<Object> getContainerUnchecked(ApplicationEx application) {
		if(LibraryUtil.usesLibrary(CDILibrary.LIBRARY_ID, application)) {
			String bundleId = getApplicationCDIBundle(application);
			if(StringUtil.isNotEmpty(bundleId)) {
				Optional<Bundle> bundle = LibraryUtil.getBundle(bundleId);
				if(bundle.isPresent()) {
					// For now, at least, it's fine to passively activate a Bundle container
					return getContainer(bundle.get());
				}
				
				// Look for the database so we can share the replica ID
				String id;
				try {
					id = NotesContext.getCurrent().getNotesDatabase().getReplicaID();
				} catch (NotesAPIException e) {
					throw new RuntimeException(e);
				}
				
				WeldContainer instance = WeldContainer.instance(id);
				if(instance == null || !instance.isRunning()) {
					return null;
				} else {
					return instance;
				}
			}
		}
		return null;
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
	@SuppressWarnings("nls")
	public static CDI<Object> getContainer(Bundle bundle) {
		String id = bundle.getSymbolicName();

		return withLock(id, () -> {
			WeldContainer instance = WeldContainer.instance(id);
			if(instance == null || !instance.isRunning()) {
				try {
					// Register a new one
					Weld weld = constructWeld(id)
						.setResourceLoader(new BundleDependencyResourceLoader(bundle));
					
					Set<String> bundleNames = new HashSet<>();
					Set<String> classNames = new HashSet<>();
					try {
						addBundleBeans(bundle, weld, bundleNames, classNames);
					} catch (BundleException e) {
						e.printStackTrace();
					}
					
					OSGiServletBeanArchiveHandler.PROCESSING_BUNDLE.set(bundle);
					OSGiServletBeanArchiveHandler.PROCESSING_ID.set(id);
					try {
						instance = weld.initialize();
					} finally {
						OSGiServletBeanArchiveHandler.PROCESSING_BUNDLE.set(null);
						OSGiServletBeanArchiveHandler.PROCESSING_ID.set(null);
					}
				} catch(IllegalStateException e) {
					System.err.println(MessageFormat.format("Encountered exception while initializing CDI container for {0}", bundle.getSymbolicName()));
					if(e.getMessage().contains("Class path entry does not exist or cannot be read")) { //$NON-NLS-1$
						String classpath = AccessController.doPrivileged((PrivilegedAction<String>)() -> System.getProperty("java.class.path")); //$NON-NLS-1$
						System.err.println(MessageFormat.format("Current class path: {0}", classpath));
					}
					e.printStackTrace();
					return null;
				}
			}
			return instance;
		});
	}
	
	private static void addBundleBeans(Bundle bundle, Weld weld, Set<String> bundleNames, Set<String> classNames) throws BundleException {
		String symbolicName = bundle.getSymbolicName();
		if(bundleNames.contains(symbolicName)) {
			return;
		}
		bundleNames.add(symbolicName);
		// Add classes from the bundle here
		DiscoveryUtil.findExportedClassNames(bundle, false)
			.filter(t -> !classNames.contains(t))
			.peek(classNames::add)
			.distinct()
			.map(t -> {
				try {
					return bundle.loadClass(t);
				} catch (ClassNotFoundException e) {
					return null;
				}
			})
			.filter(Objects::nonNull)
			.forEach(weld::addBeanClass);
		
		String requireBundle = bundle.getHeaders().get("Require-Bundle"); //$NON-NLS-1$
		if(StringUtil.isNotEmpty(requireBundle)) {
			ManifestElement[] elements = ManifestElement.parseHeader("Require-Bundle", requireBundle); //$NON-NLS-1$
			for(ManifestElement el : elements) {
				String bundleName = el.getValue();
				if(StringUtil.isNotEmpty(bundleName)) {
					Optional<Bundle> dependency = LibraryUtil.getBundle(bundleName);
					if(dependency.isPresent()) {
						addBundleBeans(dependency.get(), weld, bundleNames, classNames);
					}
				}
			}
		}
	}
	
	/**
	 * 
	 * @param database the database to open
	 * @return an existing or new {@link WeldContainer}, or {@code null} if the application does not use CDI
	 * @throws NotesAPIException if there is a problem reading the database
	 * @throws UncheckedIOException if there is a problem parsing the database configuration
	 */
	public static CDI<Object> getContainer(NotesDatabase database) throws NotesAPIException {
		if(LibraryUtil.usesLibrary(CDILibrary.LIBRARY_ID, database)) {
			String bundleId = getApplicationCDIBundle(database);
			if(StringUtil.isNotEmpty(bundleId)) {
				Optional<Bundle> bundle = LibraryUtil.getBundle(bundleId);
				if(bundle.isPresent()) {
					return getContainer(bundle.get());
				}
			}
			
			String id = database.getReplicaID();

			return withLock(id, () -> {
				WeldContainer instance = WeldContainer.instance(id);
				if(instance == null || !instance.isRunning()) {
					Weld weld = constructWeld(id)
						.property(Weld.SCAN_CLASSPATH_ENTRIES_SYSTEM_PROPERTY, true);
					String baseBundleId = getApplicationCDIBundleBase(database);
					Bundle bundle = null;
					if(StringUtil.isNotEmpty(baseBundleId)) {
						Optional<Bundle> optBundle = LibraryUtil.getBundle(baseBundleId);
						if(optBundle.isPresent()) {
							bundle = optBundle.get();
							weld = weld.setResourceLoader(new BundleDependencyResourceLoader(bundle));
							
							Set<String> bundleNames = new HashSet<>();
							Set<String> classNames = new HashSet<>();
							try {
								addBundleBeans(bundle, weld, bundleNames, classNames);
							} catch (BundleException e) {
								e.printStackTrace();
							}
						}
					}
	
					Weld fweld = weld;
					OSGiServletBeanArchiveHandler.PROCESSING_BUNDLE.set(bundle);
					OSGiServletBeanArchiveHandler.PROCESSING_ID.set(id);
					try {
						instance = AccessController.doPrivileged((PrivilegedAction<WeldContainer>)() -> {
							for(WeldBeanClassContributor service : LibraryUtil.findExtensions(WeldBeanClassContributor.class)) {
								Collection<Class<?>> beanClasses = service.getBeanClasses();
								if(beanClasses != null) {
									fweld.addBeanClasses(beanClasses.toArray(new Class<?>[beanClasses.size()]));
								}
								Collection<Extension> extensions = service.getExtensions();
								if(extensions != null) {
									fweld.addExtensions(extensions.toArray(new Extension[extensions.size()]));
								}
							}
							
							return fweld.initialize();
						});
					} finally {
						OSGiServletBeanArchiveHandler.PROCESSING_BUNDLE.set(null);
						OSGiServletBeanArchiveHandler.PROCESSING_ID.set(null);
					}
				}
				return instance;
			});
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
		return application.getProperty(PROP_CDIBUNDLE, null);
	}
	
	/**
	 * Returns the configured baseline OSGi bundle for CDI beans, if any.
	 * 
	 * @param application the application to check
	 * @return the name of the bundle to use as the baseline, or {@code null} if not specified
	 * @since 2.0.0
	 */
	public static String getApplicationCDIBundleBase(ApplicationEx application) {
		return application.getProperty(PROP_CDIBUNDLEBASE, null);
	}
	
	/**
	 * Returns the configured override bundle ID for the application, if any.
	 * 
	 * @param database the application to check
	 * @return the name of the bundle to bind the container to, or <code>null</code>
	 *   if not specified
	 * @since 1.2.0
	 * @throws UncheckedIOException if there is a problem reading the xsp.properties file in the module
	 * @throws NotesAPIException if there is a problem reading the xsp.properties file in the module
	 */
	public static String getApplicationCDIBundle(NotesDatabase database) throws NotesAPIException {
		Properties props = LibraryUtil.getXspProperties(database);
		return props.getProperty(PROP_CDIBUNDLE, null);
	}
	
	/**
	 * Returns the configured baseline OSGi bundle for CDI beans, if any.
	 * 
	 * @param database the application to check
	 * @return the name of the bundle to use as the baseline, or {@code null} if not specified
	 * @since 2.0.0
	 * @throws UncheckedIOException if there is a problem reading the xsp.properties file in the module
	 * @throws NotesAPIException if there is a problem reading the xsp.properties file in the module
	 */
	public static String getApplicationCDIBundleBase(NotesDatabase database) {
		Properties props = LibraryUtil.getXspProperties(database);
		return props.getProperty(PROP_CDIBUNDLEBASE, null);
	}

	public static BeanManagerImpl getBeanManager(ApplicationEx application) {
		CDI<Object> container = getContainer(application);
		if(container == null) {
			return null;
		}
		return getBeanManager(container);
	}
	
	/**
	 * Retrieves the active {@link BeanManagerImpl} instance for the provided {@link NotesDatabase}.
	 * 
	 * @param database the database to retrieve the bean manager for
	 * @return the database's {@link BeanManagerImpl} instance, or {@code null} if CDI is not enabled
	 *         for the database
	 * @throws NotesAPIException if there is a Notes API problem accessing the database
	 * @throws UncheckedIOException if there is a stream problem reading the database config
	 * @since 2.3.0
	 */
	public static BeanManagerImpl getBeanManager(NotesDatabase database) throws NotesAPIException {
		CDI<Object> container = getContainer(database);
		if(container == null) {
			return null;
		}
		return getBeanManager(container);
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
	
	private static <T> T withLock(String lockId, Supplier<T> supplier) {
		synchronized(CONTAINER_INIT_LOCKS.computeIfAbsent(lockId, key -> new Object())) {
			try {
				return supplier.get();
			} finally {
				CONTAINER_INIT_LOCKS.remove(lockId);
			}
		}
	}
	
	private static Weld constructWeld(String id) {
		return new Weld()
			.containerId(id)
			.addServices(new NSFProxyServices())
			.property(Weld.SCAN_CLASSPATH_ENTRIES_SYSTEM_PROPERTY, false)
			// Disable concurrent deployment to avoid Notes thread init trouble
			.property(ConfigurationKey.CONCURRENT_DEPLOYMENT.get(), false)
			.property(ConfigurationKey.EXECUTOR_THREAD_POOL_TYPE.get(), "SINGLE_THREAD") //$NON-NLS-1$
			.addExtension(new CDIScopesExtension())
			.addServices(new ExpressionLanguageSupport() {
				@Override
				public ExpressionFactory wrapExpressionFactory(ExpressionFactory expressionFactory) {
					return new WeldExpressionFactory(expressionFactory);
				}

				@Override
				public ELResolver createElResolver(BeanManagerImpl manager) {
					return new WeldELResolver(manager);
				}

				@Override
				public void cleanup() {
					
				}
			});
	}
	
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
							Optional<Bundle> dependency = LibraryUtil.getBundle(bundleName);
							if(dependency.isPresent()) {
								addBundleResources(name, dependency.get(), result, bundleNames);
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
