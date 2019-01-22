/**
 * Copyright © 2019 Jesse Gallagher
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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;

import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.CDI;
import javax.enterprise.inject.spi.Extension;

import org.eclipse.core.runtime.Platform;
import org.jboss.weld.config.ConfigurationKey;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.resources.ClassLoaderResourceLoader;
import org.jboss.weld.util.ForwardingBeanManager;
import org.openntf.xsp.cdi.CDILibrary;
import org.openntf.xsp.cdi.discovery.WeldBeanClassContributor;
import org.openntf.xsp.jakartaee.LibraryUtil;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;

import com.ibm.commons.util.StringUtil;
import com.ibm.designer.runtime.domino.adapter.ComponentModule;
import com.ibm.domino.xsp.module.nsf.NotesContext;
import com.ibm.xsp.application.ApplicationEx;

/**
 * Utility methods for working with Weld containers based on a given XPages application.
 * 
 * @author Jesse Gallagher
 * @since 1.0.0
 */
public enum ContainerUtil {
	;

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
	 * Returns the configured override bundle ID for the application, if any.
	 * 
	 * @param application the application to check
	 * @return the name of the bundle to bind the container to, or <code>null</code>
	 *   if not specified
	 * @since 1.1.0
	 */
	public static String getApplicationCDIBundle(ApplicationEx application) {
		return application.getProperty(CDILibrary.LIBRARY_ID + ".cdibundle", null);
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
	@SuppressWarnings("unchecked")
	public static synchronized CDI<Object> getContainer(Bundle bundle) {
		@SuppressWarnings("rawtypes")
		ServiceReference<CDI> ref = bundle.getBundleContext().getServiceReference(CDI.class);
		if(ref != null) {
			return (CDI<Object>)bundle.getBundleContext().getService(ref);
		} else {
			// Register a new one
			Weld weld = new Weld()
				.containerId(bundle.getSymbolicName())
				.property(Weld.SCAN_CLASSPATH_ENTRIES_SYSTEM_PROPERTY, true)
				// Disable concurrent deployment to avoid Notes thread init trouble
				.property(ConfigurationKey.CONCURRENT_DEPLOYMENT.get(), false);
			@SuppressWarnings("rawtypes")
			ServiceRegistration<CDI> reg = bundle.getBundleContext().registerService(CDI.class, weld.initialize(), new Hashtable<>());
			bundle.getBundleContext().addBundleListener(e -> {
				if(e.getType() == BundleEvent.STOPPING) {
					@SuppressWarnings("rawtypes")
					CDI cdi = bundle.getBundleContext().getService(reg.getReference());
					if(cdi instanceof WeldContainer) {
						try(WeldContainer c = (WeldContainer)cdi) {
							c.shutdown();
						}
					}
				}
			});
			return bundle.getBundleContext().getService(reg.getReference());
		}
	}

	public static BeanManagerImpl getBeanManager(ApplicationEx application) {
		CDI<Object> container = CDI.current();
		BeanManager manager = container.getBeanManager();
		if(manager instanceof BeanManagerImpl) {
			return (BeanManagerImpl)manager;
		} else if(manager instanceof ForwardingBeanManager) {
			return (BeanManagerImpl) ((ForwardingBeanManager)manager).delegate();
		} else {
			throw new IllegalStateException("Cannot find BeanManagerImpl in " + manager); //$NON-NLS-1$
		}
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
}
