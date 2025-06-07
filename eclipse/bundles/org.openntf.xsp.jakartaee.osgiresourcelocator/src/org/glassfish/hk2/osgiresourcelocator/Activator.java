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

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import com.ibm.commons.extension.ExtensionManager;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {
	/**
	 * Store looked up extensions by class for the lifetime of the JVM.
	 * @since 2.4.0
	 */
	private static final Map<Class<?>, List<?>> EXTENSION_CACHE = new ConcurrentHashMap<>();

	@Override
	public void start(final BundleContext bundleContext) throws Exception {
		ServiceLoader.init(bundleContext);
	}

	@Override
	public void stop(final BundleContext bundleContext) throws Exception {

	}
	
	@SuppressWarnings("unchecked")
	public static <T> List<T> findExtensions(final Class<T> extensionClass) {
		return (List<T>)computeIfAbsent(EXTENSION_CACHE, extensionClass, c ->
			AccessController.doPrivileged((PrivilegedAction<List<T>>)() ->
				(List<T>)ExtensionManager.findServices(null, c.getClassLoader(), c.getName(), c)
			)
		);
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
