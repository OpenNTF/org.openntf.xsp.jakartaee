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
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.openntf.xsp.jakartaee.osgiresourceloader.ContextServiceLoader;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;

import jakarta.annotation.Priority;

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

	public static final String SERVICE_LOCATION = "META-INF/services"; //$NON-NLS-1$
	private static final String COMMENT_PATTERN = "#"; //$NON-NLS-1$
	private static final Map<Bundle, Set<String>> OSGI_PROVIDERS = new HashMap<>();
	@SuppressWarnings("rawtypes")
	private static final Map<Class<?>, Iterable<Class>> OSGI_INSTANCES = Collections.synchronizedMap(new HashMap<>());

	public static void init(final BundleContext bundleContext) {
		Arrays.stream(bundleContext.getBundles())
			.filter(bundle -> {
				int state = bundle.getState();
				return state == Bundle.ACTIVE || state == Bundle.RESOLVED;
			})
			.filter(bundle -> bundle.getHeaders().get("Eclipse-SourceBundle") == null) //$NON-NLS-1$
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
	public static <T> Iterable<? extends T> lookupProviderInstances(final Class<T> serviceClass) {
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
	public static <T> Iterable<Class> lookupProviderClasses(final Class<T> serviceClass) {
		List<Class> result = new ArrayList<>();

		Iterable<Class> osgi = computeIfAbsent(OSGI_INSTANCES, serviceClass, ServiceLoader::resolveBundleServices);
		osgi.forEach(result::add);
		
		Activator.findExtensions(ContextServiceLoader.class).forEach(l -> {
			l.resolveModuleServices(serviceClass).forEach(result::add);
		});

		return result.stream()
			.sorted(ClassPriorityComparator.DESCENDING)
			.toList();
    }

    // *******************************************************************************
	// * Internal implementation methods
	// *******************************************************************************

    private static void addBundle(final Bundle bundle) {
    	OSGI_PROVIDERS.put(bundle, identifyServices(bundle));
    }
    private static void removeBundle(final Bundle bundle) {
    	OSGI_PROVIDERS.remove(bundle);
    	// This is rare on Domino, so just invalidate the whole cache
		OSGI_INSTANCES.clear();
    }

    private static Set<String> identifyServices(final Bundle bundle) {
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
	private static Iterable<Class> resolveBundleServices(final Class<?> serviceClass) {
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

    public static Stream<String> parseServiceClassNames(final URL url) {
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

	@SuppressWarnings("rawtypes")
    public enum ClassPriorityComparator implements Comparator<Class> {
    	ASCENDING(true), DESCENDING(false);

    	private final boolean ascending;

    	private ClassPriorityComparator(final boolean ascending) {
    		this.ascending = ascending;
    	}

		@Override
    	public int compare(final Class a, final Class b) {
    		int priorityA = Optional.ofNullable(((Class<?>)a).getAnnotation(Priority.class))
    			.map(Priority::value)
    			.orElse(ascending ? Integer.MAX_VALUE : 0);
    		int priorityB = Optional.ofNullable(((Class<?>)b).getAnnotation(Priority.class))
    			.map(Priority::value)
    			.orElse(ascending ? Integer.MAX_VALUE : 0);
    		if(ascending) {
    			return Integer.compare(priorityA, priorityB);
    		} else {
    			return Integer.compare(priorityB, priorityA);
    		}
    	}

    }

	private static <S, T> T computeIfAbsent(final Map<S, T> map, final S key, final Function<S, T> sup) {
		synchronized(map) {
			T result;
			if(!map.containsKey(key)) {
				result = sup.apply(key);
				map.put(key, result);
			} else {
				result = map.get(key);
			}
			return result;
		}
	}
}
