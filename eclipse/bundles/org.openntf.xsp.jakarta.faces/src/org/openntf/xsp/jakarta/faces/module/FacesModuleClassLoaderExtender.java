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
package org.openntf.xsp.jakarta.faces.module;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;

import com.ibm.designer.runtime.domino.adapter.ComponentModule;

import org.openntf.xsp.jakartaee.module.ModuleClassLoaderExtender;
import org.openntf.xsp.jakartaee.util.LibraryUtil;
import org.openntf.xsp.jakartaee.util.ModuleUtil;

import jakarta.faces.context.FacesContext;

/**
 * {@link ModuleClassLoaderExtender} implementation that handles blocking legacy
 * JSF class loading and provides access to resources from the Jakarta Faces bundle.
 * 
 * @since 3.4.0
 */
public class FacesModuleClassLoaderExtender implements ModuleClassLoaderExtender {
	
	@Override
	public Collection<ClassLoaderExtension> provide(ComponentModule module) {
		// XPages-having modules already use FacesBlockingClassLoader
		if(LibraryUtil.usesLibrary(LibraryUtil.LIBRARY_UI, module) && !ModuleUtil.hasXPages(module)) {
			return Collections.singleton(new FacesBlockingExtension());
		} else {
			return Collections.emptyList();
		}
	}

	public static class FacesBlockingExtension implements ClassLoaderExtension {
		private final Collection<ClassLoader> facesCl;
		
		public FacesBlockingExtension() {
			this.facesCl = Arrays.asList(FacesContext.class.getClassLoader(), getClass().getClassLoader());
		}

		@Override
		public Optional<Class<?>> loadClass(String name) throws ClassNotFoundException {
			if (name != null && name.startsWith("com.sun.faces.")) { //$NON-NLS-1$
				throw new ClassNotFoundException();
			}
			return Optional.empty();
		}

		@Override
		public Optional<URL> getResource(String name) {
			return facesCl.stream()
				.map(cl -> cl.getResource(name))
				.filter(Objects::nonNull)
				.findFirst();
		}

		@Override
		public Optional<InputStream> getResourceAsStream(String name) {
			return facesCl.stream()
				.map(cl -> cl.getResourceAsStream(name))
				.filter(Objects::nonNull)
				.findFirst();
		}

		@Override
		public Collection<URL> getResources(String name) throws IOException {
			return facesCl.stream()
				.map(cl -> {
					try {
						return cl.getResources(name);
					} catch (IOException e) {
						throw new UncheckedIOException(e);
					}
				})
				.map(Collections::list)
				.flatMap(Collection::stream)
				.toList();
		}
		
	}
}
