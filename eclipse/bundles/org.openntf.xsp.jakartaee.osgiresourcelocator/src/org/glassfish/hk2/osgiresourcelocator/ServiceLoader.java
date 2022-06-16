package org.glassfish.hk2.osgiresourcelocator;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;

/**
 * This class is intended to be API-compatible with the GlassFish
 * implementation, but provides extensions specific to Domino to allow
 * resolution of providers from an active ComponentModule.
 * 
 * @author Jesse Gallagher
 * @since 2.6.0
 */
public class ServiceLoader {
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
					List<Class<?>> result = new ArrayList<>();
					try(InputStream is = url.openStream(); Scanner scanner = new Scanner(is)) {
						while(scanner.hasNextLine()) {
							String line = scanner.nextLine();
							if(!line.startsWith(COMMENT_PATTERN)) {
								StringTokenizer tokenizer = new StringTokenizer(line);
								while(tokenizer.hasMoreTokens()) {
									String className = tokenizer.nextToken();
									try {
										Class<?> inst = bundle.loadClass(className);
										result.add(inst);
									} catch(Exception e) {
										String msg = MessageFormat.format("Encountered exception loading class {0} from bundle {1}", serviceName, bundle.getSymbolicName());
										new RuntimeException(msg, e).printStackTrace();
									}
								}
							}
						}
					} catch(IOException e) {
						throw new UncheckedIOException(e);
					}
					return result.stream();
				} else {
					return Stream.empty();
				}
			})
			.collect(Collectors.toList());
    }
}
