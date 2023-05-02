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
package org.openntf.xsp.nosql.mapping.extension.impl;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

import org.eclipse.jnosql.mapping.reflection.Reflections;
import org.eclipse.jnosql.mapping.repository.DynamicReturn;
import org.eclipse.jnosql.mapping.repository.DynamicReturn.DefaultDynamicReturnBuilder;
import org.eclipse.jnosql.mapping.repository.RepositoryReturn;
import org.openntf.xsp.nosql.mapping.extension.DominoRepository;
import org.openntf.xsp.nosql.mapping.extension.DominoTemplate;
import org.openntf.xsp.nosql.mapping.extension.ViewDocuments;
import org.openntf.xsp.nosql.mapping.extension.ViewEntries;
import org.openntf.xsp.nosql.mapping.extension.ViewQuery;

import jakarta.enterprise.inject.spi.CDI;
import jakarta.nosql.ServiceLoaderProvider;
import jakarta.nosql.mapping.Entity;
import jakarta.nosql.mapping.Pagination;
import jakarta.nosql.mapping.Repository;
import jakarta.nosql.mapping.Sorts;

/**
 * Implementation proxy for extended capabilities for Domino document
 * repositories.
 * 
 * @author Jesse Gallagher
 *
 * @param <T> the model-object type produced by the repository
 */
public class DominoDocumentRepositoryProxy<T> implements InvocationHandler {
	
	// Known handled methods
	private static final Method putInFolder;
	private static final Method removeFromFolder;
	private static final Method saveWithForm;
	private static final Method getByNoteId;
	private static final Method getByNoteIdInt;
	private static final Method readViewEntries;
	private static final Method readViewDocuments;
	
	static {
		try {
			putInFolder = DominoRepository.class.getDeclaredMethod("putInFolder", Object.class, String.class); //$NON-NLS-1$
			removeFromFolder = DominoRepository.class.getDeclaredMethod("removeFromFolder", Object.class, String.class); //$NON-NLS-1$
			saveWithForm = DominoRepository.class.getDeclaredMethod("save", Object.class, boolean.class); //$NON-NLS-1$
			getByNoteId = DominoRepository.class.getDeclaredMethod("findByNoteId", String.class); //$NON-NLS-1$
			getByNoteIdInt = DominoRepository.class.getDeclaredMethod("findByNoteId", int.class); //$NON-NLS-1$
			readViewEntries = DominoRepository.class.getDeclaredMethod("readViewEntries", String.class, int.class, boolean.class, ViewQuery.class, Sorts.class, Pagination.class); //$NON-NLS-1$
			readViewDocuments = DominoRepository.class.getDeclaredMethod("readViewDocuments", String.class, int.class, boolean.class, ViewQuery.class, Sorts.class, Pagination.class); //$NON-NLS-1$
		} catch (NoSuchMethodException | SecurityException e) {
			throw new RuntimeException(e);
		}
	}

	private final Class<T> typeClass;
	private final DominoTemplate template;
	private final Repository<?, String> repository;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	DominoDocumentRepositoryProxy(DominoTemplate template, Class<?> repositoryType, Repository<?, String> repository) {
        this.template = template;
        this.typeClass = (Class) ((ParameterizedType) repositoryType.getGenericInterfaces()[0])
                .getActualTypeArguments()[0];
        if(!typeClass.isAnnotationPresent(Entity.class)) {
        	throw new IllegalStateException(MessageFormat.format("Target type \"{0}\" for repository class \"{1}\" is missing an @Entity annotation", typeClass.getName(), repositoryType.getName()));
        }
        this.repository = repository;
    }

	@Override
	public Object invoke(Object o, Method method, Object[] args) throws Throwable {
		
		// View entries support
		ViewEntries viewEntries = method.getAnnotation(ViewEntries.class);
		if(viewEntries != null) {
			Pagination pagination = findArg(args, Pagination.class);
			ViewQuery viewQuery = findArg(args, ViewQuery.class);
			Sorts sorts = findArg(args, Sorts.class);
			
			String entityName = typeClass.getAnnotation(Entity.class).value();
			if(entityName == null || entityName.isEmpty()) {
				entityName = typeClass.getSimpleName();
			}
			
			Class<?> returnType = method.getReturnType();
			boolean singleResult = !(Collection.class.isAssignableFrom(returnType) || Stream.class.isAssignableFrom(returnType));
			Object result = template.viewEntryQuery(entityName, viewEntries.value(), pagination, sorts, viewEntries.maxLevel(), viewEntries.documentsOnly(), viewQuery, singleResult);
			return convert(result, method);
		}
		if(method.equals(readViewEntries)) {
			String viewName = (String)args[0];
			int maxLevel = (int)args[1];
			boolean documentsOnly = (boolean)args[2];
			ViewQuery viewQuery = (ViewQuery)args[3];
			Sorts sorts = (Sorts)args[4];
			Pagination pagination = (Pagination)args[5];
			
			String entityName = typeClass.getAnnotation(Entity.class).value();
			if(entityName == null || entityName.isEmpty()) {
				entityName = typeClass.getSimpleName();
			}
			Object result = template.viewEntryQuery(entityName, viewName, pagination, sorts, maxLevel, documentsOnly, viewQuery, false);
			return convert(result, method);
		}
		
		// View documents support
		ViewDocuments viewDocuments = method.getAnnotation(ViewDocuments.class);
		if(viewDocuments != null) {
			Pagination pagination = findArg(args, Pagination.class);
			ViewQuery viewQuery = findArg(args, ViewQuery.class);
			Sorts sorts = findArg(args, Sorts.class);
			String entityName = typeClass.getAnnotation(Entity.class).value();
			if(entityName == null || entityName.isEmpty()) {
				entityName = typeClass.getSimpleName();
			}
			boolean distinct = viewDocuments.distinct();
			
			Class<?> returnType = method.getReturnType();
			boolean singleResult = !(Collection.class.isAssignableFrom(returnType) || Stream.class.isAssignableFrom(returnType));
			Object result = template.viewDocumentQuery(entityName, viewDocuments.value(), pagination, sorts, viewDocuments.maxLevel(), viewQuery, singleResult, distinct);
			return convert(result, method);
		}
		if(method.equals(readViewDocuments)) {
			String viewName = (String)args[0];
			int maxLevel = (int)args[1];
			boolean distinct = (boolean)args[2];
			ViewQuery viewQuery = (ViewQuery)args[3];
			Sorts sorts = (Sorts)args[4];
			Pagination pagination = (Pagination)args[5];
			
			String entityName = typeClass.getAnnotation(Entity.class).value();
			if(entityName == null || entityName.isEmpty()) {
				entityName = typeClass.getSimpleName();
			}
			Object result = template.viewDocumentQuery(entityName, viewName, pagination, sorts, maxLevel, viewQuery, false, distinct);
			return convert(result, method);
		}
		
		if(method.equals(putInFolder)) {
			String id = getId(args[0]);
			String folderName = (String)args[1];
			
			template.putInFolder(id, folderName);
			return null;
		}
		
		if(method.equals(removeFromFolder)) {
			String id = getId(args[0]);
			String folderName = (String)args[1];
			
			template.removeFromFolder(id, folderName);
			return null;
		}
		
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
		
		if(method.equals(getByNoteId)) {
			String entityName = typeClass.getAnnotation(Entity.class).value();
			if(entityName == null || entityName.isEmpty()) {
				entityName = typeClass.getSimpleName();
			}
			Object result = template.getByNoteId(entityName, (String)args[0]);
			return convert(result, method);
		}
		
		if(method.equals(getByNoteIdInt)) {
			String entityName = typeClass.getAnnotation(Entity.class).value();
			if(entityName == null || entityName.isEmpty()) {
				entityName = typeClass.getSimpleName();
			}
			Object result = template.getByNoteId(entityName, Integer.toHexString((int)args[0]));
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
	
	private <A> A findArg(Object[] args, Class<A> clazz) {
		if(args != null) {
			return Stream.of(args)
				.filter(clazz::isInstance)
				.map(clazz::cast)
				.findFirst()
				.orElse(null);
		} else {
			return null;
		}
	}
}
