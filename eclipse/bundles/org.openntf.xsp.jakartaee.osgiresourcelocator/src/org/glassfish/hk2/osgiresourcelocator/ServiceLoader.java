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
package org.glassfish.hk2.osgiresourcelocator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;

import com.ibm.designer.runtime.domino.adapter.ComponentModule;
import com.ibm.domino.xsp.module.nsf.NotesContext;

/**
 * This class is intended to be API-compatible with the GlassFish
 * implementation, but provides extensions specific to Domino to allow
 * resolution of providers from an active ComponentModule.
 * 
 * @author Jesse Gallagher
 * @since 2.6.0
 */
public class ServiceLoader {
	private static final Logger LOGGER = Logger.getLogger(ServiceLoader.class.getName());
	
	private static final String SERVICE_LOCATION = "META-INF/services"; //$NON-NLS-1$
	private static final String COMMENT_PATTERN = "#"; //$NON-NLS-1$
	private static final Map<Bundle, Set<String>> OSGI_PROVIDERS = Collections.synchronizedMap(new HashMap<>());
	@SuppressWarnings("rawtypes")
	private static final Map<Class<?>, Iterable<Class>> OSGI_INSTANCES = Collections.synchronizedMap(new HashMap<>());
	
	public static void init(BundleContext bundleContext) {
		Arrays.stream(bundleContext.getBundles())
			.filter(bundle -> {
				int state = bundle.getState();
				return state == Bundle.ACTIVE || state == Bundle.RESOLVED;
			})
			.forEach(ServiceLoader::addBundle);
		
		bundleContext.addBundleListener(bundleEvent -> {
			Bundle bundle = bundleEvent.getBundle();
    		int type = bundleEvent.getType();
    		switch(type) {
    		case BundleEvent.INSTALLED:
    			addBundle(bundle);
    			break;
    		case BundleEvent.UNINSTALLED:
    			removeBundle(bundle);
    			break;
    		case BundleEvent.UPDATED:
    			removeBundle(bundle);
    			addBundle(bundle);
    			break;
    		}
    	});
	}
	
	@SuppressWarnings("unchecked")
	public static <T> Iterable<? extends T> lookupProviderInstances(Class<T> serviceClass) {
		@SuppressWarnings("rawtypes")
		Iterable<Class> classes = lookupProviderClasses(serviceClass);
		return (List<? extends T>)StreamSupport.stream(classes.spliterator(), false)
			.map(c -> {
				try {
					return c.newInstance();
				} catch (InstantiationException | IllegalAccessException e) {
					throw new RuntimeException(e);
				}
			})
			.collect(Collectors.toList());
	}
	
    @SuppressWarnings("rawtypes")
	public static <T> Iterable<Class> lookupProviderClasses(Class<T> serviceClass) {
		List<Class> result = new ArrayList<>();
		
		Iterable<Class> osgi = OSGI_INSTANCES.computeIfAbsent(serviceClass, ServiceLoader::resolveBundleServices);
		osgi.forEach(result::add);
		
		NotesContext nsfContext = NotesContext.getCurrentUnchecked();
		if(nsfContext != null) {
			Iterable<Class> nsf = resolveModuleServices(nsfContext.getModule(), serviceClass);
			nsf.forEach(result::add);
		}
		
		// TODO support OSGi NotesContext
		
		return result;
    }
    
    // *******************************************************************************
	// * Internal implementation methods
	// *******************************************************************************
    
    private static void addBundle(Bundle bundle) {
    	OSGI_PROVIDERS.put(bundle, identifyServices(bundle));
    }
    private static void removeBundle(Bundle bundle) {
    	OSGI_PROVIDERS.remove(bundle);
    	// This is rare on Domino, so just invalidate the whole cache
		OSGI_INSTANCES.clear();
    }
    
    private static Set<String> identifyServices(Bundle bundle) {
    	if(bundle.getEntry(SERVICE_LOCATION) == null) {
    		// Then no services at all
    		return Collections.emptySet();
    	}
    	
    	return Collections.list(bundle.getEntryPaths(SERVICE_LOCATION))
    		.stream()
    		.map(path -> path.substring(SERVICE_LOCATION.length()+1))
    		.collect(Collectors.toSet());
    }
    
    @SuppressWarnings("rawtypes")
	private static Iterable<Class> resolveBundleServices(Class<?> serviceClass) {
    	String serviceName = serviceClass.getName();
    	
		return OSGI_PROVIDERS.entrySet().stream()
			.filter(entry -> entry.getValue().contains(serviceClass.getName()))
			.map(Map.Entry::getKey)
			.filter(bundle -> {
				int state = bundle.getState();
				return state == Bundle.RESOLVED || state == Bundle.ACTIVE;
			})
			.flatMap(bundle -> {
				URL url = bundle.getResource(SERVICE_LOCATION + '/' + serviceName);
				if(url != null) {
					// Read the file and ignore blank or comment lines
					return parseServiceClassNames(url)
						.map(className -> {
							try {
								return bundle.loadClass(className);
							} catch(Exception e) {
								String msg = MessageFormat.format("Encountered exception loading class {0} from bundle {1}", serviceName, bundle.getSymbolicName());
								LOGGER.log(Level.SEVERE, msg, e);
								return null;
							}
						})
						.filter(Objects::nonNull);
				} else {
					return Stream.empty();
				}
			})
			.collect(Collectors.toList());
    }
    
    @SuppressWarnings("rawtypes")
	private static Iterable<Class> resolveModuleServices(ComponentModule module, Class<?> serviceClass) {
    	String serviceName = serviceClass.getName();
    	try {
    		URL url = module.getResource(SERVICE_LOCATION + '/' + serviceName);
    		if(url != null) {
	    		return parseServiceClassNames(url)
	    			.map(className -> {
	    				try {
							return Class.forName(className, true, module.getModuleClassLoader());
						} catch (Exception e) {
							String msg = MessageFormat.format("Encountered exception loading class {0} from module {1}", serviceName, module);
							LOGGER.log(Level.SEVERE, msg, e);
							return null;
						}
	    			})
	    			.filter(Objects::nonNull)
	    			.collect(Collectors.toList());
    		} else {
    			return Collections.emptyList();
    		}
    	} catch(IOException e) {
    		throw new UncheckedIOException(e);
    	}
    }
    
    private static Stream<String> parseServiceClassNames(URL url) {
    	List<String> result = new ArrayList<>();
    	try(
    		InputStream is = url.openStream();
    		Reader r = new InputStreamReader(is, StandardCharsets.UTF_8);
    		BufferedReader br = new BufferedReader(r);
    	) {
    		String line;
    		while((line = br.readLine()) != null) {
				if(!line.startsWith(COMMENT_PATTERN)) {
					String className = line.trim();
					if(!className.isEmpty()) {
						result.add(className);
					}
				}
			}
    	} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
    	return result.stream();
    }
}
