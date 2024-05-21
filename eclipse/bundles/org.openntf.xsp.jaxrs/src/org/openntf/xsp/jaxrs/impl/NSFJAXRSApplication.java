/**
 * Copyright (c) 2018-2024 Contributors to the XPages Jakarta EE Support Project
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
import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import org.openntf.xsp.jakartaee.discovery.ApplicationPropertyLocator;
import org.openntf.xsp.jakartaee.module.ComponentModuleLocator;
import org.openntf.xsp.jakartaee.util.LibraryUtil;
import org.openntf.xsp.jakartaee.util.ModuleUtil;
import org.openntf.xsp.jakartaee.util.PriorityComparator;
import org.openntf.xsp.jaxrs.JAXRSClassContributor;

import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Feature;
import jakarta.ws.rs.ext.Provider;
import jakarta.ws.rs.ext.Providers;

/**
 * An {@link Application} subclass that searches the current module for resource classes.
 * 
 * @author Jesse Gallagher
 * @since 1.0.0
 */
public class NSFJAXRSApplication extends Application {
	private static final Logger log = Logger.getLogger(NSFJAXRSApplication.class.getPackage().getName());

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
		
		List<JAXRSClassContributor> contributors = ComponentModuleLocator.getDefault()
			.map(ComponentModuleLocator::getActiveModule)
			.map(module -> LibraryUtil.findExtensions(JAXRSClassContributor.class, module))
			.orElseGet(() -> LibraryUtil.findExtensions(JAXRSClassContributor.class));
		contributors.stream()
			.map(JAXRSClassContributor::getSingletons)
			.filter(Objects::nonNull)
			.forEach(result::addAll);
		
		return result;
	}
	
	@Override
	public Set<Class<?>> getClasses() {
		return ComponentModuleLocator.getDefault()
			.map(ComponentModuleLocator::getActiveModule)
			.map(module -> {
				Set<Class<?>> result = new HashSet<>();
				result.addAll(super.getClasses());
				
				List<JAXRSClassContributor> contributors = LibraryUtil.findExtensions(JAXRSClassContributor.class, module);
				contributors.stream()
					.map(JAXRSClassContributor::getClasses)
					.filter(Objects::nonNull)
					.forEach(result::addAll);
				
				ModuleUtil.getClasses(module)
					.filter(this::isJAXRSClass)
					.forEach(result::add);
				
				return result;
			})
			.orElseGet(Collections::emptySet);
	}
	
	@Override
	public Map<String, Object> getProperties() {
		Map<String, Object> result = new LinkedHashMap<>();
		
		// Read in xsp.properties
		LibraryUtil.findExtensions(ApplicationPropertyLocator.class)
			.stream()
			.sorted(PriorityComparator.DESCENDING)
			.filter(ApplicationPropertyLocator::isActive)
			.map(ApplicationPropertyLocator::getApplicationProperties)
			.filter(Optional::isPresent)
			.map(Optional::get)
			.forEach(properties -> {
				properties.forEach((key, value) -> result.put(key.toString(), value));
			});
		
		// Read in any contributors
		List<JAXRSClassContributor> contributors = ComponentModuleLocator.getDefault()
			.map(ComponentModuleLocator::getActiveModule)
			.map(module -> LibraryUtil.findExtensions(JAXRSClassContributor.class, module))
			.orElseGet(() -> LibraryUtil.findExtensions(JAXRSClassContributor.class));
		contributors.stream()
			.map(JAXRSClassContributor::getProperties)
			.filter(Objects::nonNull)
			.forEach(result::putAll);
		
		return result;
	}
	
	private boolean isJAXRSClass(Class<?> clazz) {
		try {
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
			
			if(clazz.isAnnotationPresent(Provider.class)) {
				return true;
			}
			
			return false;
		} catch(Throwable e) {
			// Catch Throwable because this may come through as an Error
			log.log(Level.WARNING, MessageFormat.format("Encounterd exception processing class {0}", clazz.getName()), e);
			return false;
		}
	}

}
