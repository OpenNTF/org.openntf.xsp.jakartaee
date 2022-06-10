/**
 * Copyright © 2018-2022 Contributors to the XPages Jakarta EE Support Project
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
package org.openntf.xsp.nosql.mapping.extension.impl;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;

import org.openntf.xsp.nosql.mapping.extension.DominoTemplate;
import org.openntf.xsp.nosql.mapping.extension.ViewCategory;
import org.openntf.xsp.nosql.mapping.extension.ViewEntries;
import jakarta.nosql.mapping.Entity;
import jakarta.nosql.mapping.Repository;

/**
 * Implementation proxy for extended capabilities for Domino document
 * repositories.
 * 
 * @author Jesse Gallagher
 *
 * @param <T> the model-object type produced by the repository
 */
public class DominoDocumentRepositoryProxy<T> implements InvocationHandler {

	private final Class<T> typeClass;
	private final DominoTemplate template;
	private final Repository<?, ?> repository;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	DominoDocumentRepositoryProxy(DominoTemplate template, Class<?> repositoryType, Repository<?, ?> repository) {
        this.template = template;
        this.typeClass = (Class) ((ParameterizedType) repositoryType.getGenericInterfaces()[0])
                .getActualTypeArguments()[0];
        this.repository = repository;
    }

	@Override
	public Object invoke(Object o, Method method, Object[] args) throws Throwable {

		// View entries support
		ViewEntries viewEntries = method.getAnnotation(ViewEntries.class);
		if(viewEntries != null) {
			// Check for category annotations on the method
			// TODO consider support for multiple categories
			String category = null;
			Parameter[] params = method.getParameters();
			for(int i = 0; i < params.length; i++) {
				if(params[i].isAnnotationPresent(ViewCategory.class)) {
					category = args[i] == null ? null : args[i].toString();
					break;
				}
			}
			
			String entityName = typeClass.getAnnotation(Entity.class).value();
			return template.viewEntryQuery(entityName, viewEntries.value(), category);
		}
		
		return method.invoke(repository, args);
	}
}
