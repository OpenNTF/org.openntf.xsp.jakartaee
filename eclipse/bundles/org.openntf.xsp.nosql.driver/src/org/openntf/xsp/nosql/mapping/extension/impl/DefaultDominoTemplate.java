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

import java.time.temporal.TemporalAccessor;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import org.eclipse.jnosql.mapping.document.AbstractDocumentTemplate;
import org.eclipse.jnosql.mapping.metadata.EntitiesMetadata;
import org.openntf.xsp.nosql.communication.driver.DominoDocumentManager;
import org.openntf.xsp.nosql.communication.driver.ViewInfo;
import org.openntf.xsp.nosql.mapping.extension.DominoRepository.CalendarModScope;
import org.openntf.xsp.nosql.mapping.extension.DominoTemplate;
import org.openntf.xsp.nosql.mapping.extension.ViewQuery;

import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.inject.Inject;

import org.eclipse.jnosql.communication.document.DocumentEntity;
import org.eclipse.jnosql.mapping.Converters;
import jakarta.nosql.Entity;
import jakarta.data.repository.Pageable;
import jakarta.data.repository.Sort;
import org.eclipse.jnosql.mapping.document.DocumentEntityConverter;
import org.eclipse.jnosql.mapping.document.DocumentEventPersistManager;

/**
 * Default implementation of {@link DominoTemplate}.
 * 
 * @author Jesse Gallagher
 * @since 2.5.0
 */
public class DefaultDominoTemplate extends AbstractDocumentTemplate implements DominoTemplate {

	private Instance<DominoDocumentManager> manager;

    private DocumentEntityConverter converter;

    private DocumentEventPersistManager persistManager;

    private EntitiesMetadata mappings;

    private Converters converters;

    @Inject
    DefaultDominoTemplate(Instance<DominoDocumentManager> manager,
                             DocumentEntityConverter converter,
                             DocumentEventPersistManager persistManager,
                             EntitiesMetadata mappings,
                             Converters converters) {
        this.manager = manager;
        this.converter = converter;
        this.persistManager = persistManager;
        this.mappings = mappings;
        this.converters = converters;
    }

    DefaultDominoTemplate() {
    }

    @Override
    protected DocumentEntityConverter getConverter() {
		DocumentEntityConverter converter = this.converter == null ? CDI.current().select(DocumentEntityConverter.class).get() : this.converter;
		Objects.requireNonNull(converter, "Unable to acquire DocumentEntityConverter");
        return converter;
    }

    @Override
    protected DominoDocumentManager getManager() {
        return manager.get();
    }

    @Override
    protected DocumentEventPersistManager getEventManager() {
    	return persistManager;
    }

    @Override
    protected EntitiesMetadata getEntities() {
    	return mappings;
    }

    @Override
    protected Converters getConverters() {
        return converters;
    }

	@SuppressWarnings("unchecked")
	@Override
	public <T> Stream<T> viewEntryQuery(String entityName, String viewName, Pageable pagination, Sort sorts, int maxLevel, boolean docsOnly, ViewQuery viewQuery, boolean singleResult) {
		return getManager().viewEntryQuery(entityName, viewName, pagination, sorts, maxLevel, docsOnly, viewQuery, singleResult)
			.map(getConverter()::toEntity)
			.map(d -> (T)d);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> Stream<T> viewDocumentQuery(String entityName, String viewName, Pageable pagination, Sort sorts, int maxLevel, ViewQuery viewQuery, boolean singleResult, boolean distinct) {
		return getManager().viewDocumentQuery(entityName, viewName, pagination, sorts, maxLevel, viewQuery, singleResult, distinct)
			.map(getConverter()::toEntity)
			.map(d -> (T)d);
	}
	
	@Override
	public void putInFolder(String entityId, String folderName) {
		getManager().putInFolder(entityId, folderName);
	}
	
	@Override
	public void removeFromFolder(String entityId, String folderName) {
		getManager().removeFromFolder(entityId, folderName);
	}
	
	@Override
	public boolean existsById(String unid) {
		return getManager().existsById(unid);
	}
	
	@Override
	public <T> Optional<T> getByNoteId(String entityName, String noteId) {
		return getManager().getByNoteId(entityName, noteId).map(getConverter()::toEntity);
	}
	
	@Override
	public <T, K> Optional<T> find(Class<T> entityClass, K id) {
		Entity entityAnnotation = entityClass.getAnnotation(Entity.class);
		String entityName = entityAnnotation == null ? "" : entityAnnotation.value(); //$NON-NLS-1$
		if(entityName.isEmpty()) {
			entityName = entityClass.getSimpleName();
		}
		
		return getManager().getById(entityName, String.valueOf(id)).map(getConverter()::toEntity);
	}
	
	@Override
	public Stream<ViewInfo> getViewInfo() {
		return getManager().getViewInfo();
	}
	
	@Override
	public <T> Optional<T> getByName(String entityName, String name, String userName) {
		return getManager().getByName(entityName, name, userName).map(getConverter()::toEntity);
	}
	
	@Override
	public <T> Optional<T> getProfileDocument(String entityName, String profileName, String userName) {
		return getManager().getProfileDocument(entityName, profileName, userName).map(getConverter()::toEntity);
	}

	@Override
	public String readCalendarRange(TemporalAccessor start, TemporalAccessor end, Pageable pagination) {
		return getManager().readCalendarRange(start, end, pagination);
	}
	
	@Override
	public Optional<String> readCalendarEntry(String uid) {
		return getManager().readCalendarEntry(uid);
	}

	@Override
	public String createCalendarEntry(String icalData, boolean sendInvitations) {
		return getManager().createCalendarEntry(icalData, sendInvitations);
	}

	@Override
	public void updateCalendarEntry(String uid, String icalData, String comment, boolean sendInvitations,
			boolean overwrite, String recurId) {
		getManager().updateCalendarEntry(uid, icalData, comment, sendInvitations, overwrite, recurId);
	}

	@Override
	public void removeCalendarEntry(String uid, CalendarModScope scope, String recurId) {
		getManager().removeCalendarEntry(uid, scope, recurId);
	}

	@Override
	public <T> T insert(T entity, boolean computeWithForm) {
		return persist(entity, e -> getManager().insert(e, computeWithForm));
	}

	@Override
	public <T> T update(T entity, boolean computeWithForm) {
		return persist(entity, e -> getManager().update(e, computeWithForm));
	}
	
    private <T> UnaryOperator<T> toUnary(Consumer<T> consumer) {
        return t -> {
            consumer.accept(t);
            return t;
        };
    }
	
	protected <T> T persist(T entity, UnaryOperator<DocumentEntity> persistAction) {
        return Stream.of(entity)
            .map(toUnary(getEventManager()::firePreEntity))
            .map(getConverter()::toDocument)
            .map(persistAction)
            .map(t -> getConverter().toEntity(entity, t))
            .map(toUnary(getEventManager()::firePostEntity))
            .findFirst()
            .orElseThrow();
    }

}
