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
package org.openntf.xsp.jaxrs.impl;

import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Stream;

import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Feature;
import jakarta.ws.rs.ext.Providers;

import org.openntf.xsp.jakartaee.util.LibraryUtil;
import org.openntf.xsp.jakartaee.util.ModuleUtil;
import org.openntf.xsp.jaxrs.JAXRSClassContributor;

import com.ibm.domino.xsp.module.nsf.NSFComponentModule;
import com.ibm.domino.xsp.module.nsf.NotesContext;

/**
 * An {@link Application} subclass that searches the current module for resource classes.
 * 
 * @author Jesse Gallagher
 * @since 1.0.0
 */
public class NSFJAXRSApplication extends Application {

	public NSFJAXRSApplication() {
	}
	
	@Override
	public Set<Object> getSingletons() {
		Set<Object> result = new HashSet<>();
		result.addAll(super.getSingletons());
		
		List<Providers> providers = LibraryUtil.findExtensionsUncached(Providers.class);
		result.addAll(providers);
		
		List<Feature> features = LibraryUtil.findExtensionsUncached(Feature.class);
		result.addAll(features);
		
		List<JAXRSClassContributor> contributors = LibraryUtil.findExtensions(JAXRSClassContributor.class);
		contributors.stream()
			.map(JAXRSClassContributor::getSingletons)
			.filter(Objects::nonNull)
			.forEach(result::addAll);
		
		return result;
	}
	
	@Override
	public Set<Class<?>> getClasses() {
		NSFComponentModule module = NotesContext.getCurrent().getModule();
		Set<Class<?>> result = new HashSet<>();
		result.addAll(super.getClasses());
		
		List<JAXRSClassContributor> contributors = LibraryUtil.findExtensions(JAXRSClassContributor.class);
		contributors.stream()
			.map(JAXRSClassContributor::getClasses)
			.filter(Objects::nonNull)
			.forEach(result::addAll);
		
		ModuleUtil.getClassNames(module)
			.filter(className -> !ModuleUtil.GENERATED_CLASSNAMES.matcher(className).matches())
			.distinct()
			.map(className -> loadClass(module, className))
			.filter(this::isJAXRSClass)
			.forEach(result::add);
		return result;
	}
	
	@Override
	public Map<String, Object> getProperties() {
		Map<String, Object> result = new LinkedHashMap<>();
		// Read in xsp.properties
		NSFComponentModule module = NotesContext.getCurrent().getModule();
		Properties xspProperties = LibraryUtil.getXspProperties(module);
		xspProperties.forEach((key, value) -> result.put(key.toString(), value));
		return result;
	}
	
	private boolean isJAXRSClass(Class<?> clazz) {
		if(clazz.isInterface()) {
			return false;
		}
		if(Modifier.isAbstract(clazz.getModifiers())) {
			return false;
		}
		if(clazz.isAnnotationPresent(Path.class)) {
			return true;
		}
		
		if(Stream.of(clazz.getMethods()).anyMatch(m -> m.isAnnotationPresent(Path.class))) {
			return true;
		}
		
		return false;
	}
	
	private static Class<?> loadClass(NSFComponentModule module, String className) {
		try {
			return module.getModuleClassLoader().loadClass(className);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

}
