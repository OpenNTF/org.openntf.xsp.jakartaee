/**
 * Copyright © 2018-2021 Jesse Gallagher
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
package org.openntf.xsp.jakartaee;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import com.ibm.commons.extension.ExtensionManager;
import com.ibm.designer.domino.napi.NotesAPIException;
import com.ibm.designer.domino.napi.NotesDatabase;
import com.ibm.designer.domino.napi.NotesNote;
import com.ibm.designer.domino.napi.design.FileAccess;
import com.ibm.designer.runtime.domino.adapter.ComponentModule;
import com.ibm.xsp.application.ApplicationEx;

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
	 * Determines whether the provided {@link ApplicationEx} uses the provided library
	 * ID.
	 * 
	 * @param libraryId the library ID to look for
	 * @param app the application instance to check
	 * @return whether the library is loaded by the application
	 */
	public static boolean usesLibrary(String libraryId, ApplicationEx app) {
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
	 * @throws IOException if there is a problem reading the xsp.properties file in the module
	 * @since 1.2.0
	 */
	public static boolean usesLibrary(String libraryId, ComponentModule module) throws IOException {
		Properties props = new Properties();
		try(InputStream is = module.getResourceAsStream("/WEB-INF/xsp.properties")) { //$NON-NLS-1$
			props.load(is);
		}
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
	 * @throws IOException if there is a problem reading the xsp.properties file in the module
	 * @throws NotesAPIException if there is a problem reading the xsp.properties file in the module
	 * @since 1.2.0
	 */
	public static boolean usesLibrary(String libraryId, NotesDatabase database) throws NotesAPIException, IOException {
		Properties props = getXspProperties(database);
		String prop = props.getProperty("xsp.library.depends", ""); //$NON-NLS-1$ //$NON-NLS-2$
		return Arrays.asList(prop.split(",")).contains(libraryId); //$NON-NLS-1$
	}
	
	/**
	 * Reads the xsp.properties file for the provided database. 
	 * 
	 * @param database the database to read
	 * @return a {@link Properties} file with the database's XSP properties loaded, if available
	 * @throws IOException if there is a problem reading the xsp.properties file in the module
	 * @throws NotesAPIException if there is a problem reading the xsp.properties file in the module
	 * @since 1.2.0
	 */
	public static Properties getXspProperties(NotesDatabase database) throws NotesAPIException, IOException {
		String dbReplicaId = database.getReplicaID();
		
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
	 * Finds extensions for the given class using the IBM Commons extension mechanism.
	 * 
	 * <p>This method assumes that the extension point name is the same as the qualified class name.</p>
	 * 
	 * @param <T> the class of extension to find
	 * @param extensionClass the class object representing the extension point
	 * @return a {@link List} of service objects for the class
	 */
	public static <T> List<T> findExtensions(Class<T> extensionClass) {
		return AccessController.doPrivileged((PrivilegedAction<List<T>>)() ->
			ExtensionManager.findServices(null, extensionClass.getClassLoader(), extensionClass.getName(), extensionClass)
		);
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
}
