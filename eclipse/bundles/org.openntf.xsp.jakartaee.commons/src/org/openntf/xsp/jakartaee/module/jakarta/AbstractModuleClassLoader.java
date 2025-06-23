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
package org.openntf.xsp.jakartaee.module.jakarta;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import com.ibm.commons.extension.ExtensionManager.ApplicationClassLoader;

import org.openntf.xsp.jakartaee.module.ModuleClassLoaderExtender;
import org.openntf.xsp.jakartaee.module.ModuleClassLoaderExtender.ClassLoaderExtension;
import org.openntf.xsp.jakartaee.util.LibraryUtil;

/**
 * @since 3.5.0
 */
public abstract class AbstractModuleClassLoader extends URLClassLoader implements ApplicationClassLoader {
	private final AbstractJakartaModule module;
	private final List<ClassLoaderExtension> extensions;
	
	public AbstractModuleClassLoader(AbstractJakartaModule module, String name, URL[] urls, ClassLoader parent) {
		super(name, urls, parent);
		
		this.module = module;
		this.extensions = LibraryUtil.findExtensions(ModuleClassLoaderExtender.class).stream()
			.map(l -> l.provide(module))
			.filter(Objects::nonNull)
			.flatMap(Collection::stream)
			.toList();
	}
	
	public AbstractJakartaModule getModule() {
		return module;
	}
	
	protected List<ClassLoaderExtension> getExtensions() {
		return extensions;
	}

	@Override
	public Enumeration<URL> findApplicationResources(String path) throws IOException {
		// TODO figure out why DynamicClassLoader branches on hasJars - maybe performance?
		return super.findResources(path);
	}

	public InputStream getJarResourceAsStream(String name) {
		return super.getResourceAsStream(name);
	}

	public URL getJarResource(String name) {
		return super.getResource(name);
	}
	
	public abstract Set<String> getClassNames();
}