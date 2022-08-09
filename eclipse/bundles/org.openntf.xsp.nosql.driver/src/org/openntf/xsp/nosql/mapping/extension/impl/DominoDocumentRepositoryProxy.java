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

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

import org.eclipse.jnosql.mapping.reflection.Reflections;
import org.eclipse.jnosql.mapping.repository.DynamicReturn;
import org.eclipse.jnosql.mapping.repository.RepositoryReturn;
import org.eclipse.jnosql.mapping.repository.DynamicReturn.DefaultDynamicReturnBuilder;
import org.openntf.xsp.nosql.mapping.extension.DominoRepository;
import org.openntf.xsp.nosql.mapping.extension.DominoTemplate;
import org.openntf.xsp.nosql.mapping.extension.ViewCategory;
import org.openntf.xsp.nosql.mapping.extension.ViewDocuments;
import org.openntf.xsp.nosql.mapping.extension.ViewEntries;
import org.openntf.xsp.nosql.mapping.extension.ViewKey;

import jakarta.enterprise.inject.spi.CDI;
import jakarta.nosql.ServiceLoaderProvider;
import jakarta.nosql.mapping.Entity;
import jakarta.nosql.mapping.Pagination;
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
	private final Repository<?, String> repository;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	DominoDocumentRepositoryProxy(DominoTemplate template, Class<?> repositoryType, Repository<?, String> repository) {
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
			
			Pagination pagination;
			if(args != null) {
				pagination = Stream.of(args)
					.filter(Pagination.class::isInstance)
					.map(Pagination.class::cast)
					.findFirst()
					.orElse(null);
			} else {
				pagination = null;
			}
			String entityName = typeClass.getAnnotation(Entity.class).value();
			if(entityName == null || entityName.isEmpty()) {
				entityName = typeClass.getSimpleName();
			}
			
			ViewKeyQuery keyQuery = composeViewKeyQuery(method, args);
			Object result = template.viewEntryQuery(entityName, viewEntries.value(), category, pagination, viewEntries.maxLevel(), viewEntries.documentsOnly(), keyQuery);
			return convert(result, method);
		}
		
		// View documents support
		ViewDocuments viewDocuments = method.getAnnotation(ViewDocuments.class);
		if(viewDocuments != null) {
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
			
			Pagination pagination;
			if(args != null) {
				pagination = Stream.of(args)
					.filter(Pagination.class::isInstance)
					.map(Pagination.class::cast)
					.findFirst()
					.orElse(null);
			} else {
				pagination = null;
			}
			String entityName = typeClass.getAnnotation(Entity.class).value();
			if(entityName == null || entityName.isEmpty()) {
				entityName = typeClass.getSimpleName();
			}
			
			ViewKeyQuery keyQuery = composeViewKeyQuery(method, args);
			Object result = template.viewDocumentQuery(entityName, viewDocuments.value(), category, pagination, viewDocuments.maxLevel(), keyQuery);
			return convert(result, method);
		}
		
		Method putInFolder = DominoRepository.class.getDeclaredMethod("putInFolder", Object.class, String.class); //$NON-NLS-1$
		if(method.equals(putInFolder)) {
			String id = getId(args[0]);
			String folderName = (String)args[1];
			
			template.putInFolder(id, folderName);
			return null;
		}
		Method removeFromFolder = DominoRepository.class.getDeclaredMethod("removeFromFolder", Object.class, String.class); //$NON-NLS-1$
		if(method.equals(removeFromFolder)) {
			String id = getId(args[0]);
			String folderName = (String)args[1];
			
			template.removeFromFolder(id, folderName);
			return null;
		}
		
		Method saveWithForm = DominoRepository.class.getDeclaredMethod("save", Object.class, boolean.class); //$NON-NLS-1$
		if(method.equals(saveWithForm)) {
			String id = getId(args[0]);
			if(id !=null && !id.isEmpty() && template.existsById(id)) {
				Object result = template.update(args[0], (boolean)args[1]);
				return convert(result, method);
			} else {
				Object result = template.insert(args[0], (boolean)args[1]);
				return convert(result, method);
			}
		}
		
		Method getByNoteId = DominoRepository.class.getDeclaredMethod("findByNoteId", String.class); //$NON-NLS-1$
		if(method.equals(getByNoteId)) {
			String entityName = typeClass.getAnnotation(Entity.class).value();
			if(entityName == null || entityName.isEmpty()) {
				entityName = typeClass.getSimpleName();
			}
			Object result = template.getByNoteId(entityName, (String)args[0]);
			return convert(result, method);
		}
		
		return method.invoke(repository, args);
	}
	
	private String getId(Object entity) {
		Reflections reflections = CDI.current().select(Reflections.class).get();
		
		Field idField = reflections.getFields(entity.getClass())
			.stream()
			.filter(reflections::isIdField)
			.findFirst()
			.orElseThrow(() -> new IllegalStateException("Unable to find @Id field on " + entity.getClass()));
		
		try {
			idField.setAccessible(true);
			return (String)idField.get(entity);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}
	
	@SuppressWarnings("unchecked")
	private Object convert(Object result, Method method) {
		Class<?> returnType = method.getReturnType();

		RepositoryReturn repoReturn = ServiceLoaderProvider.getSupplierStream(RepositoryReturn.class)
				.filter(RepositoryReturn.class::isInstance)
				.map(RepositoryReturn.class::cast)
				.filter(r -> r.isCompatible(typeClass, returnType))
				.findFirst()
				.orElse(null);
		if(repoReturn == null) {
			return result;
		} else {
			DefaultDynamicReturnBuilder<Object> builder = DynamicReturn.builder()
				.withMethodSource(method)
				.withClassSource(typeClass);
			if(result instanceof Stream) {
				builder = builder.withResult(() -> (Stream<Object>)result)
					.withSingleResult(() -> ((Stream<Object>)result).findFirst());
			} else if(result instanceof Collection) {
				builder = builder.withResult(() -> ((Collection<Object>)result).stream())
					.withSingleResult(() -> ((Collection<Object>)result).stream().findFirst());
			} else if(result instanceof Optional) {
				Optional<Object> opt = (Optional<Object>)result;
				builder = builder.withResult(() -> opt.isPresent() ? Stream.of(opt.get()) : Stream.empty())
					.withSingleResult(() -> opt);
			} else {
				Optional<Object> opt = Optional.ofNullable(result);
				builder = builder.withResult(() -> opt.isPresent() ? Stream.of(opt.get()) : Stream.empty())
					.withSingleResult(() -> opt);
			}
			return repoReturn.convert(builder.build());
		}
	}
	
	@SuppressWarnings("unchecked")
	private ViewKeyQuery composeViewKeyQuery(Method method, Object[] args) {
		Annotation[][] paramAnnotations = method.getParameterAnnotations();
		Class<?>[] paramTypes = method.getParameterTypes();
		Class<?> returnType = method.getReturnType();
		
		boolean exact = false;
		Collection<Object> keys = null;
		for(int i = 0; i < paramAnnotations.length; i++) {
			Annotation[] param = paramAnnotations[i];
			ViewKey viewKey = Arrays.stream(param)
				.filter(ann -> ViewKey.class.equals(ann.annotationType()))
				.map(ViewKey.class::cast)
				.findFirst()
				.orElse(null);
			if(viewKey != null) {
				// If this is our first one, store the exactMatch value
				if(keys == null) {
					exact = viewKey.exact();
				}
				
				Object arg = args[i];
				if(Collection.class.isAssignableFrom(paramTypes[i])) {
					// Then ignore any subsequent keys
					keys = (Collection<Object>)arg;
					break;
				} else {
					// Then add to our existing pool
					if(keys == null) {
						keys = new ArrayList<>();
					}
					keys.add(arg);
				}
			}
		}
		
		// Check the return type to see if it should be a single entry or not
		boolean singleResult = !(Collection.class.isAssignableFrom(returnType) || Stream.class.isAssignableFrom(returnType));
		
		return new ViewKeyQuery(keys, exact, singleResult);
	}
}
