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
package org.openntf.xsp.jakarta.faces.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Optional;

import org.openntf.xsp.jakarta.faces.module.FacesModuleClassLoaderExtender.FacesBlockingExtension;

/**
 * This {@link ClassLoader} implementation blocks access from a JSF environment
 * to the XPages implementation classes. Additionally, it allows access to
 * META-INF resources from the Faces API bundle, which would otherwise
 * be invisible in OSGi.
 *
 * @author Jesse Gallagher
 * @since 2.12.0
 */
public class FacesBlockingClassLoader extends URLClassLoader {
	private final FacesBlockingExtension extension = new FacesBlockingExtension();

	public FacesBlockingClassLoader(final URL[] urls, final ClassLoader parent) {
		super(urls, parent);
	}

	@Override
	public Class<?> loadClass(final String name) throws ClassNotFoundException {
		Optional<Class<?>> c = extension.loadClass(name);
		if(c.isPresent()) {
			return c.get();
		} else {
			return super.loadClass(name);
		}
	}

	// The Faces API will already be on the classpath, but its resources won't be.
	//   Also shim in access to that

	@Override
	public URL getResource(final String name) {
		URL parent = super.getResource(name);
		if(parent != null) {
			return parent;
		}
		return extension.getResource(name).orElse(null);
	}

	@Override
	public InputStream getResourceAsStream(final String name) {
		InputStream parent = super.getResourceAsStream(name);
		if(parent != null) {
			return parent;
		}
		return extension.getResourceAsStream(name).orElse(null);
	}

	@Override
	public Enumeration<URL> getResources(final String name) throws IOException {
		List<URL> parent = Collections.list(super.getResources(name));
		parent.addAll(extension.getResources(name));
		return Collections.enumeration(parent);
	}

}
