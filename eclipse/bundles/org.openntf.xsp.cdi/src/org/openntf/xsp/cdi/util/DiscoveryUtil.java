/**
 * Copyright © 2018-2022 Contributors to the XPages Jakarta EE Support Project
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

import java.util.stream.Stream;

import org.openntf.xsp.jakartaee.util.LibraryUtil;
import org.osgi.framework.Bundle;

/**
 * @author Jesse Gallagher
 * @since 2.2.0
 */
public enum DiscoveryUtil {
	;
	
	/**
	 * Searches through the provided bundle to find all exported class names.
	 * 
	 * <p>This restricts querying to bundles with a beans.xml file and to classes within
	 * the bundle's {@code Export-Package} listing.</p>
	 * 
	 * @param bundle a {@link Bundle} instance to query
	 * @param force include classes even when there's no beans.xml
	 * @return a {@link Stream} of discovered exported classes
	 * @throws RuntimeException if there is a problem parsing the bundle manifest
	 */
	public static Stream<String> findExportedClassNames(Bundle bundle, boolean force) {
		if(force || bundle.getResource("/META-INF/beans.xml") != null || bundle.getResource("/WEB-INF/beans.xml") != null) { //$NON-NLS-1$ //$NON-NLS-2$
			return LibraryUtil.findBundleClassNames(bundle, true);
		}
		
		return Stream.empty();
	}
	
	/**
	 * Searches through the provided bundle to find all exported classes, loading them
	 * from the bundle.
	 * 
	 * <p>This restricts querying to bundles with a beans.xml file and to classes within
	 * the bundle's {@code Export-Package} listing.</p>
	 * 
	 * @param bundle a {@link Bundle} instance to query
	 * @param force include classes even when there's no beans.xml
	 * @return a {@link Stream} of discovered exported classes
	 * @throws RuntimeException if there is a problem parsing the bundle manifest or loading
	 *         a class
	 * @since 2.3.0
	 */
	public static Stream<Class<?>> findExportedClasses(Bundle bundle, boolean force) {
		return findExportedClassNames(bundle, force)
			.map(className -> {
				try {
					return bundle.loadClass(className);
				} catch (ClassNotFoundException e) {
					throw new RuntimeException(e);
				}
			});
	}
}
