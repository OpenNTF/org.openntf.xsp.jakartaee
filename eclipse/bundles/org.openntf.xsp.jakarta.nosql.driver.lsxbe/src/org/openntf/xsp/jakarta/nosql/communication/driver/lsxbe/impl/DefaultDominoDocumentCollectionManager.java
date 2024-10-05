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
package org.openntf.xsp.jakarta.nosql.communication.driver.lsxbe.impl;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.time.temporal.TemporalAccessor;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.eclipse.jnosql.mapping.metadata.EntityMetadata;
import org.openntf.xsp.jakarta.nosql.communication.driver.DominoConstants;
import org.openntf.xsp.jakarta.nosql.communication.driver.ViewColumnInfo;
import org.openntf.xsp.jakarta.nosql.communication.driver.ViewInfo;
import org.openntf.xsp.jakarta.nosql.communication.driver.impl.AbstractDominoDocumentCollectionManager;
import org.openntf.xsp.jakarta.nosql.communication.driver.impl.AbstractEntityConverter;
import org.openntf.xsp.jakarta.nosql.communication.driver.impl.DQL;
import org.openntf.xsp.jakarta.nosql.communication.driver.impl.EntityUtil;
import org.openntf.xsp.jakarta.nosql.communication.driver.impl.QueryConverter;
import org.openntf.xsp.jakarta.nosql.communication.driver.impl.ViewColumnInfoImpl;
import org.openntf.xsp.jakarta.nosql.communication.driver.impl.ViewInfoImpl;
import org.openntf.xsp.jakarta.nosql.communication.driver.impl.DQL.DQLTerm;
import org.openntf.xsp.jakarta.nosql.communication.driver.impl.QueryConverter.QueryConverterResult;
import org.openntf.xsp.jakarta.nosql.communication.driver.lsxbe.DatabaseSupplier;
import org.openntf.xsp.jakarta.nosql.communication.driver.lsxbe.SessionSupplier;
import org.openntf.xsp.jakarta.nosql.communication.driver.lsxbe.util.DominoNoSQLUtil;
import org.openntf.xsp.jakarta.nosql.mapping.extension.FTSearchOption;
import org.openntf.xsp.jakarta.nosql.mapping.extension.ViewQuery;
import org.openntf.xsp.jakarta.nosql.mapping.extension.DominoRepository.CalendarModScope;

import com.ibm.commons.util.StringUtil;
import com.ibm.designer.domino.napi.NotesAPIException;
import com.ibm.designer.domino.napi.NotesSession;

import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.data.Sort;
import org.eclipse.jnosql.communication.semistructured.CommunicationEntity;
import org.eclipse.jnosql.communication.semistructured.DeleteQuery;
import org.eclipse.jnosql.communication.semistructured.Element;
import org.eclipse.jnosql.communication.semistructured.SelectQuery;

import jakarta.nosql.Column;
import jakarta.data.page.PageRequest;
import jakarta.transaction.RollbackException;
import jakarta.transaction.Status;
import jakarta.transaction.SystemException;
import jakarta.transaction.Transaction;
import jakarta.transaction.TransactionManager;
import lotus.domino.ACL;
import lotus.domino.ACLEntry;
import lotus.domino.Base;
import lotus.domino.Database;
import lotus.domino.DateTime;
import lotus.domino.DbDirectory;
import lotus.domino.DocumentCollection;
import lotus.domino.DominoQuery;
import lotus.domino.NotesCalendar;
import lotus.domino.NotesCalendarEntry;
import lotus.domino.NotesException;
import lotus.domino.QueryResultsProcessor;
import lotus.domino.Session;
import lotus.domino.View;
import lotus.domino.ViewColumn;
import lotus.domino.ViewEntry;
import lotus.domino.ViewEntryCollection;
import lotus.domino.ViewNavigator;

public class DefaultDominoDocumentCollectionManager extends AbstractDominoDocumentCollectionManager {
	private final Logger log = Logger.getLogger(DefaultDominoDocumentCollectionManager.class.getName());
	
	private final DatabaseSupplier supplier;
	private final SessionSupplier sessionSupplier;
	private final LSXBEEntityConverter entityConverter;
	
	public DefaultDominoDocumentCollectionManager(DatabaseSupplier supplier, SessionSupplier sessionSupplier) {
		this.supplier = supplier;
		this.sessionSupplier = sessionSupplier;
		this.entityConverter = new LSXBEEntityConverter(supplier);
	}

	@Override
	public String name() {
		return getClass().getName();
	}
	
	/**
	 * @since 2.6.0
	 */
	@Override
	public CommunicationEntity insert(CommunicationEntity entity, boolean computeWithForm) {
		try {
			Database database = supplier.get();
			beginTransaction(database);
			
			// Special handling for named and profile notes
			lotus.domino.Document target;
			Optional<Element> maybeName = entity.find(DominoConstants.FIELD_NOTENAME);
			Optional<Element> maybeProfileName = entity.find(DominoConstants.FIELD_PROFILENAME);
			if(maybeName.isPresent() && StringUtil.isNotEmpty(maybeName.get().get(String.class))) {
				Optional<Element> maybeUserName = entity.find(DominoConstants.FIELD_USERNAME);
				if(maybeUserName.isPresent() && StringUtil.isNotEmpty(maybeUserName.get().get(String.class))) {
					target = database.getNamedDocument(maybeName.get().get(String.class), maybeUserName.get().get(String.class));
				} else {
					target = database.getNamedDocument(maybeName.get().get(String.class));
				}
			} else if(maybeProfileName.isPresent() && StringUtil.isNotEmpty(maybeProfileName.get().get(String.class))) {
				Optional<Element> maybeUserName = entity.find(DominoConstants.FIELD_PROFILEKEY);
				target = database.getProfileDocument(maybeProfileName.get().get(String.class), maybeUserName.map(d -> d.get(String.class)).orElse(null));
			} else {
				target = database.createDocument();
			}
			
			Optional<String> maybeId = entity.find(DominoConstants.FIELD_ID, String.class);
			if(maybeId.isPresent() && !StringUtil.isEmpty(maybeId.get())) {
				target.setUniversalID(maybeId.get());
			} else {
				// Write the generated UNID into the entity
				entity.add(Element.of(DominoConstants.FIELD_ID, target.getUniversalID()));
			}

			EntityMetadata mapping = EntityUtil.getClassMapping(entity.name());
			entityConverter.convertNoSQLEntity(entity, false, target, mapping);
			if(computeWithForm) {
				target.computeWithForm(false, false);
			}
			target.save();
			return entity;
		} catch(NotesException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Iterable<CommunicationEntity> insert(Iterable<CommunicationEntity> entities) {
		if(entities == null) {
			return Collections.emptySet();
		} else {
			return StreamSupport.stream(entities.spliterator(), false)
				.map(this::insert)
				.collect(Collectors.toList());
		}
	}

	@Override
	public CommunicationEntity update(CommunicationEntity entity, boolean computeWithForm) {
		try {
			Database database = supplier.get();
			beginTransaction(database);
			
			Element id = entity.find(DominoConstants.FIELD_ID)
				.orElseThrow(() -> new IllegalArgumentException(MessageFormat.format("Unable to find {0} in entity", DominoConstants.FIELD_ID)));
			
			lotus.domino.Document target = database.getDocumentByUNID((String)id.get());

			EntityMetadata mapping = EntityUtil.getClassMapping(entity.name());
			entityConverter.convertNoSQLEntity(entity, false, target, mapping);
			if(computeWithForm) {
				target.computeWithForm(false, false);
			}
			target.save();
			return entity;
		} catch(NotesException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void delete(DeleteQuery query) {
		try {
			Database database = supplier.get();
			beginTransaction(database);
			List<String> unids = query.columns();
			if(unids != null && !unids.isEmpty()) {
				for(String unid : unids) {
					if(unid != null && !unid.isEmpty()) {
						lotus.domino.Document doc = database.getDocumentByUNID(unid);
						doc.remove(true);
					}
				}
			} else if(query.condition().isPresent()) {
				// Then do it via DQL
				DQLTerm dql = QueryConverter.getCondition(query.condition().get());
				DominoQuery dominoQuery = database.createDominoQuery();
				DocumentCollection docs = dominoQuery.execute(dql.toString());
				docs.removeAll(true);
			}
		} catch(NotesException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Stream<CommunicationEntity> select(SelectQuery query) {
		try {
			String entityName = query.name();
			EntityMetadata mapping = EntityUtil.getClassMapping(entityName);
			
			QueryConverterResult queryResult = QueryConverter.select(query);
			
			long skip = queryResult.getSkip();
			long limit = queryResult.getLimit();
			List<Sort<?>> sorts = query.sorts();
			Stream<CommunicationEntity> result;

			Database database = supplier.get();
			beginTransaction(database);
			if(sorts != null && !sorts.isEmpty()) {
				Session sessionAsSigner = sessionSupplier.get();
				Database qrpDatabase = getQrpDatabase(sessionAsSigner, database);

				String userName = database.getParent().getEffectiveUserName();
				String dqlQuery = queryResult.getStatement().toString();
				String viewName = getClass().getName() + "-" + Objects.hash(sorts, userName, dqlQuery); //$NON-NLS-1$
				View view = qrpDatabase.getView(viewName);
				if(view != null) {
					// Check to see if we need to "expire" it based on the data mod time of the DB
					DateTime created = view.getCreated();
					try {
						// Skip using the server name when it's local, as that can cause resolution trouble
						//   if the server doesn't know it's itself (Issue #461)
						String serverName = database.getServer();
						if(StringUtil.equals(database.getParent().getUserName(), serverName)) {
							serverName = ""; //$NON-NLS-1$
						}
						long dataMod = NotesSession.getLastDataModificationDateByName(serverName, database.getFilePath());
						if(dataMod > (created.toJavaDate().getTime() / 1000)) {
							view.remove();
							view.recycle();
							view = null;
						}
					} catch (NotesAPIException e) {
						throw new RuntimeException(e);
					} finally {
						recycle(created);
					}
				}

				if(view != null) {
					result = entityConverter.convertQRPViewDocuments(database, view, mapping);
				} else {
					DominoQuery dominoQuery = database.createDominoQuery();
					QueryResultsProcessor qrp = qrpDatabase.createQueryResultsProcessor();
					try {
						qrp.addDominoQuery(dominoQuery, dqlQuery, null);
						for(Sort sort : sorts) {
							String itemName = EntityUtil.findItemName(sort.property(), mapping);
							
							int dir = sort.isDescending() ? QueryResultsProcessor.SORT_DESCENDING : QueryResultsProcessor.SORT_ASCENDING;
							qrp.addColumn(itemName, itemName, null, dir, false, false);
						}
						
						view = qrp.executeToView(viewName, 24);
						result = entityConverter.convertQRPViewDocuments(database, view, mapping);
					} finally {
						recycle(qrp, dominoQuery);
					}
				}
				
			} else {
				DominoQuery dominoQuery = database.createDominoQuery();
				DocumentCollection docs = dominoQuery.execute(queryResult.getStatement().toString());
				try {
					result = entityConverter.convertDocuments(docs, mapping);
				} finally {
					recycle(dominoQuery);
				}
			}
			
			if(skip > 0) {
				result = result.skip(skip);
			}
			if(limit > 0) {
				result = result.limit(limit);
			}
			
			return result;
		} catch(NotesException e) {
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public Stream<CommunicationEntity> viewEntryQuery(String entityName, String viewName, PageRequest pagination,
			Sort sorts, int maxLevel, boolean docsOnly, ViewQuery viewQuery, boolean singleResult) {
		EntityMetadata mapping = EntityUtil.getClassMapping(entityName);
		
		if(viewQuery != null && viewQuery.getKey() != null && singleResult) {
			try {
				Database database = supplier.get();
				beginTransaction(database);
				View view = database.getView(viewName);
				Objects.requireNonNull(view, () -> "Unable to open view: " + viewName);

				Object keys = DominoNoSQLUtil.toDominoFriendly(database.getParent(), viewQuery.getKey(), Optional.empty());
				Vector<Object> vecKeys;
				if(keys instanceof List) {
					vecKeys = (Vector<Object>)keys;
				} else {
					vecKeys = new Vector<>(Arrays.asList(keys));
				}
				ViewEntry entry = view.getEntryByKey(vecKeys, viewQuery.isExact());
				if(entry == null) {
					return Stream.empty();
				}
				return Stream.of(entityConverter.convertViewEntry(entityName, entry, mapping));
			} catch(NotesException e) {
				throw new RuntimeException(e);
			}
		}
		
		return buildNavigtor(viewName, pagination, sorts, maxLevel, viewQuery, singleResult, mapping,
			(nav, limit, didSkip, didKey) -> {
				try {
					if(nav instanceof ViewNavigator) {
						return entityConverter.convertViewEntries(entityName, (ViewNavigator)nav, didSkip, didKey, limit, docsOnly, mapping);
					} else if(nav instanceof ViewEntryCollection) {
						return entityConverter.convertViewEntries(entityName, (ViewEntryCollection)nav, didSkip, didKey, limit, docsOnly, mapping);
					} else {
						throw new IllegalStateException("Cannot process " + nav);
					}
				} catch (NotesException e) {
					throw new RuntimeException(e);
				}
			}
		);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Stream<CommunicationEntity> viewDocumentQuery(String entityName, String viewName, PageRequest pagination,
			Sort sorts, int maxLevel, ViewQuery viewQuery, boolean singleResult, boolean distinct) {
		EntityMetadata mapping = EntityUtil.getClassMapping(entityName);
		
		if(viewQuery != null && viewQuery.getKey() != null && singleResult) {
			try {
				Database database = supplier.get();
				beginTransaction(database);
				View view = database.getView(viewName);
				Objects.requireNonNull(view, () -> "Unable to open view: " + viewName);
				
				Object keys = DominoNoSQLUtil.toDominoFriendly(database.getParent(), viewQuery.getKey(), Optional.empty());
				Vector<Object> vecKeys;
				if(keys instanceof List) {
					vecKeys = (Vector<Object>)keys;
				} else {
					vecKeys = new Vector<>(Arrays.asList(keys));
				}
				lotus.domino.Document doc = view.getDocumentByKey(vecKeys, viewQuery.isExact());
				if(doc == null) {
					return Stream.empty();
				}
				Map<String, Class<?>> itemTypes = EntityUtil.getItemTypes(mapping);
				return Stream.of(CommunicationEntity.of(entityName, entityConverter.convertDominoDocument(doc, mapping, itemTypes)));
			} catch(NotesException e) {
				throw new RuntimeException(e);
			}
		}
		
		return buildNavigtor(viewName, pagination, sorts, maxLevel, viewQuery, singleResult, mapping,
			(nav, limit, didSkip, didKey) -> {
				try {
					if(nav instanceof ViewNavigator) {
						return entityConverter.convertViewDocuments(entityName, (ViewNavigator)nav, didSkip, didKey, limit, distinct, mapping);
					} else if(nav instanceof ViewEntryCollection) {
						return entityConverter.convertViewDocuments(entityName, (ViewEntryCollection)nav, didSkip, didKey, limit, distinct, mapping);
					} else {
						throw new IllegalStateException("Cannot process " + nav);
					}
				} catch (NotesException e) {
					throw new RuntimeException(e);
				}
			}
		);
	}
	
	@Override
	public void putInFolder(String entityId, String folderName) {
		if(StringUtil.isEmpty(entityId)) {
			throw new IllegalArgumentException("entityId cannot be empty");
		}
		if(StringUtil.isEmpty(folderName)) {
			throw new IllegalArgumentException("folderName cannot be empty");
		}
		
		Database database = supplier.get();
		beginTransaction(database);
		try {
			lotus.domino.Document doc = database.getDocumentByUNID(entityId);
			if(doc != null) {
				doc.putInFolder(folderName);
			}
		} catch(NotesException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public void removeFromFolder(String entityId, String folderName) {
		if(StringUtil.isEmpty(entityId)) {
			// No harm here
			return;
		}
		if(StringUtil.isEmpty(folderName)) {
			throw new IllegalArgumentException("folderName cannot be empty");
		}
		
		Database database = supplier.get();
		beginTransaction(database);
		try {
			lotus.domino.Document doc = database.getDocumentByUNID(entityId);
			if(doc != null) {
				doc.removeFromFolder(folderName);
			}
		} catch(NotesException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public long count(String documentCollection) {
		try {
			Database database = supplier.get();
			beginTransaction(database);
			DominoQuery dominoQuery = database.createDominoQuery();
			DQLTerm dql = DQL.item(DominoConstants.FIELD_NAME).isEqualTo(documentCollection);
			DocumentCollection result = dominoQuery.execute(dql.toString());
			return result.getCount();
		} catch(NotesException e) {
			throw new RuntimeException(e);
		}
		
	}
	
	@Override
	public boolean existsById(String unid) {
		try {
			Database database = supplier.get();
			beginTransaction(database);
			lotus.domino.Document doc = database.getDocumentByUNID(unid);
			// TODO consider checking the form
			return doc != null;
		} catch(NotesException e) {
			// Assume it doesn't exist
			return false;
		}
	}
	
	@Override
	public Optional<CommunicationEntity> getByNoteId(String entityName, String noteId) {
		try {
			Database database = supplier.get();
			beginTransaction(database);
			lotus.domino.Document doc = database.getDocumentByID(noteId);
			return processDocument(entityName, doc);
		} catch(NotesException e) {
			// Assume it doesn't exist
			return Optional.empty();
		}
	}
	
	@Override
	public Optional<CommunicationEntity> getById(String entityName, String id) {
		try {
			Database database = supplier.get();
			beginTransaction(database);
			lotus.domino.Document doc = database.getDocumentByUNID(id);
			return processDocument(entityName, doc);
		} catch(NotesException e) {
			// Assume it doesn't exist
			return Optional.empty();
		}
	}
	
	@Override
	public Optional<CommunicationEntity> getByName(String entityName, String name, String userName) {
		try {
			Database database = supplier.get();
			beginTransaction(database);
			lotus.domino.Document doc;
			if(StringUtil.isEmpty(userName)) {
				doc = database.getNamedDocument(name);
			} else {
				doc = database.getNamedDocument(name, userName);
			}
			return processDocument(entityName, doc);
		} catch(NotesException e) {
			// Assume it doesn't exist
			return Optional.empty();
		}
	}
	
	@Override
	public Optional<CommunicationEntity> getProfileDocument(String entityName, String profileName, String userName) {
		try {
			Database database = supplier.get();
			beginTransaction(database);
			lotus.domino.Document doc = database.getProfileDocument(profileName, userName);
			return processDocument(entityName, doc);
		} catch(NotesException e) {
			// Assume it doesn't exist
			return Optional.empty();
		}
	}
	
	@Override
	public Stream<ViewInfo> getViewInfo() {
		try {
			Database database = supplier.get();
			@SuppressWarnings("unchecked")
			List<View> views = database.getViews();
			return views.stream()
				.map(view -> {
					try {
						ViewInfo.Type type = view.isFolder() ? ViewInfo.Type.FOLDER : ViewInfo.Type.VIEW;
						String title = view.getName();
						@SuppressWarnings("unchecked")
						List<String> aliases = view.getAliases();
						String unid = view.getUniversalID();
						String selectionFormula = view.getSelectionFormula();
						
						@SuppressWarnings("unchecked")
						Vector<ViewColumn> columns = view.getColumns();
						List<ViewColumnInfo> columnInfo = columns.stream()
							.map(column -> {
								try {
									String columnTitle = column.getTitle();
									String itemName = column.getItemName();
									ViewColumnInfo.SortOrder sortOrder = column.isSorted() ?
										(
											column.isSortDescending() ? ViewColumnInfo.SortOrder.DESCENDING :
											ViewColumnInfo.SortOrder.ASCENDING
										) :
										ViewColumnInfo.SortOrder.NONE;
									Collection<ViewColumnInfo.SortOrder> resortOrders = EnumSet.noneOf(ViewColumnInfo.SortOrder.class);
									if(column.isResortAscending()) {
										resortOrders.add(ViewColumnInfo.SortOrder.ASCENDING);
									}
									if(column.isResortDescending()) {
										resortOrders.add(ViewColumnInfo.SortOrder.DESCENDING);
									}
									boolean categorized = column.isCategory();
									
									return new ViewColumnInfoImpl(columnTitle, itemName, sortOrder, resortOrders, categorized);
										
								} catch(NotesException e) {
									throw new RuntimeException(e);
								}
							})
							.collect(Collectors.toList());
						view.recycle(columns);
						
						return new ViewInfoImpl(type, title, aliases, unid, selectionFormula, columnInfo);
					} catch(NotesException e) {
						throw new RuntimeException(e);
					} finally {
						try {
							view.recycle();
						} catch(NotesException e) {
							// ignore
						}
					}
				});
		} catch(NotesException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String readCalendarRange(TemporalAccessor start, TemporalAccessor end, PageRequest pagination) {
		try {
			Database database = supplier.get();
			Session session = database.getParent();
			NotesCalendar cal = session.getCalendar(database);
			
			DateTime startDt = DominoNoSQLUtil.fromTemporal(session, start);
			DateTime endDt = DominoNoSQLUtil.fromTemporal(session, end);
			
			if(pagination != null) {
				return cal.readRange(startDt, endDt, (int)(pagination.size() * (pagination.page()-1)), pagination.size());
			} else {
				return cal.readRange(startDt, endDt);
			}
		} catch(NotesException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public Optional<String> readCalendarEntry(String uid) {
		try {
			Database database = supplier.get();
			Session session = database.getParent();
			NotesCalendar cal = session.getCalendar(database);
			
			NotesCalendarEntry entry = cal.getEntry(uid);
			try {
				return Optional.of(entry.read());
			} catch(NotesException e) {
				// Accessor methods throw exceptions when the entry doesn't exist
				return Optional.empty();
			}
		} catch(NotesException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String createCalendarEntry(String icalData, boolean sendInvitations) {
		try {
			Database database = supplier.get();
			Session session = database.getParent();
			NotesCalendar cal = session.getCalendar(database);
			
			NotesCalendarEntry entry = cal.createEntry(icalData, sendInvitations ? 0 : NotesCalendar.CS_WRITE_DISABLE_IMPLICIT_SCHEDULING);
			return entry.getUID();
		} catch(NotesException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void updateCalendarEntry(String uid, String icalData, String comment, boolean sendInvitations,
			boolean overwrite, String recurId) {
		try {
			Database database = supplier.get();
			Session session = database.getParent();
			NotesCalendar cal = session.getCalendar(database);
			
			NotesCalendarEntry entry = cal.getEntry(uid);
			int flags = (sendInvitations ? 0 : NotesCalendar.CS_WRITE_DISABLE_IMPLICIT_SCHEDULING) | (overwrite ? NotesCalendar.CS_WRITE_MODIFY_LITERAL : 0);
			if(StringUtil.isEmpty(recurId)) {
				entry.update(icalData, comment, flags);
			} else {
				entry.update(icalData, comment, flags, recurId);
			}
		} catch(NotesException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void removeCalendarEntry(String uid, CalendarModScope scope, String recurId) {
		try {
			Database database = supplier.get();
			Session session = database.getParent();
			NotesCalendar cal = session.getCalendar(database);
			
			NotesCalendarEntry entry = cal.getEntry(uid);
			if(StringUtil.isEmpty(recurId)) {
				entry.remove();
			} else {
				int scopeVal;
				if(scope == null) {
					scopeVal = 0;
				} else {
					scopeVal = switch (scope) {
						case ALL -> 1;
						case FUTURE -> 3;
						case PREV -> 2;
						case CURRENT -> 0;
						default -> 0;
					};
				}
				entry.remove(scopeVal, recurId);
			}
		} catch(NotesException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void close() {
	}
	
	// *******************************************************************************
	// * Internal implementation utilities
	// *******************************************************************************
	
	@SuppressWarnings("unchecked")
	private <T> T buildNavigtor(String viewName, PageRequest pagination, Sort<?> sorts, int maxLevel, ViewQuery viewQuery, boolean singleResult, EntityMetadata mapping, NavFunction<T> consumer) {
		try {
			if(StringUtil.isEmpty(viewName)) {
				throw new IllegalArgumentException("viewName cannot be empty");
			}
			
			Database database = supplier.get();
			beginTransaction(database);
			View view = database.getView(viewName);
			Objects.requireNonNull(view, () -> "Unable to open view: " + viewName);
			view.setAutoUpdate(false);
			applySorts(view, sorts, mapping, viewQuery, pagination);
			
			long limit = 0;
			boolean didSkip = false;
			long skip = 0;
			if(pagination != null) {
				skip = (pagination.size() * (pagination.page()-1));
				limit = pagination.size();
				
				if(skip > Integer.MAX_VALUE) {
					throw new UnsupportedOperationException("Domino does not support skipping more than Integer.MAX_VALUE entries");
				}
				if(skip > 0) {
					didSkip = true;
				}
			}

			// If we did an FT search, then we can't use a view navigator
			if(viewQuery != null && !viewQuery.getFtSearch().isEmpty()) {
				ViewEntryCollection entries = view.getAllEntries();
				if(skip > 0) {
					int n = Math.min((int)skip, entries.getCount());
					// Downstream code will call getNext, so position one before
					// NB: not recycling is intentional, as doing so un-does the skip
					entries.getNthEntry(n); 
				}
				return consumer.apply(entries, limit, didSkip, false);
			}
			
			// Override anything above if we were told to get a single view entry
			if(viewQuery != null && viewQuery.getKey() != null) {
				if(singleResult) {
					limit = 1;
				}
			}
			
			boolean didKey = false;
			ViewNavigator nav;
			String category = viewQuery == null ? null : viewQuery.getCategory();
			if(category == null) {
				if(viewQuery != null && viewQuery.getKey() != null) {
					Object keys = DominoNoSQLUtil.toDominoFriendly(database.getParent(), viewQuery.getKey(), Optional.empty());
					Vector<Object> vecKeys;
					if(keys instanceof List) {
						vecKeys = (Vector<Object>)keys;
					} else {
						vecKeys = new Vector<>(Arrays.asList(keys));
					}
					nav = view.createViewNavFromKey(vecKeys, viewQuery.isExact());
					didKey = true;
				} else {
					nav = view.createViewNav();
				}
			} else {
				nav = view.createViewNavFromCategory(category);
			}
			
			// Check if the class requests count data and skip reading if not
			boolean requestsCounts = mapping.fields()
				.stream()
				.map(EntityUtil::getNativeField)
				.map(f -> f.getAnnotation(Column.class))
				.filter(Objects::nonNull)
				.map(col -> col.value())
				.anyMatch(name ->
					DominoConstants.FIELD_CHILDCOUNT.equals(name)
					|| DominoConstants.FIELD_SIBLINGCOUNT.equals(name)
					|| DominoConstants.FIELD_DESCENDANTCOUNT.equals(name)
				);
			if(!requestsCounts) {
				nav.setEntryOptions(ViewNavigator.VN_ENTRYOPT_NOCOUNTDATA);
			}
			
			
			if(maxLevel > -1) {
				nav.setMaxLevel(maxLevel);
			}
			
			if(skip > 0) {
				nav.skip((int)skip-1);
			}
			
			// Override anything above if we were told to get a single view entry
			if(viewQuery != null && viewQuery.getKey() != null) {
				if(singleResult) {
					limit = 1;
				}
			}
			
			if(limit > 0) {
				nav.setBufferMaxEntries((int)Math.min(400, limit));
			} else {
				nav.setBufferMaxEntries(400);
			}
			
			return consumer.apply(nav, limit, didSkip, didKey);
		} catch(NotesException e) {
			throw new RuntimeException(e);
		}
	}
	
	private Database getQrpDatabase(Session session, Database database) throws NotesException {
		String server = database.getServer();
		String filePath = database.getFilePath();

		try {
			String fileName = AbstractEntityConverter.md5(server + filePath) + ".nsf"; //$NON-NLS-1$
			
			Path dest = DominoNoSQLUtil.getQrpDirectory()
				.orElseGet(() -> DominoNoSQLUtil.getTempDirectory().resolve(getClass().getPackageName()));
			Files.createDirectories(dest);
			Path dbPath = dest.resolve(fileName);
			
			Database qrp = session.getDatabase("", dbPath.toString()); //$NON-NLS-1$
			if(!qrp.isOpen()) {
				qrp.recycle();
				DbDirectory dbDir = session.getDbDirectory(null);
				// TODO encrypt when the API allows
				qrp = dbDir.createDatabase(dbPath.toString(), true);
				
				ACL acl = qrp.getACL();
				ACLEntry entry = acl.createACLEntry(session.getEffectiveUserName(), ACL.LEVEL_MANAGER);
				entry.setCanDeleteDocuments(true);
				acl.save();
			}
			return qrp;
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		
	}

	private static void recycle(Object... objects) {
		for(Object obj : objects) {
			if(obj instanceof Base) {
				try {
					((Base)obj).recycle();
				} catch (NotesException e) {
					// Ignore
				}
			}
		}
	}

	private Optional<CommunicationEntity> processDocument(String entityName, lotus.domino.Document doc) throws NotesException {
		if(doc != null) {
			if(doc.isDeleted()) {
				return Optional.empty();
			} else if(!doc.isValid()) {
				return Optional.empty();
			}
			String unid = doc.getUniversalID();
			if(unid == null || unid.isEmpty()) {
				return Optional.empty();
			}
			
			// TODO consider checking the form
			EntityMetadata EntityMetadata = EntityUtil.getClassMapping(entityName);
			Map<String, Class<?>> itemTypes = EntityUtil.getItemTypes(EntityMetadata);
			List<Element> result = entityConverter.convertDominoDocument(doc, EntityMetadata, itemTypes);
			return Optional.of(CommunicationEntity.of(entityName, result));
		} else {
			return Optional.empty();
		}
	}
	
	private static void applySorts(View view, Sort<?> sorts, EntityMetadata mapping, ViewQuery query, PageRequest pagination) throws NotesException {
		Collection<String> ftSearch = query == null ? Collections.emptySet() : query.getFtSearch();
		Collection<FTSearchOption> options = query == null ? Collections.emptySet() : query.getFtSearchOptions();
		
		if(sorts == null) {
			if(ftSearch != null && !ftSearch.isEmpty()) {
				if(options.contains(FTSearchOption.UPDATE_INDEX)) {
					view.getParent().updateFTIndex(true);
				}
				boolean exact = options.contains(FTSearchOption.EXACT);
				boolean variants = options.contains(FTSearchOption.VARIANTS);
				boolean fuzzy = options.contains(FTSearchOption.FUZZY);
				int maxDocs = 0;
				if(pagination != null) {
					maxDocs = (int)Math.min(pagination.size() * pagination.page() + pagination.size(), Integer.MAX_VALUE);
				}

				// Using View.VIEW_FTSS_RELEVANCE_ORDER causes trouble sometimes in some
				//   compilation environments, so use the raw integer value 512
				view.FTSearchSorted(new Vector<>(ftSearch), maxDocs, 512, true, exact, variants, fuzzy);
			}
			
			return;
		} else {
			String itemName = EntityUtil.findItemName(sorts.property(), mapping);
			
			if(ftSearch != null && !ftSearch.isEmpty()) {
				if(options.contains(FTSearchOption.UPDATE_INDEX)) {
					view.getParent().updateFTIndex(true);
				}
				boolean exact = options.contains(FTSearchOption.EXACT);
				boolean variants = options.contains(FTSearchOption.VARIANTS);
				boolean fuzzy = options.contains(FTSearchOption.FUZZY);
				int maxDocs = 0;
				if(pagination != null) {
					maxDocs = (int)Math.min(pagination.size() * pagination.page() + pagination.size(), Integer.MAX_VALUE);
				}
				view.FTSearchSorted(new Vector<>(ftSearch), maxDocs, itemName, sorts.isAscending(), exact, variants, fuzzy);
			} else {
				view.resortView(itemName, sorts.isAscending());
			}
		}
	}
	
	private void beginTransaction(Database database) {
		if(transactionsAvailable) {
			Instance<TransactionManager> tm = CDI.current().select(TransactionManager.class);
			if(tm.isResolvable()) {
				Transaction t = null;
				try {
					t = tm.get().getTransaction();
				} catch (SystemException e) {
					if(log.isLoggable(Level.SEVERE)) {
						log.log(Level.SEVERE, "Encountered unexpected exception retrieving active transaction", e);
					}
					return;
				}
				if(t != null) {
					DatabaseXAResource res = null;
					try {
						if(t.getStatus() != Status.STATUS_ACTIVE) {
							// Ignore softly
							// TODO determine if this should throw an exception in other states
							return;
						}
						
						res = new DatabaseXAResource(database);
						if(t.enlistResource(res)) {
							// Only begin a DB transaction if there wasn't already a transaction
							//  for it
							database.transactionBegin();
						}
						
						
					} catch (IllegalStateException | RollbackException | SystemException | NotesException e) {
						if(log.isLoggable(Level.SEVERE)) {
							if(e instanceof NotesException && ((NotesException)e).id == 4864) {
								// "Transactional Logging must be enabled for this function"
								log.log(Level.SEVERE, "Transactional logging is not enabled for this server; skipping transaction registration", e);
								if(res != null) {
									try {
										t.delistResource(res, XAResource.TMNOFLAGS);
									} catch (IllegalStateException | SystemException e1) {
										// Ignore
									}
								}
							} else {
								log.log(Level.SEVERE, "Encountered unexpected exception enlisting the transaction resource", e);
							}
						}
					}
				}
			}
		}
	}
	
	private static class DatabaseXAResource implements XAResource {
		
		private final Database database;
		private final String server;
		private final String filePath;
		private int transactionTimeout = Integer.MAX_VALUE;
		
		public DatabaseXAResource(Database database) throws NotesException {
			this.database = database;
			this.server = database.getServer();
			this.filePath = database.getFilePath();
		}

		@Override
		public void commit(Xid xid, boolean onePhase) throws XAException {
			try {
				database.transactionCommit();
			} catch (NotesException e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public void end(Xid xid, int flags) throws XAException {
			// TODO Figure out what to do here
			
		}

		@Override
		public void forget(Xid xid) throws XAException {
			// TODO Figure out what to do here
			
		}

		@Override
		public int getTransactionTimeout() throws XAException {
			return this.transactionTimeout;
		}

		@Override
		public boolean isSameRM(XAResource xares) throws XAException {
			if(xares == this) {
				return true;
			} else if(xares instanceof DatabaseXAResource dbres) {
				return StringUtil.equals(server, dbres.server) && StringUtil.equals(filePath, dbres.filePath);
			} else {
				return false;
			}
		}

		@Override
		public int prepare(Xid xid) throws XAException {
			// NOP for Domino
			return XA_OK;
		}

		@Override
		public Xid[] recover(int flag) throws XAException {
			// TODO Figure out what to do here
			return new Xid[0];
		}

		@Override
		public void rollback(Xid xid) throws XAException {
			try {
				database.transactionRollback();
			} catch (NotesException e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public boolean setTransactionTimeout(int seconds) throws XAException {
			this.transactionTimeout = seconds;
			return true;
		}

		@Override
		public void start(Xid xid, int flags) throws XAException {
			// TODO Figure out what to do here
		}

		@Override
		public String toString() {
			return String.format("DatabaseXAResource [server=%s, filePath=%s]", server, filePath); //$NON-NLS-1$
		}

		
	}
	
	@FunctionalInterface
	public interface NavFunction<R> {
	    R apply(Object nav, long limit, boolean didSkip, boolean didKey);
	}
}
