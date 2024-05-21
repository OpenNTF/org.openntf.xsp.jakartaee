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
package org.openntf.xsp.nosql.mapping.extension.impl;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.text.MessageFormat;
import java.time.temporal.TemporalAccessor;
import java.util.Collection;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.ServiceLoader.Provider;
import java.util.stream.Stream;

import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.core.query.AbstractRepository;
import org.eclipse.jnosql.mapping.core.repository.DynamicReturn;
import org.eclipse.jnosql.mapping.core.repository.DynamicReturn.DefaultDynamicReturnBuilder;
import org.eclipse.jnosql.mapping.core.repository.RepositoryReturn;
import org.eclipse.jnosql.mapping.metadata.EntitiesMetadata;
import org.eclipse.jnosql.mapping.metadata.EntityMetadata;
import org.eclipse.jnosql.mapping.semistructured.SemiStructuredTemplate;
import org.eclipse.jnosql.mapping.semistructured.query.AbstractSemiStructuredRepositoryProxy;
import org.eclipse.jnosql.mapping.semistructured.query.SemiStructuredRepositoryProxy;
import org.openntf.xsp.nosql.mapping.extension.DominoReflections;
import org.openntf.xsp.nosql.mapping.extension.DominoRepository;
import org.openntf.xsp.nosql.mapping.extension.DominoRepository.CalendarModScope;
import org.openntf.xsp.nosql.mapping.extension.DominoTemplate;
import org.openntf.xsp.nosql.mapping.extension.ViewDocuments;
import org.openntf.xsp.nosql.mapping.extension.ViewEntries;
import org.openntf.xsp.nosql.mapping.extension.ViewQuery;

import jakarta.data.Sort;
import jakarta.data.page.PageRequest;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.nosql.Entity;
import jakarta.validation.ConstraintViolationException;

/**
 * Implementation proxy for extended capabilities for Domino document
 * repositories.
 * 
 * @author Jesse Gallagher
 *
 * @param <T> the model-object type produced by the repository
 */
public class DominoDocumentRepositoryProxy<T, K> extends AbstractSemiStructuredRepositoryProxy<T, K> {
	
	// Known handled methods
	private static final Method putInFolder;
	private static final Method removeFromFolder;
	private static final Method saveWithForm;
	private static final Method getByNoteId;
	private static final Method getByNoteIdInt;
	private static final Method readViewEntries;
	private static final Method readViewDocuments;
	private static final Method getViewInfo;
	private static final Method findNamedDocument;
	private static final Method findProfileDocument;
	private static final Method readCalendarRange;
	private static final Method readCalendarEntry;
	private static final Method createCalendarEntry;
	private static final Method updateCalendarEntry;
	private static final Method removeCalendarEntry;
	
	static {
		try {
			putInFolder = DominoRepository.class.getDeclaredMethod("putInFolder", Object.class, String.class); //$NON-NLS-1$
			removeFromFolder = DominoRepository.class.getDeclaredMethod("removeFromFolder", Object.class, String.class); //$NON-NLS-1$
			saveWithForm = DominoRepository.class.getDeclaredMethod("save", Object.class, boolean.class); //$NON-NLS-1$
			getByNoteId = DominoRepository.class.getDeclaredMethod("findByNoteId", String.class); //$NON-NLS-1$
			getByNoteIdInt = DominoRepository.class.getDeclaredMethod("findByNoteId", int.class); //$NON-NLS-1$
			readViewEntries = DominoRepository.class.getDeclaredMethod("readViewEntries", String.class, int.class, boolean.class, ViewQuery.class, Sort.class, PageRequest.class); //$NON-NLS-1$
			readViewDocuments = DominoRepository.class.getDeclaredMethod("readViewDocuments", String.class, int.class, boolean.class, ViewQuery.class, Sort.class, PageRequest.class); //$NON-NLS-1$
			getViewInfo = DominoRepository.class.getDeclaredMethod("getViewInfo"); //$NON-NLS-1$
			findNamedDocument = DominoRepository.class.getDeclaredMethod("findNamedDocument", String.class, String.class); //$NON-NLS-1$
			findProfileDocument = DominoRepository.class.getDeclaredMethod("findProfileDocument", String.class, String.class); //$NON-NLS-1$
			readCalendarRange = DominoRepository.class.getDeclaredMethod("readCalendarRange", TemporalAccessor.class, TemporalAccessor.class, PageRequest.class); //$NON-NLS-1$
			readCalendarEntry = DominoRepository.class.getDeclaredMethod("readCalendarEntry", String.class); //$NON-NLS-1$
			createCalendarEntry = DominoRepository.class.getDeclaredMethod("createCalendarEntry", String.class, boolean.class); //$NON-NLS-1$
			updateCalendarEntry = DominoRepository.class.getDeclaredMethod("updateCalendarEntry", String.class, String.class, String.class, boolean.class, boolean.class, String.class); //$NON-NLS-1$
			removeCalendarEntry = DominoRepository.class.getDeclaredMethod("removeCalendarEntry", String.class, CalendarModScope.class, String.class); //$NON-NLS-1$
		} catch (NoSuchMethodException | SecurityException e) {
			throw new RuntimeException(e);
		}
	}

	private final Class<T> typeClass;
	private final DominoTemplate template;
	private final AbstractRepository<T, K> repository;
	
	private final Converters converters;
	private final EntityMetadata entityMetadata;
	private final Class<?> repositoryType;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	DominoDocumentRepositoryProxy(DominoTemplate template, Class<?> repositoryType, Converters converters, EntitiesMetadata entitiesMetadata) {
        this.template = template;
        this.typeClass = (Class) ((ParameterizedType) repositoryType.getGenericInterfaces()[0])
                .getActualTypeArguments()[0];
        if(!typeClass.isAnnotationPresent(Entity.class)) {
        	throw new IllegalStateException(MessageFormat.format("Target type \"{0}\" for repository class \"{1}\" is missing an @Entity annotation", typeClass.getName(), repositoryType.getName()));
        }
        this.converters = converters;
        this.entityMetadata = entitiesMetadata.get(typeClass);
        this.repositoryType = repositoryType;
        this.repository = SemiStructuredRepositoryProxy.SemiStructuredRepository.of(template, entityMetadata);
    }

	@Override
	public Object invoke(Object o, Method method, Object[] args) throws Throwable {
		// View entries support
		ViewEntries viewEntries = method.getAnnotation(ViewEntries.class);
		if(viewEntries != null) {
			PageRequest pagination = findArg(args, PageRequest.class);
			ViewQuery viewQuery = findArg(args, ViewQuery.class);
			Sort<?> sorts = findArg(args, Sort.class);
			
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
			Sort<?> sorts = (Sort<?>)args[4];
			PageRequest pagination = (PageRequest)args[5];
			
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
			PageRequest pagination = findArg(args, PageRequest.class);
			ViewQuery viewQuery = findArg(args, ViewQuery.class);
			Sort<?> sorts = findArg(args, Sort.class);
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
			Sort<?> sorts = (Sort<?>)args[4];
			PageRequest pagination = (PageRequest)args[5];
			
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
		
		if(method.equals(getViewInfo)) {
			return template.getViewInfo();
		}
		
		if(method.equals(findNamedDocument)) {
			String entityName = typeClass.getAnnotation(Entity.class).value();
			if(entityName == null || entityName.isEmpty()) {
				entityName = typeClass.getSimpleName();
			}
			Object result = template.getByName(entityName, (String)args[0], (String)args[1]);
			return convert(result, method);
		}
		
		if(method.equals(findProfileDocument)) {
			String entityName = typeClass.getAnnotation(Entity.class).value();
			if(entityName == null || entityName.isEmpty()) {
				entityName = typeClass.getSimpleName();
			}
			Object result = template.getProfileDocument(entityName, (String)args[0], (String)args[1]);
			return convert(result, method);
		}
		
		// Calendar operations
		if(method.equals(readCalendarRange)) {
			return template.readCalendarRange((TemporalAccessor)args[0], (TemporalAccessor)args[1], (PageRequest)args[2]);
		}
		if(method.equals(readCalendarEntry)) {
			return template.readCalendarEntry((String)args[0]);
		}
		if(method.equals(createCalendarEntry)) {
			return template.createCalendarEntry((String)args[0], (boolean)args[1]);
		}
		if(method.equals(updateCalendarEntry)) {
			template.updateCalendarEntry((String)args[0], (String)args[1], (String)args[2], (boolean)args[3], (boolean)args[4], (String)args[5]);
			return null;
		}
		if(method.equals(removeCalendarEntry)) {
			template.removeCalendarEntry((String)args[0], (CalendarModScope)args[1], (String)args[2]);
			return null;
		}
		
		try {
			return super.invoke(o, method, args);
		} catch(InvocationTargetException e) {
			if(e.getCause() instanceof ConstraintViolationException ve) {
				throw ve;
			}
			throw e;
		}
	}
	
	private String getId(Object entity) {
		DominoReflections reflections = CDI.current().select(DominoReflections.class).get();
		
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

		RepositoryReturn repoReturn = ServiceLoader.load(RepositoryReturn.class, RepositoryReturn.class.getClassLoader())
				.stream()
				.map(Provider<RepositoryReturn>::get)
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

	@Override
	protected Converters converters() {
		return this.converters;
	}

	@Override
	protected EntityMetadata entityMetadata() {
		return this.entityMetadata;
	}

	@Override
	protected SemiStructuredTemplate template() {
		return this.template;
	}

	@Override
	protected AbstractRepository<T, K> repository() {
		return this.repository;
	}

	@Override
	protected Class<?> repositoryType() {
		return this.repositoryType;
	}
}
