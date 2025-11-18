/**
 * Copyright (c) 2018-2025 Contributors to the XPages Jakarta EE Support Project
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
package org.openntf.xsp.jakarta.nosql.mapping.extension.impl;

import java.time.temporal.TemporalAccessor;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import org.eclipse.jnosql.communication.semistructured.CommunicationEntity;
import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.metadata.EntitiesMetadata;
import org.eclipse.jnosql.mapping.semistructured.AbstractSemiStructuredTemplate;
import org.eclipse.jnosql.mapping.semistructured.EntityConverter;
import org.eclipse.jnosql.mapping.semistructured.EventPersistManager;
import org.openntf.xsp.jakarta.nosql.communication.driver.DominoDocumentManager;
import org.openntf.xsp.jakarta.nosql.communication.driver.ViewInfo;
import org.openntf.xsp.jakarta.nosql.mapping.extension.DominoRepository.CalendarModScope;
import org.openntf.xsp.jakarta.nosql.mapping.extension.AccessRights;
import org.openntf.xsp.jakarta.nosql.mapping.extension.DominoTemplate;
import org.openntf.xsp.jakarta.nosql.mapping.extension.ViewQuery;

import jakarta.data.Sort;
import jakarta.data.page.PageRequest;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.nosql.Entity;

/**
 * Default implementation of {@link DominoTemplate}.
 *
 * @author Jesse Gallagher
 * @since 2.5.0
 */
//@Typed(DominoTemplate.class)
//@ApplicationScoped
public class DefaultDominoTemplate extends AbstractSemiStructuredTemplate implements DominoTemplate {

	private Instance<DominoDocumentManager> manager;

	private EntityConverter converter;

	private EventPersistManager persistManager;

	private EntitiesMetadata mappings;

	private Converters converters;

	@Inject
	DefaultDominoTemplate(final Instance<DominoDocumentManager> manager,
			final EntityConverter converter,
			final EventPersistManager persistManager,
			final EntitiesMetadata mappings,
			final Converters converters) {
		this.manager = manager;
		this.converter = converter;
		this.persistManager = persistManager;
		this.mappings = mappings;
		this.converters = converters;
	}

	DefaultDominoTemplate() {
	}

	@Override
	protected EntityConverter converter() {
		return converter;
	}

	@Override
	protected DominoDocumentManager manager() {
		return manager.get();
	}

	@Override
	protected EventPersistManager eventManager() {
		return persistManager;
	}

	@Override
	protected EntitiesMetadata entities() {
		return mappings;
	}

	@Override
	protected Converters converters() {
		return converters;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> Stream<T> viewEntryQuery(final String entityName, final String viewName, final PageRequest pagination, final Sort<?> sorts,
			final int maxLevel, final boolean docsOnly, final ViewQuery viewQuery, final boolean singleResult) {
		return manager()
				.viewEntryQuery(entityName, viewName, pagination, sorts, maxLevel, docsOnly, viewQuery, singleResult)
				.map(converter()::toEntity)
				.map(d -> (T) d);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> Stream<T> viewDocumentQuery(final String entityName, final String viewName, final PageRequest pagination, final Sort<?> sorts,
			final int maxLevel, final ViewQuery viewQuery, final boolean singleResult, final boolean distinct) {
		return manager()
				.viewDocumentQuery(entityName, viewName, pagination, sorts, maxLevel, viewQuery, singleResult, distinct)
				.map(converter()::toEntity)
				.map(d -> (T) d);
	}

	@Override
	public void putInFolder(final String entityId, final String folderName) {
		manager().putInFolder(entityId, folderName);
	}

	@Override
	public void removeFromFolder(final String entityId, final String folderName) {
		manager().removeFromFolder(entityId, folderName);
	}

	@Override
	public boolean existsById(final String unid) {
		return manager().existsById(unid);
	}

	@Override
	public <T> Optional<T> getByNoteId(final String entityName, final String noteId) {
		return manager().getByNoteId(entityName, noteId).map(converter()::toEntity);
	}

	@Override
	public <T, K> Optional<T> find(final Class<T> entityClass, final K id) {
		Entity entityAnnotation = entityClass.getAnnotation(Entity.class);
		String entityName = entityAnnotation == null ? "" : entityAnnotation.value(); //$NON-NLS-1$
		if (entityName.isEmpty()) {
			entityName = entityClass.getSimpleName();
		}

		return manager().getById(entityName, String.valueOf(id)).map(converter()::toEntity);
	}

	@Override
	public Stream<ViewInfo> getViewInfo() {
		return manager().getViewInfo();
	}

	@Override
	public <T> Optional<T> getByName(final String entityName, final String name, final String userName) {
		return manager().getByName(entityName, name, userName).map(converter()::toEntity);
	}

	@Override
	public <T> Optional<T> getProfileDocument(final String entityName, final String profileName, final String userName) {
		return manager().getProfileDocument(entityName, profileName, userName).map(converter()::toEntity);
	}

	@Override
	public String readCalendarRange(final TemporalAccessor start, final TemporalAccessor end, final PageRequest pagination) {
		return manager().readCalendarRange(start, end, pagination);
	}

	@Override
	public Optional<String> readCalendarEntry(final String uid) {
		return manager().readCalendarEntry(uid);
	}

	@Override
	public String createCalendarEntry(final String icalData, final boolean sendInvitations) {
		return manager().createCalendarEntry(icalData, sendInvitations);
	}

	@Override
	public void updateCalendarEntry(final String uid, final String icalData, final String comment, final boolean sendInvitations,
			final boolean overwrite, final String recurId) {
		manager().updateCalendarEntry(uid, icalData, comment, sendInvitations, overwrite, recurId);
	}

	@Override
	public void removeCalendarEntry(final String uid, final CalendarModScope scope, final String recurId) {
		manager().removeCalendarEntry(uid, scope, recurId);
	}

	@Override
	public <T> T insert(final T entity, final boolean computeWithForm) {
		return persist(entity, e -> manager().insert(e, computeWithForm));
	}

	@Override
	public <T> T update(final T entity, final boolean computeWithForm) {
		return persist(entity, e -> manager().update(e, computeWithForm));
	}
	
	@Override
	public AccessRights queryEffectiveAccess() {
		return manager().queryEffectiveAccess();
	}
	
	@Override
	public <T> T send(final T entity, boolean attachForm, boolean computeWithForm, boolean save) {
		return persist(entity, e -> manager().send(e, attachForm, computeWithForm, save));
	}

	private <T> UnaryOperator<T> toUnary(final Consumer<T> consumer) {
		return t -> {
			consumer.accept(t);
			return t;
		};
	}

	@Override
	protected <T> T persist(final T entity, final UnaryOperator<CommunicationEntity> persistAction) {
		return Stream.of(entity)
				.map(toUnary(eventManager()::firePreEntity))
				.map(converter()::toCommunication)
				.map(persistAction)
				.map(t -> converter().toEntity(entity, t))
				.map(toUnary(eventManager()::firePostEntity))
				.findFirst()
				.orElseThrow();
	}

}
