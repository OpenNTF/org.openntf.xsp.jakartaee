/**
 * Copyright © 2018 Jesse Gallagher
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
import java.util.List;

import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;

import org.jboss.weld.config.ConfigurationKey;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.resources.ClassLoaderResourceLoader;
import org.jboss.weld.util.ForwardingBeanManager;
import org.openntf.xsp.cdi.discovery.WeldBeanClassContributor;

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
	public static synchronized WeldContainer getContainer(ApplicationEx application) {
		WeldContainer instance = WeldContainer.instance(application.getApplicationId());
		if(instance == null) {
			Weld weld = new Weld()
				.containerId(application.getApplicationId())
//				.property(Weld.SCAN_CLASSPATH_ENTRIES_SYSTEM_PROPERTY, true)
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
	}

	public static BeanManagerImpl getBeanManager(ApplicationEx application) {
		WeldContainer container = getContainer(application);
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
