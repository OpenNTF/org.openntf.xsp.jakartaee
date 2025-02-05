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
package org.openntf.xsp.jakarta.nosql.communication.driver.impl;

import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.eclipse.jnosql.mapping.metadata.ClassInformationNotFoundException;
import org.eclipse.jnosql.mapping.metadata.EntitiesMetadata;
import org.eclipse.jnosql.mapping.metadata.EntityMetadata;
import org.eclipse.jnosql.mapping.metadata.FieldMetadata;
import org.eclipse.jnosql.mapping.reflection.DefaultFieldMetadata;
import org.openntf.xsp.jakarta.nosql.mapping.extension.FormName;

import jakarta.enterprise.inject.spi.CDI;
import jakarta.nosql.Column;

/**
 * Contains utility methods for working with NoSQL entities.
 *
 * @author Jesse Gallagher
 * @since 2.9.0
 */
@SuppressWarnings({ "removal", "deprecation" })
public enum EntityUtil {
	;

	// For now, assumine that all implementations used AbstractFieldMetadata
	private static final Field fieldField;
	static {
		fieldField = AccessController.doPrivileged((PrivilegedAction<Field>)() -> {
			try {
				Field result = DefaultFieldMetadata.class.getSuperclass().getDeclaredField("field"); //$NON-NLS-1$
				result.setAccessible(true);
				return result;
			} catch (NoSuchFieldException | SecurityException e) {
				throw new RuntimeException(e);
			}
		});
	}

	public static EntityMetadata getClassMapping(final String entityName) {
		EntitiesMetadata mappings = CDI.current().select(EntitiesMetadata.class).get();
		try {
			return mappings.findByName(entityName);
		} catch(ClassInformationNotFoundException e) {
			// Shouldn't happen, but we should account for it
			return null;
		}
	}

	public static Map<String, Class<?>> getItemTypes(final EntityMetadata classMapping) {
		return classMapping == null ? Collections.emptyMap() : classMapping.fields()
			.stream()
			.collect(Collectors.toMap(
				(Function<? super FieldMetadata, ? extends String>) FieldMetadata::name,
				f -> getNativeField(f).getType()
			));
	}

	/**
	 * Determines the back-end item name for the given Java property.
	 *
	 * @param propName the Java property to check
	 * @param mapping the {@link ClassMapping} instance for the class in question
	 * @return the effective item name based on the class properties
	 */
	public static String findItemName(final String propName, final EntityMetadata mapping) {
		if(mapping != null) {
			Column annotation = mapping.fieldMapping(propName)
				.map(EntityUtil::getNativeField)
				.map(f -> f.getAnnotation(Column.class))
				.orElse(null);
			if(annotation != null && !annotation.value().isEmpty()) {
				return annotation.value();
			} else {
				return propName;
			}
		} else {
			return propName;
		}
	}

	public static Field getNativeField(final FieldMetadata fieldMapping) {
		try {
			return (Field)fieldField.get(fieldMapping);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	public static String getFormName(EntityMetadata classMapping) {
		FormName ann = classMapping.type().getAnnotation(FormName.class);
		if(ann != null) {
			return ann.value();
		} else {
			return classMapping.name();
		}
	}
}
