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
package org.openntf.xsp.jakartaee.module.impl;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.glassfish.hk2.osgiresourcelocator.ServiceLoader;
import org.glassfish.hk2.osgiresourcelocator.ServiceLoader.ClassPriorityComparator;
import org.openntf.xsp.jakartaee.module.ComponentModuleLocator;
import org.openntf.xsp.jakartaee.osgiresourceloader.ContextServiceLoader;
import org.openntf.xsp.jakartaee.util.LibraryUtil;

import com.ibm.designer.runtime.domino.adapter.ComponentModule;

/**
 * @since 3.4.0
 */
@SuppressWarnings("rawtypes")
public class ActiveModuleServiceLoader implements ContextServiceLoader {
	private static final Logger log = Logger.getLogger(ActiveModuleServiceLoader.class.getPackageName());
	
	@Override
	public boolean isActive() {
		return ComponentModuleLocator.getDefault().isPresent();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<Object> resolveModuleInstances(Class<?> serviceClass) {
		ComponentModule module = ComponentModuleLocator.getDefault()
			.map(ComponentModuleLocator::getActiveModule)
			.get();
		
		String key = getClass().getName() + "_classes_" + serviceClass.getName(); //$NON-NLS-1$
		Map<String, Object> attrs = module.getAttributes();
		return (List<Object>)LibraryUtil.computeIfAbsent(attrs, key, k ->
			findClasses(serviceClass, module).stream()
				.map(c -> {
					try {
						return c.getConstructor().newInstance();
					} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
						throw new RuntimeException(e);
					}
				})
				.toList()
		);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Class> resolveModuleServices(Class<?> serviceClass) {
		ComponentModule module = ComponentModuleLocator.getDefault()
			.map(ComponentModuleLocator::getActiveModule)
			.get();
		
		String key = getClass().getName() + "_classes_" + serviceClass.getName(); //$NON-NLS-1$
		Map<String, Object> attrs = module.getAttributes();
		return (List<Class>)LibraryUtil.computeIfAbsent(attrs, key, k -> findClasses(serviceClass, module));
	}

	private static List<Class> findClasses(Class<?> serviceClass, ComponentModule module) {
		try {
			List<Class> result = new ArrayList<>();
			
			Iterable<Class> osgi = ServiceLoader.lookupOsgiProviderClasses(serviceClass);
			osgi.forEach(result::add);
			
    		ClassLoader classLoader = module.getModuleClassLoader();
    		if(classLoader != null) {
	    		URL url = module.getResource(ServiceLoader.SERVICE_LOCATION + '/' + serviceClass.getName());
	    		if(url != null) {
	    			
		    		ServiceLoader.parseServiceClassNames(url)
		    			.map(className -> {
		    				try {
								return Class.forName(className, true, classLoader);
							} catch (Exception e) {
								String msg = MessageFormat.format("Encountered exception loading class {0} from module {1}", serviceClass.getName(), module);
								if(log.isLoggable(Level.SEVERE)) {
									log.log(Level.SEVERE, msg, e);
								}
								return null;
							}
		    			})
		    			.filter(Objects::nonNull)
		    			.forEach(result::add);
	    		}
    		}
    		
    		return result.stream()
				.sorted(ClassPriorityComparator.DESCENDING)
    			.collect(Collectors.toList());
    	} catch(IOException e) {
    		throw new UncheckedIOException(e);
    	}
	}
}
