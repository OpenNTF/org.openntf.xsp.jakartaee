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
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.Optional;

import com.ibm.designer.runtime.domino.adapter.ComponentModule;

/**
 * This extension interface defines classes that to be involved in
 * the ClassLoader for custom {@link ComponentModule} types.
 * 
 * @since 3.4.0
 */
public interface ModuleClassLoaderExtender {
	interface ClassLoaderExtension {
		/**
		 * Called before delegates to load a class, primarily to allow for ClassNotFoundException.
		 * 
		 * @param name the name of the class to load
		 * @return an {@link Optional} containing the class if present, or an empty one if not
		 * @throws ClassNotFoundException if the class should be blocked from loading
		 */
		Optional<Class<?>> loadClass(final String name) throws ClassNotFoundException;
		
		Optional<URL> getResource(final String name);
		
		Optional<InputStream> getResourceAsStream(final String name);
		
		Collection<URL> getResources(final String name) throws IOException;
	}
	
	Collection<ClassLoaderExtension> provide(ComponentModule module);
}
