/**
 * Copyright (c) 2018-2024 Contributors to the XPages Jakarta EE Support Project
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
package org.openntf.xsp.jakarta.cdi.util;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ibm.commons.util.StringUtil;
import com.ibm.designer.runtime.domino.adapter.ComponentModule;
import com.ibm.domino.xsp.adapter.osgi.AbstractOSGIModule;

import org.eclipse.core.runtime.Platform;
import org.eclipse.osgi.util.ManifestElement;
import org.jboss.weld.config.ConfigurationKey;
import org.jboss.weld.environment.deployment.discovery.jandex.Jandex;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.module.ExpressionLanguageSupport;
import org.jboss.weld.module.web.el.WeldELResolver;
import org.jboss.weld.module.web.el.WeldExpressionFactory;
import org.jboss.weld.resources.ClassLoaderResourceLoader;
import org.jboss.weld.resources.spi.ResourceLoader;
import org.jboss.weld.resources.spi.ResourceLoadingException;
import org.jboss.weld.util.ForwardingBeanManager;
import org.openntf.xsp.jakarta.cdi.context.CDIScopesExtension;
import org.openntf.xsp.jakarta.cdi.discovery.CDIClassContributor;
import org.openntf.xsp.jakartaee.util.LibraryUtil;
import org.openntf.xsp.jakartaee.util.ModuleUtil;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.osgi.framework.wiring.BundleWiring;

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
	public static final String PROP_CDIBUNDLE = "org.openntf.xsp.jakarta.cdi.cdibundle"; //$NON-NLS-1$
	/**
	 * The xsp.properties key used to determine an OSGi bundle to use as a baseline for CDI
	 * beans for an NSF. When this is set, CDI will pull all classes and resources from the named
	 * OSGi bundle, but will use a separate CDI container for each NSF.
	 * @since 1.2.0
	 */
	public static final String PROP_CDIBUNDLEBASE = "org.openntf.xsp.jakarta.cdi.cdibundlebase"; //$NON-NLS-1$

	/**
	 * Keeps track of CDI container IDs mapped to module refresh times. This allows for calls to
	 * check for modules that were fully refreshed outside our lifecycle and invalidate/recreate
	 * them.
	 * @since 2.13.0
	 */
	private static final Map<String, Long> ID_REFRESH_CACHE = new ConcurrentHashMap<>();

	/**
	 * Keeps locks for initializing containers by ID, to reduce problems from multiple calls to
	 * `getContainer` for the same location from stepping on each other.
	 */
	// Note: the use of a Map here still leaves small windows for multiple threads to enter
	//   the same init, and thus it would be preferable to find a better solution
	private static final Map<String, Object> CONTAINER_INIT_LOCKS = new ConcurrentHashMap<>();

	private static final String ATTR_CONTEXTCONTAINER = "org.openntf.xsp.jakarta.cdi.cdicontainer"; //$NON-NLS-1$

	private static final Logger log = Logger.getLogger(ContainerUtil.class.getPackage().getName());

	/**
	 * Retrieves or creates a CDI container specific to the provided OSGi bundle.
	 *
	 * <p>The container is registered as an OSGi service.</p>
	 *
	 * @param bundle the bundle to find the container for
	 * @return the existing container or a newly-registered one if one did not exist
	 * @since 1.1.0
	 */
	@SuppressWarnings({ "nls", "unchecked" })
	public static CDI<Object> getContainer(final Bundle bundle) {
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
						addBundleBeans(bundle, weld, bundleNames, classNames, true);
					} catch (BundleException e) {
						if(log.isLoggable(Level.WARNING)) {
							log.log(Level.WARNING, "Encountered exception loading bundle beans", e);
						}
					}

					for(CDIClassContributor service : LibraryUtil.findExtensions(CDIClassContributor.class)) {
						Collection<Class<?>> beanClasses = service.getBeanClasses();
						if(beanClasses != null) {
							weld.addBeanClasses(beanClasses.toArray(new Class<?>[beanClasses.size()]));
						}
						Collection<Extension> extensions = service.getExtensions();
						if(extensions != null) {
							weld.addExtensions(extensions.toArray(new Extension[extensions.size()]));
						}
						Collection<Class<? extends Extension>> extensionClasses = service.getExtensionClasses();
						if(extensionClasses != null) {
							extensionClasses.forEach(weld::addExtensions);
						}
					}
					instance = weld.initialize();
				} catch(IllegalStateException e) {
					if(log.isLoggable(Level.SEVERE)) {
						log.severe(MessageFormat.format("Encountered exception while initializing CDI container for {0}", bundle.getSymbolicName()));
						if(e.getMessage().contains("Class path entry does not exist or cannot be read")) { //$NON-NLS-1$
							String classpath = LibraryUtil.getSystemProperty("java.class.path"); //$NON-NLS-1$
							log.severe(MessageFormat.format("Current class path: {0}", classpath));
						}
						log.log(Level.SEVERE, "Original exception", e);
					}
					return null;
				} catch(Throwable t) {
					if(log.isLoggable(Level.SEVERE)) {
						log.log(Level.SEVERE, MessageFormat.format("Encountered exception while initializing CDI container for {0}", bundle.getSymbolicName()), t);
					}
					throw t;
				}
			}
			return instance;
		});
	}

	/**
	 * Retrieves the CDI container for the provided ComponentModule, if it has been
	 * initialized.
	 *
	 * @param servletContext the {@link ComponentModule} instance to query
	 * @return the module's container, or {@code null} if it has not been initialized
	 * @since 2.7.0
	 */
	@SuppressWarnings("unchecked")
	public static CDI<Object> getContainerUnchecked(final ComponentModule module) {
		return (CDI<Object>)module.getAttributes().get(ATTR_CONTEXTCONTAINER);
	}

	private static void addBundleBeans(final Bundle bundle, final Weld weld, final Set<String> bundleNames, final Set<String> classNames, final boolean nonExported) throws BundleException {
		String symbolicName = bundle.getSymbolicName();
		if(bundleNames.contains(symbolicName)) {
			return;
		}
		bundleNames.add(symbolicName);
		// Add classes from the bundle here
		DiscoveryUtil.findBeanClasses(bundle, nonExported)
			.filter(t -> !classNames.contains(t.getName()))
			.peek(t -> classNames.add(t.getName()))
			.distinct()
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
						addBundleBeans(dependency.get(), weld, bundleNames, classNames, false);
					}
				}
			}
		}
	}

	/**
	 *
	 * @param module the module to retrieve a container for
	 * @return an existing or new {@link CDI}, or {@code null} if the application does not use CDI
	 * @since 2.13.0
	 */
	@SuppressWarnings("unchecked")
	public static CDI<Object> getContainer(final ComponentModule module) {
		// OSGi Servlets use their containing bundle, and we have to assume
		//   that it's from the current thread
		if(module instanceof AbstractOSGIModule) {

			ClassLoader cl = Thread.currentThread().getContextClassLoader();
			Optional<Bundle> bundle = DiscoveryUtil.getBundleForClassLoader(cl);
			if(bundle.isPresent()) {
				return getContainer(bundle.get());
			}
		}

		long refresh = module.getLastRefresh();

		Map<String, Object> attributes = module.getAttributes();
		if(attributes != null) {
			WeldContainer existing = (WeldContainer)attributes.get(ATTR_CONTEXTCONTAINER);
			if(existing != null && existing.isRunning()) {
				Long lastRefresh = ID_REFRESH_CACHE.get(existing.getId());
				if(lastRefresh != null && lastRefresh < refresh || module.isExpired(System.currentTimeMillis())) {
					existing.close();
				} else {
					return existing;
				}
			}
		}


		if(module instanceof AbstractOSGIModule || LibraryUtil.usesLibrary(LibraryUtil.LIBRARY_CORE, module)) {
			String bundleId = getApplicationCDIBundle(module);
			if(StringUtil.isNotEmpty(bundleId)) {
				Optional<Bundle> bundle = LibraryUtil.getBundle(bundleId);
				if(bundle.isPresent()) {
					return getContainer(bundle.get());
				}
			}

			String id = ModuleUtil.getModuleId(module);

			return withLock(id, () -> {
				WeldContainer instance = WeldContainer.instance(id);

				// If the instance exists, we may have to invalidate it from an
				//   app refresh
				if(instance != null && instance.isRunning()) {
					Long lastRefresh = ID_REFRESH_CACHE.get(id);
					if(lastRefresh != null && lastRefresh < refresh || module.isExpired(System.currentTimeMillis())) {
						instance.close();
					}
				}

				if(instance == null || !instance.isRunning()) {
					Weld weld = constructWeld(id)
						.property(Weld.SCAN_CLASSPATH_ENTRIES_SYSTEM_PROPERTY, true);
					String baseBundleId = getApplicationCDIBundleBase(module);
					Bundle bundle = null;
					if(StringUtil.isNotEmpty(baseBundleId)) {
						Optional<Bundle> optBundle = LibraryUtil.getBundle(baseBundleId);
						if(optBundle.isPresent()) {
							bundle = optBundle.get();
							weld = weld.setResourceLoader(new BundleDependencyResourceLoader(bundle));

							Set<String> bundleNames = new HashSet<>();
							Set<String> classNames = new HashSet<>();
							try {
								addBundleBeans(bundle, weld, bundleNames, classNames, false);
							} catch (BundleException e) {
								if(log.isLoggable(Level.WARNING)) {
									log.log(Level.WARNING, "Encountered exception loading bundle beans", e);
								}
							}
						}
					} else {
						// OSGi Servlets don't provide this ClassLoader
						if(module.getModuleClassLoader() != null) {
							weld.setResourceLoader(new ModuleContextResourceLoader(module));
						} else {
							weld.setResourceLoader(new ClassLoaderResourceLoader(Thread.currentThread().getContextClassLoader()));
						}
					}

					Weld fweld = weld;
					instance = AccessController.doPrivileged((PrivilegedAction<WeldContainer>)() -> {
						// Special case for using an NSFComponentModule when there's no available NotesContext,
						//   such as when the first request in is a service

						for(Extension extension : LibraryUtil.findExtensions(Extension.class, module)) {
							fweld.addExtension(extension);
						}

						for(CDIClassContributor service : LibraryUtil.findExtensions(CDIClassContributor.class, module)) {
							Collection<Class<?>> beanClasses = service.getBeanClasses();
							if(beanClasses != null) {
								fweld.addBeanClasses(beanClasses.toArray(new Class<?>[beanClasses.size()]));
							}
							Collection<Extension> exts = service.getExtensions();
							if(exts != null) {
								fweld.addExtensions(exts.toArray(new Extension[exts.size()]));
							}
							Collection<Class<? extends Extension>> extensionClasses = service.getExtensionClasses();
							if(extensionClasses != null) {
								extensionClasses.forEach(fweld::addExtensions);
							}
						}

						return fweld.initialize();
					});

					// Also set it in the ServletContext for other use
					if(attributes != null) {
						attributes.put(ATTR_CONTEXTCONTAINER, instance);
					}
					ID_REFRESH_CACHE.put(id, refresh);
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
	 * @param module the module to check
	 * @return the name of the bundle to bind the container to, or <code>null</code>
	 *   if not specified
	 * @since 1.13.0
	 */
	public static String getApplicationCDIBundle(final ComponentModule module) {
		return LibraryUtil.getXspProperties(module).getProperty(PROP_CDIBUNDLE, null);
	}

	/**
	 * Returns the configured baseline OSGi bundle for CDI beans, if any.
	 *
	 * @param module the module to check
	 * @return the name of the bundle to use as the baseline, or {@code null} if not specified
	 * @since 2.13.0
	 */
	public static String getApplicationCDIBundleBase(final ComponentModule module) {
		return LibraryUtil.getXspProperties(module).getProperty(PROP_CDIBUNDLEBASE, null);
	}

	/**
	 * @since 1.2.0
	 */
	public static BeanManagerImpl getBeanManager(final CDI<Object> container) {
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
	public static void setThreadContextDatabasePath(final String databasePath) {
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

	private static <T> T withLock(final String lockId, final Supplier<T> supplier) {
		synchronized(CONTAINER_INIT_LOCKS.computeIfAbsent(lockId, key -> new Object())) {
			try {
				return supplier.get();
			} finally {
				CONTAINER_INIT_LOCKS.remove(lockId);
			}
		}
	}

	private static Weld constructWeld(final String id) {
		return new Weld()
			.containerId(id)
			.addServices(new NSFProxyServices())
			.property(Weld.SCAN_CLASSPATH_ENTRIES_SYSTEM_PROPERTY, false)
			// Disable concurrent deployment to avoid Notes thread init trouble
			.property(ConfigurationKey.CONCURRENT_DEPLOYMENT.get(), false)
			.property(ConfigurationKey.EXECUTOR_THREAD_POOL_TYPE.get(), "SINGLE_THREAD") //$NON-NLS-1$
			// Disable Jandex, as it causes problems in D14
			.property(Jandex.DISABLE_JANDEX_DISCOVERY_STRATEGY, true)
			.addExtension(new CDIScopesExtension())
			.addServices(new ExpressionLanguageSupport() {
				@Override
				public ExpressionFactory wrapExpressionFactory(final ExpressionFactory expressionFactory) {
					return new WeldExpressionFactory(expressionFactory);
				}

				@Override
				public ELResolver createElResolver(final BeanManagerImpl manager) {
					return new WeldELResolver(manager);
				}

				@Override
				public void cleanup() {

				}
			});
	}

	private static class ModuleContextResourceLoader extends ClassLoaderResourceLoader {
		private final ComponentModule module;

		public ModuleContextResourceLoader(final ComponentModule module) {
			super(module.getModuleClassLoader());

			this.module = module;
		}

		@Override
		public Collection<URL> getResources(final String name) {
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

		public BundleDependencyResourceLoader(final Bundle bundle) {
			this.bundle = bundle;
		}

		@Override
		public Class<?> classForName(final String name) {
			try {
				return Class.forName(name, true, Thread.currentThread().getContextClassLoader());
			} catch (ClassNotFoundException | LinkageError e) {
				// Couldn't find it here
			}
			try {
				return Class.forName(name, true, bundle.adapt(BundleWiring.class).getClassLoader());
			} catch (ClassNotFoundException | LinkageError e) {
				throw new ResourceLoadingException("Error loading class " + name, e);
			}
		}

		@Override
		public URL getResource(final String name) {
			return bundle.adapt(BundleWiring.class).getClassLoader().getResource(name);
		}

		@Override
		public Collection<URL> getResources(final String name) {
			Collection<URL> result = new HashSet<>();
			Collection<String> bundleNames = new HashSet<>();
			addBundleResources(name, bundle, result, bundleNames);
			return result;
		}

		@Override
		public void cleanup() {

		}

		private void addBundleResources(final String name, final Bundle bundle, final Collection<URL> result, final Collection<String> bundleNames) {
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
