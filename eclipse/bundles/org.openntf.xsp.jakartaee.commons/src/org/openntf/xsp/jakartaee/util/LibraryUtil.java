/**
 * Copyright (c) 2018-2023 Contributors to the XPages Jakarta EE Support Project
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
package org.openntf.xsp.jakartaee.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.core.runtime.Platform;
import org.openntf.xsp.jakartaee.discovery.ApplicationPropertyLocator;
import org.openntf.xsp.jakartaee.discovery.ComponentEnabledLocator;
import org.osgi.framework.Bundle;

import lotus.domino.Database;
import lotus.domino.NotesException;
import lotus.domino.Session;
import com.ibm.commons.extension.ExtensionManager;
import com.ibm.designer.domino.napi.NotesAPIException;
import com.ibm.designer.domino.napi.NotesDatabase;
import com.ibm.designer.domino.napi.NotesNote;
import com.ibm.designer.domino.napi.design.FileAccess;
import com.ibm.designer.runtime.domino.adapter.ComponentModule;
import com.ibm.xsp.application.ApplicationEx;

import jakarta.annotation.Priority;

/**
 * Utility methods for working with XSP Libraries.
 * 
 * @author Jesse Gallagher
 * @since 1.0.0
 */
public enum LibraryUtil {
	;
	
	private static final Map<String, Long> NSF_MOD = new HashMap<>();
	private static final Map<String, Properties> NSF_PROPS = new ConcurrentHashMap<>();
	
	/**
	 * Store bundles by symbolic name to speed up lookups, since we don't realistically have to worry
	 * about dynamically loaded/unloaded bundles.
	 * @since 2.4.0
	 */
	private static final Map<String, Bundle> BUNDLE_CACHE = Collections.synchronizedMap(new HashMap<>());
	/**
	 * Store looked up extensions by class for the lifetime of the JVM.
	 * @since 2.4.0
	 */
	private static final Map<Class<?>, List<?>> EXTENSION_CACHE = Collections.synchronizedMap(new HashMap<>());
	
	/**
	 * Property used to house the time that the xsp.properties resource in a ComponentModule
	 * was last read.
	 * @since 2.8.0
	 */
	public static final String PROP_XSPPROPSREAD = LibraryUtil.class.getName() + "_xsppropsread"; //$NON-NLS-1$
	/**
	 * Property used to house the parsed xsp.properties value for a ComponentModule
	 * @since 2.8.0
	 */
	public static final String PROP_XSPPROPS = LibraryUtil.class.getName() + "_xspprops"; //$NON-NLS-1$
	
	/**
	 * Attempts to determine whether the given XPages Library is active for the
	 * current application.
	 * 
	 * @param libraryId the library ID to check
	 * @return {@code true} if the library is active; {@code false} if it is
	 *         not or if the context application cannot be identified
	 * @since 2.3.0
	 */
	public static boolean isLibraryActive(String libraryId) {
		return findExtensions(ComponentEnabledLocator.class)
			.stream()
			.sorted(DescendingPriorityComparator.INSTANCE)
			.filter(ComponentEnabledLocator::isActive)
			.anyMatch(locator -> locator.isComponentEnabled(libraryId));
	}
	
	/**
	 * Attempts to determine whether all of the given XPages Libraries are
	 * active for the current application.
	 * 
	 * @param libraryIds the library IDs to check
	 * @return {@code true} if all libraries are active; {@code false} if any
	 *         of them are not or if the context application cannot be
	 *         identified
	 * @since 2.11.0
	 */
	public static boolean isLibraryActive(String... libraryIds) {
		return Stream.of(libraryIds).allMatch(LibraryUtil::isLibraryActive);
	}
	
	/**
	 * Attempts to retrieve the given property from the current application.
	 * 
	 * @param prop the property to load
	 * @param defaultValue a default value to return if the property is
	 *                     not available
	 * @return the property value if the property is set; {@code defaultValue}
	 *         if it is not or if the context application cannot be identified
	 * @since 2.3.0
	 */
	public static String getApplicationProperty(String prop, String defaultValue) {
		return findExtensions(ApplicationPropertyLocator.class)
			.stream()
			.sorted(DescendingPriorityComparator.INSTANCE)
			.filter(ApplicationPropertyLocator::isActive)
			.findFirst()
			.map(locator -> locator.getApplicationProperty(prop, defaultValue))
			.orElse(defaultValue);
	}
	
	/**
	 * Determines whether the provided {@link ApplicationEx} uses the provided library
	 * ID.
	 * 
	 * @param libraryId the library ID to look for
	 * @param app the application instance to check
	 * @return whether the library is loaded by the application
	 */
	public static boolean usesLibrary(String libraryId, ApplicationEx app) {
		if(app == null) {
			return false;
		}
		String prop = app.getProperty("xsp.library.depends", ""); //$NON-NLS-1$ //$NON-NLS-2$
		return Arrays.asList(prop.split(",")).contains(libraryId); //$NON-NLS-1$
	}

	/**
	 * Determines whether the provided {@link ComponentModule} uses the provided library
	 * ID.
	 * 
	 * @param libraryId the library ID to look for
	 * @param module the component module to check
	 * @return whether the library is loaded by the application
	 * @throws UncheckedIOException if there is a problem reading the xsp.properties file in the module
	 * @since 1.2.0
	 */
	public static boolean usesLibrary(String libraryId, ComponentModule module) {
		if(module == null) {
			return false;
		}
		Properties props = getXspProperties(module);
		String prop = props.getProperty("xsp.library.depends", ""); //$NON-NLS-1$ //$NON-NLS-2$
		return Arrays.asList(prop.split(",")).contains(libraryId); //$NON-NLS-1$
	}
	
	/**
	 * Determines whether the provided {@link NotesDatabase} uses the provided library
	 * ID.
	 * 
	 * @param libraryId the library ID to look for
	 * @param module the database to check
	 * @return whether the library is loaded by the application
	 * @throws UncheckedIOException if there is a problem reading the xsp.properties file in the module
	 * @throws NotesAPIException if there is a problem reading the xsp.properties file in the module
	 * @since 1.2.0
	 */
	public static boolean usesLibrary(String libraryId, NotesDatabase database) throws NotesAPIException {
		if(database == null) {
			return false;
		}
		Properties props = getXspProperties(database);
		String prop = props.getProperty("xsp.library.depends", ""); //$NON-NLS-1$ //$NON-NLS-2$
		return Arrays.asList(prop.split(",")).contains(libraryId); //$NON-NLS-1$
	}
	
	/**
	 * Reads the xsp.properties file for the provided database. 
	 * 
	 * @param database the database to read
	 * @return a {@link Properties} file with the database's XSP properties loaded, if available
	 * @throws RuntimeException if there is a problem reading the xsp.properties file in the module
	 * @since 1.2.0
	 */
	public static Properties getXspProperties(NotesDatabase database) {
		String dbReplicaId;
		try {
			dbReplicaId = database.getReplicaID();
		} catch (NotesAPIException e) {
			throw new RuntimeException(e);
		}
		
		return NSF_PROPS.compute(dbReplicaId, (replicaId, existing) -> {
			try {
				// Check to see if we need a cache refresh
				if(NSF_MOD.containsKey(replicaId)) {
					long lastMod = database.getLastNonDataModificationDate();
					if(lastMod < NSF_MOD.get(replicaId)) {
						// Then we're good to use what's there
						return existing;
					}
				}
				NSF_MOD.put(replicaId, System.currentTimeMillis());
				
				// Read xsp.properties from the NSF
				Properties props = new Properties();
				NotesNote xspProperties = FileAccess.getFileByPath(database, "/WEB-INF/xsp.properties"); //$NON-NLS-1$
				if(xspProperties != null) {
					try(InputStream is = FileAccess.readFileContentAsInputStream(xspProperties)) {
						props.load(is);
					} finally {
						xspProperties.recycle();
					}
				}
				return props;
			} catch(IOException e) {
				throw new UncheckedIOException(e);
			} catch(NotesAPIException e) {
				throw new RuntimeException(e);
			}
		});
	}
	
	/**
	 * Loads the xsp.properties file content for the provided module.
	 * 
	 * @param module the {@link ComponentModule} instance to open
	 * @return the loaded XSP properties
	 * @since 2.3.0
	 */
	public static Properties getXspProperties(ComponentModule module) {
		
		Map<String, Object> attributes = module.getAttributes();
		synchronized(attributes) {
			long lastMod = module.getLastRefresh();
			boolean needsRebuild = false;
			Properties props = (Properties)attributes.get(PROP_XSPPROPS);
			if(props != null) {
				Long updated = (Long)attributes.get(PROP_XSPPROPSREAD);
				if(lastMod > updated) {
					needsRebuild = true;
				}
			} else {
				needsRebuild = true;
			}
			
			if(needsRebuild) {
				props = new Properties();
				try(InputStream is = module.getResourceAsStream("/WEB-INF/xsp.properties")) { //$NON-NLS-1$
					if(is != null) {
						props.load(is);
					}
				} catch (IOException e) {
					throw new UncheckedIOException(e);
				}
				attributes.put(PROP_XSPPROPS, props);
				attributes.put(PROP_XSPPROPSREAD, lastMod);
			}
			
			return props;
		}
		
	}
	
	/**
	 * Finds extensions for the given class using the IBM Commons extension mechanism, storing instances
	 * in a global per-extension-class cache.
	 * 
	 * <p>This method assumes that the extension point name is the same as the qualified class name.</p>
	 * 
	 * @param <T> the class of extension to find
	 * @param extensionClass the class object representing the extension point
	 * @return a {@link List} of service objects for the class
	 */
	@SuppressWarnings("unchecked")
	public static <T> List<T> findExtensions(Class<T> extensionClass) {
		return (List<T>)EXTENSION_CACHE.computeIfAbsent(extensionClass, LibraryUtil::findExtensionsUncached);
	}
	
	/**
	 * Finds extensions for the given class using the IBM Commons extension mechanism, creating new instances
	 * of each found class to return.
	 * 
	 * <p>This method assumes that the extension point name is the same as the qualified class name.</p>
	 * 
	 * @param <T> the class of extension to find
	 * @param extensionClass the class object representing the extension point
	 * @return a {@link List} of service objects for the class
	 * @since 2.9.0
	 */
	public static <T> List<T> findExtensionsUncached(Class<T> extensionClass) {
		return AccessController.doPrivileged((PrivilegedAction<List<T>>)() ->
			ExtensionManager.findServices(null, extensionClass.getClassLoader(), extensionClass.getName(), extensionClass)
		);
	}
	
	/**
	 * Finds extension for the given class using the IBM Commons extension mechanism, sorted based
	 * on the {@link Priority} annotation.
	 * s the qualified class name.</p>
	 * 
	 * @param <T> the class of extension to find
	 * @param extensionClass the class object representing the extension point
	 * @param ascending {@code true} if the value of the {@link Priority} annotation should be sorted
	 *                  in ascending order; {@code false} otherwise
	 * @return a {@link List} of service objects for the class
	 * @since 2.7.0
	 */
	public static <T> List<T> findExtensionsSorted(Class<T> extensionClass, boolean ascending) {
		return findExtensions(extensionClass)
			.stream()
			.filter(Objects::nonNull)
			.sorted((a, b) -> {
				int priorityA = Optional.ofNullable(a.getClass().getAnnotation(Priority.class))
					.map(Priority::value)
					.orElse(ascending ? Integer.MAX_VALUE : 0);
				int priorityB = Optional.ofNullable(b.getClass().getAnnotation(Priority.class))
					.map(Priority::value)
					.orElse(ascending ? Integer.MAX_VALUE : 0);
				if(ascending) {
					return Integer.compare(priorityA, priorityB);
				} else {
					return Integer.compare(priorityB, priorityA);
				}
			})
			.collect(Collectors.toList());
	}
	
	/**
	 * Finds an implementation of the given extension class, throwing an exception if no implementation
	 * is found.
	 * 
	 * <p>This method has the same expectations as {@link #findExtensions(Class)}.</p>
	 * 
	 * @param <T> the class of extension to find
	 * @param extensionClass the class object representing the extension point
	 * @return the first available implementation of the class
	 * @throws IllegalStateException if no implementation can be found
	 */
	public static <T> T findRequiredExtension(Class<T> extensionClass) {
		List<T> extensions = findExtensions(extensionClass);
		if(extensions.isEmpty()) {
			throw new IllegalStateException(MessageFormat.format("Unable to find implementation for required service {0}", extensionClass.getName()));
		}
		return extensions.get(0);
	}
	
	/**
	 * Executes the provided {@link Callable} inside an {@link AccessController} block
	 * and with the provided {@link ClassLoader} as the thread-context loader.
	 * 
	 * @param <T> the type of object returned by {@code c}
	 * @param cl the {@link ClassLoader} to use as the thread-context loader
	 * @param c the {@link Callable} to execute
	 * @return the value returned by {@code c}
	 * @since 2.1.0
	 */
	public static <T> T withClassLoader(ClassLoader cl, Callable<T> c) {
		try {
			return AccessController.doPrivileged((PrivilegedExceptionAction<T>)() -> {
				ClassLoader current = Thread.currentThread().getContextClassLoader();
				Thread.currentThread().setContextClassLoader(cl);
				try {
					return c.call();
				} finally {
					Thread.currentThread().setContextClassLoader(current);
				}
			});
		} catch (PrivilegedActionException e) {
			Throwable t = e.getCause();
			if(t == null) {
				throw new RuntimeException(e);
			} else if(t instanceof RuntimeException) {
				throw (RuntimeException)t;
			} else {
				throw new RuntimeException(t);
			}
		}
	}
	
	/**
	 * Retrieves the names list, including roles, for the effective user of the database.
	 * 
	 * @param database the database context to query
	 * @return a {@link List} of names and permutations
	 * @throws NotesException if there is a problem reading the names list
	 * @since 2.3.0
	 */
	@SuppressWarnings("unchecked")
	public static Collection<String> getUserNamesList(Database database) throws NotesException {
		Set<String> result = new HashSet<>();
		Session session = database.getParent();
		result.addAll(session.evaluate(" @UserNamesList ")); //$NON-NLS-1$
		result.addAll(database.queryAccessRoles(session.getEffectiveUserName()));
		return result;
	}
	
	/**
	 * Retrieves the OSGi bundle for the provided symbolic name.
	 * 
	 * <p>Unlike {@link Platform#getBundle(String)}, this method maintains an internal cache to
	 * speed up subsequent lookups.</p>
	 * 
	 * @param symbolicName the symbolic name of the bundle to look up
	 * @return an {@link Optional} describing the {@link Bundle} matching the name, or
	 *         an empty one if no such bundle is installed
	 * @since 2.4.0
	 */
	public static Optional<Bundle> getBundle(String symbolicName) {
		return Optional.ofNullable(BUNDLE_CACHE.computeIfAbsent(symbolicName, Platform::getBundle));
	}
	
	/**
	 * Returns an appropriate temp directory for the system. On Windows, this is
	 * equivalent to <code>System.getProperty("java.io.tmpdir")</code>. On
	 * Linux, however, since this seems to return the data directory in some
	 * cases, it uses <code>/tmp</code>.
	 *
	 * @return an appropriate temp directory for the system
	 * @since 2.4.0
	 */
	public static Path getTempDirectory() {
		String osName = AccessController.doPrivileged((PrivilegedAction<String>)() -> System.getProperty("os.name")); //$NON-NLS-1$
		if (osName.startsWith("Linux") || osName.startsWith("LINUX")) { //$NON-NLS-1$ //$NON-NLS-2$
			return Paths.get("/tmp"); //$NON-NLS-1$
		} else {
			String tempDir = AccessController.doPrivileged((PrivilegedAction<String>)() -> System.getProperty("java.io.tmpdir")); //$NON-NLS-1$
			return Paths.get(tempDir);
		}
	}
}
