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
