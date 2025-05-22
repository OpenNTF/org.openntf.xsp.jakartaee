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
package org.openntf.xsp.jakartaee.module;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.ibm.designer.runtime.domino.adapter.ComponentModule;

import org.glassfish.hk2.osgiresourcelocator.ServiceLoader;
import org.openntf.xsp.jakartaee.osgiresourceloader.ContextServiceLoader;

/**
 * @since 3.4.0
 */
public class ActiveModuleServiceLoader implements ContextServiceLoader {
	private static final Logger log = Logger.getLogger(ActiveModuleServiceLoader.class.getPackageName());

	@SuppressWarnings("rawtypes")
	@Override
	public Iterable<Class> resolveModuleServices(Class<?> serviceClass) {
		Optional<ComponentModule> optMod = ComponentModuleLocator.getDefault()
			.map(ComponentModuleLocator::getActiveModule);
		if(optMod.isPresent()) {
			String serviceName = serviceClass.getName();
	    	try {
	    		ClassLoader classLoader = optMod.get().getModuleClassLoader();
	    		if(classLoader != null) {
		    		URL url = optMod.get().getResource(ServiceLoader.SERVICE_LOCATION + '/' + serviceName);
		    		if(url != null) {
			    		return ServiceLoader.parseServiceClassNames(url)
			    			.map(className -> {
			    				try {
									return Class.forName(className, true, classLoader);
								} catch (Exception e) {
									String msg = MessageFormat.format("Encountered exception loading class {0} from module {1}", serviceName, optMod.get());
									if(log.isLoggable(Level.SEVERE)) {
										log.log(Level.SEVERE, msg, e);
									}
									return null;
								}
			    			})
			    			.filter(Objects::nonNull)
			    			.collect(Collectors.toList());
		    		}
	    		}
	    	} catch(IOException e) {
	    		throw new UncheckedIOException(e);
	    	}
		}
		return Collections.emptyList();
	}

}
