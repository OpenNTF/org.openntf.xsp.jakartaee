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
package org.openntf.xsp.jakarta.nosql.scanner;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jnosql.mapping.NoSQLRepository;
import org.eclipse.jnosql.mapping.metadata.ClassScanner;
import org.openntf.xsp.jakartaee.module.ComponentModuleLocator;
import org.openntf.xsp.jakartaee.util.ModuleUtil;

import jakarta.data.repository.BasicRepository;
import jakarta.data.repository.CrudRepository;
import jakarta.data.repository.DataRepository;
import jakarta.nosql.Embeddable;
import jakarta.nosql.Entity;

/**
 * Implementation of {@link ClassScanner} that scans the active component module
 * dynamically.
 * 
 * @since 3.0.0
 */
public class ComponentModuleClassScanner implements ClassScanner {

	@Override
	public Set<Class<?>> entities() {
		return ComponentModuleLocator.getDefault()
			.map(ComponentModuleLocator::getActiveModule)
			.map(ModuleUtil::getClasses)
			.map(classes -> {
				return classes.filter(c -> c.isAnnotationPresent(Entity.class))
					.collect(Collectors.toSet());
			})
			.orElseGet(Collections::emptySet);
	}

	@Override
	public Set<Class<?>> repositories() {
		return ComponentModuleLocator.getDefault()
			.map(ComponentModuleLocator::getActiveModule)
			.map(ModuleUtil::getClasses)
			.map(classes -> {
				return classes.filter(c -> DataRepository.class.isAssignableFrom(c))
					.collect(Collectors.toSet());
			})
			.orElseGet(Collections::emptySet);
	}

	@Override
	public Set<Class<?>> embeddables() {
		return ComponentModuleLocator.getDefault()
			.map(ComponentModuleLocator::getActiveModule)
			.map(ModuleUtil::getClasses)
			.map(classes -> {
				return classes.filter(c -> c.isAnnotationPresent(Embeddable.class))
					.collect(Collectors.toSet());
			})
			.orElseGet(Collections::emptySet);
	}

	@Override
	public <T extends DataRepository<?, ?>> Set<Class<?>> repositories(Class<T> filter) {
		return ComponentModuleLocator.getDefault()
			.map(ComponentModuleLocator::getActiveModule)
			.map(ModuleUtil::getClasses)
			.map(classes -> {
				return classes.filter(c -> DataRepository.class.isAssignableFrom(c))
					.filter(c -> filter.isAssignableFrom(c))
					.collect(Collectors.toSet());
			})
			.orElseGet(Collections::emptySet);
	}

	@Override
	public Set<Class<?>> repositoriesStandard() {
		return ComponentModuleLocator.getDefault()
			.map(ComponentModuleLocator::getActiveModule)
			.map(ModuleUtil::getClasses)
			.map(classes -> {
				return classes.filter(c -> {
					List<Class<?>> interfaces = Arrays.asList(c.getInterfaces());
                    return interfaces.contains(CrudRepository.class)
                            || interfaces.contains(BasicRepository.class)
                            || interfaces.contains(NoSQLRepository.class)
                            || interfaces.contains(DataRepository.class);
				})
				.collect(Collectors.toSet());
			})
			.orElseGet(Collections::emptySet);
	}

}
