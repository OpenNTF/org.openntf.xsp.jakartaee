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
package org.openntf.xsp.nosql.communication.driver.impl;

import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.jnosql.mapping.reflection.ClassInformationNotFoundException;
import org.eclipse.jnosql.mapping.reflection.ClassMapping;
import org.eclipse.jnosql.mapping.reflection.ClassMappings;

import jakarta.enterprise.inject.spi.CDI;

/**
 * Contains utility methods for working with NoSQL entities.
 * 
 * @author Jesse Gallagher
 * @since 2.9.0
 */
public enum EntityUtil {
	;
	
	public static ClassMapping getClassMapping(String entityName) {
		ClassMappings mappings = CDI.current().select(ClassMappings.class).get();
		try {
			return mappings.findByName(entityName);
		} catch(ClassInformationNotFoundException e) {
			// Shouldn't happen, but we should account for it
			return null;
		}
	}
	
	public static Map<String, Class<?>> getItemTypes(ClassMapping classMapping) {
		return classMapping == null ? Collections.emptyMap() : classMapping.getFields()
			.stream()
			.collect(Collectors.toMap(
				f -> f.getName(),
				f -> f.getNativeField().getType()
			));
	}
}
