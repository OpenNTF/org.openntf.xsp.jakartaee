/*
 * Copyright (c) 2018-2026 Contributors to the XPages Jakarta EE Support Project
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
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.glassfish.hk2.osgiresourcelocator.ServiceLoader;
import org.openntf.xsp.jakartaee.module.ComponentModuleLocator;
import org.openntf.xsp.jakartaee.osgiresourceloader.ContextServiceLoader;
import org.openntf.xsp.jakartaee.util.LibraryUtil;

import com.ibm.designer.runtime.domino.adapter.ComponentModule;

/**
 * @since 3.4.0
 */
public class ActiveModuleServiceLoader implements ContextServiceLoader {
	private static final Logger log = System.getLogger(ActiveModuleServiceLoader.class.getPackageName());

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Iterable<Class> resolveModuleServices(Class<?> serviceClass) {
		Optional<ComponentModule> optMod = ComponentModuleLocator.getDefault()
			.map(ComponentModuleLocator::getActiveModule);
		if(optMod.isPresent()) {
			String serviceName = serviceClass.getName();
			var cacheKey = serviceName + "_services"; //$NON-NLS-1$
			var cache = optMod.get().getAttributes();
			var names = (List<String>)LibraryUtil.computeIfAbsent(cache, cacheKey, k -> {
				try {
				URL url = optMod.get().getResource(ServiceLoader.SERVICE_LOCATION + '/' + serviceName);
		    		if(url != null) {
			    		return ServiceLoader.parseServiceClassNames(url).toList();
		    		} else {
		    			return List.of();
		    		}
				} catch(IOException e) {
					return List.of();
				}
			});
    		ClassLoader classLoader = optMod.get().getModuleClassLoader();
    		if(classLoader != null) {
    			return names.stream()
					.map(className -> {
	    				try {
							return (Class)Class.forName(className, true, classLoader);
						} catch (Exception e) {
							log.log(Level.ERROR, () -> MessageFormat.format("Encountered exception loading class {0} from module {1}", serviceName, optMod.get()), e);
							return null;
						}
	    			})
	    			.filter(Objects::nonNull)
	    			.toList();
    		} else {
    			return List.of();
    		}
		}
		return Collections.emptyList();
	}

}
