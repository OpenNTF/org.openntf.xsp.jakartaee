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
package org.openntf.xsp.nosql.communication.driver.impl;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.MessageFormat;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.eclipse.jnosql.mapping.reflection.ClassInformationNotFoundException;
import org.eclipse.jnosql.mapping.reflection.ClassMapping;
import org.eclipse.jnosql.mapping.reflection.ClassMappings;
import org.openntf.xsp.nosql.communication.driver.DatabaseSupplier;
import org.openntf.xsp.nosql.communication.driver.DominoDocumentCollectionManager;
import org.openntf.xsp.nosql.communication.driver.SessionSupplier;
import org.openntf.xsp.nosql.communication.driver.impl.DQL.DQLTerm;
import org.openntf.xsp.nosql.communication.driver.impl.QueryConverter.QueryConverterResult;

import com.ibm.commons.util.StringUtil;
import com.ibm.designer.domino.napi.NotesAPIException;
import com.ibm.designer.domino.napi.NotesSession;

import jakarta.enterprise.inject.spi.CDI;
import jakarta.nosql.Sort;
import jakarta.nosql.SortType;
import jakarta.nosql.document.Document;
import jakarta.nosql.document.DocumentDeleteQuery;
import jakarta.nosql.document.DocumentEntity;
import jakarta.nosql.document.DocumentQuery;
import jakarta.nosql.mapping.Pagination;
import lotus.domino.ACL;
import lotus.domino.ACLEntry;
import lotus.domino.Base;
import lotus.domino.Database;
import lotus.domino.DateTime;
import lotus.domino.DbDirectory;
import lotus.domino.DocumentCollection;
import lotus.domino.DominoQuery;
import lotus.domino.NotesException;
import lotus.domino.QueryResultsProcessor;
import lotus.domino.Session;
import lotus.domino.View;
import lotus.domino.ViewNavigator;

public class DefaultDominoDocumentCollectionManager implements DominoDocumentCollectionManager {

	private final DatabaseSupplier supplier;
	private final SessionSupplier sessionSupplier;
	private final EntityConverter entityConverter;
	
	public DefaultDominoDocumentCollectionManager(DatabaseSupplier supplier, SessionSupplier sessionSupplier) {
		this.supplier = supplier;
		this.sessionSupplier = sessionSupplier;
		this.entityConverter = new EntityConverter(supplier);
	}
	
	@Override
	public DocumentEntity insert(DocumentEntity entity) {
		try {
			Database database = supplier.get();
			lotus.domino.Document target = database.createDocument();
			
			Optional<Document> maybeId = entity.find(EntityConverter.FIELD_ID);
			if(maybeId.isPresent()) {
				target.setUniversalID(maybeId.get().get().toString());
			} else {
				// Write the generated UNID into the entity
				entity.add(Document.of(EntityConverter.FIELD_ID, target.getUniversalID()));
			}
			
			entityConverter.convert(entity, target);
			target.save();
			return entity;
		} catch(NotesException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public DocumentEntity insert(DocumentEntity entity, Duration ttl) {
		// TODO consider supporting ttl
		return insert(entity);
	}

	@Override
	public Iterable<DocumentEntity> insert(Iterable<DocumentEntity> entities) {
		if(entities == null) {
			return Collections.emptySet();
		} else {
			return StreamSupport.stream(entities.spliterator(), false)
				.map(this::insert)
				.collect(Collectors.toList());
		}
	}

	@Override
	public Iterable<DocumentEntity> insert(Iterable<DocumentEntity> entities, Duration ttl) {
		// TODO consider supporting ttl
		return insert(entities);
	}

	@Override
	public DocumentEntity update(DocumentEntity entity) {
		try {
			Database database = supplier.get();
			
			Document id = entity.find(EntityConverter.FIELD_ID)
				.orElseThrow(() -> new IllegalArgumentException(MessageFormat.format("Unable to find {0} in entity", EntityConverter.FIELD_ID)));
			
			lotus.domino.Document target = database.getDocumentByUNID((String)id.get());
			
			entityConverter.convert(entity, target);
			target.save();
			return entity;
		} catch(NotesException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Iterable<DocumentEntity> update(Iterable<DocumentEntity> entities) {
		if(entities == null) {
			return Collections.emptySet();
		} else {
			return StreamSupport.stream(entities.spliterator(), false)
				.map(this::update)
				.collect(Collectors.toList());
		}
	}

	@Override
	public void delete(DocumentDeleteQuery query) {
		try {
			Database database = supplier.get();
			List<String> unids = query.getDocuments();
			if(unids != null && !unids.isEmpty()) {
				for(String unid : unids) {
					if(unid != null && !unid.isEmpty()) {
						lotus.domino.Document doc = database.getDocumentByUNID(unid);
						doc.remove(true);
					}
				}
			} else if(query.getCondition().isPresent()) {
				// Then do it via DQL
				DQLTerm dql = QueryConverter.getCondition(query.getCondition().get());
				DominoQuery dominoQuery = database.createDominoQuery();
				DocumentCollection docs = dominoQuery.execute(dql.toString());
				docs.removeAll(true);
			}
		} catch(NotesException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Stream<DocumentEntity> select(DocumentQuery query) {
		try {
			String entityName = query.getDocumentCollection();
			ClassMapping mapping = getClassMapping(entityName);
			
			QueryConverterResult queryResult = QueryConverter.select(query);
			
			long skip = queryResult.getSkip();
			long limit = queryResult.getLimit();
			List<Sort> sorts = query.getSorts();
			Stream<DocumentEntity> result;
			
			if(sorts != null && !sorts.isEmpty()) {
				Database database = supplier.get();
				Session sessionAsSigner = sessionSupplier.get();
				Database qrpDatabase = getQrpDatabase(sessionAsSigner, database);

				String userName = database.getParent().getEffectiveUserName();
				String dqlQuery = queryResult.getStatement().toString();
				String viewName = getClass().getName() + "-" + Objects.hash(sorts, userName, dqlQuery); //$NON-NLS-1$
				View view = qrpDatabase.getView(viewName);
				try {
					if(view != null) {
						// Check to see if we need to "expire" it based on the data mod time of the DB
						DateTime created = view.getCreated();
						try {
							long dataMod = NotesSession.getLastDataModificationDateByName(database.getServer(), database.getFilePath());
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
						result = entityConverter.convert(database, view, mapping);
					} else {
						DominoQuery dominoQuery = database.createDominoQuery();		
						QueryResultsProcessor qrp = qrpDatabase.createQueryResultsProcessor();
						try {
							qrp.addDominoQuery(dominoQuery, dqlQuery, null);
							for(Sort sort : sorts) {
								int dir = sort.getType() == SortType.DESC ? QueryResultsProcessor.SORT_DESCENDING : QueryResultsProcessor.SORT_ASCENDING;
								qrp.addColumn(sort.getName(), null, null, dir, false, false);
							}
							
							view = qrp.executeToView(viewName, 24);
							try {
								result = entityConverter.convert(database, view, mapping);
							} finally {
								recycle(view);
							}
						} finally {
							recycle(qrp, dominoQuery, qrpDatabase);
						}
					}
				} finally {
					recycle(view);
				}
				
			} else {
				Database database = supplier.get();
				DominoQuery dominoQuery = database.createDominoQuery();		
				DocumentCollection docs = dominoQuery.execute(queryResult.getStatement().toString());
				try {
					result = entityConverter.convert(docs, mapping);
				} finally {
					recycle(docs, dominoQuery);
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

	@Override
	public Stream<DocumentEntity> viewEntryQuery(String entityName, String viewName, String category, Pagination pagination, int maxLevel) {
		ClassMapping mapping = getClassMapping(entityName);
		return buildNavigtor(viewName, category, pagination, maxLevel,
			(nav, limit) -> {
				try {
					return entityConverter.convertViewEntries(entityName, nav, limit, mapping);
				} catch (NotesException e) {
					throw new RuntimeException(e);
				}
			}
		);
	}
	
	@Override
	public Stream<DocumentEntity> viewDocumentQuery(String entityName, String viewName, String category,
			Pagination pagination, int maxLevel) {
		ClassMapping mapping = getClassMapping(entityName);
		return buildNavigtor(viewName, category, pagination, maxLevel,
			(nav, limit) -> {
				try {
					return entityConverter.convertViewDocuments(entityName, nav, limit, mapping);
				} catch (NotesException e) {
					throw new RuntimeException(e);
				}
			}
		);
	}

	@Override
	public long count(String documentCollection) {
		try {
			Database database = supplier.get();
			DominoQuery dominoQuery = database.createDominoQuery();
			DQLTerm dql = DQL.item(EntityConverter.FIELD_NAME).isEqualTo(documentCollection);
			DocumentCollection result = dominoQuery.execute(dql.toString());
			return result.getCount();
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
	
	private <T> T buildNavigtor(String viewName, String category, Pagination pagination, int maxLevel, BiFunction<ViewNavigator, Long, T> consumer) {
		try {
			if(StringUtil.isEmpty(viewName)) {
				throw new IllegalArgumentException("viewName cannot be empty");
			}
			
			Database database = supplier.get();
			View view = database.getView(viewName);
			Objects.requireNonNull(view, () -> "Unable to open view: " + viewName);
			view.setAutoUpdate(false);
			
			ViewNavigator nav;
			if(category == null) {
				nav = view.createViewNav();
			} else {
				nav = view.createViewNavFromCategory(category);
			}
			
			if(maxLevel > -1) {
				nav.setMaxLevel(maxLevel);
			}
			
			long limit = 0;
			if(pagination != null) {
				long skip = pagination.getSkip();
				limit = pagination.getLimit();
				
				if(skip > Integer.MAX_VALUE) {
					throw new UnsupportedOperationException("Domino does not support skipping more than Integer.MAX_VALUE entries");
				}
				if(skip > 0) {
					nav.skip((int)skip);
				}
			}
			
			if(limit > 0) {
				nav.setBufferMaxEntries((int)Math.max(400, limit));
			} else {
				nav.setBufferMaxEntries(400);
			}
			
			return consumer.apply(nav, limit);
		} catch(NotesException e) {
			throw new RuntimeException(e);
		}
	}
	
	private Database getQrpDatabase(Session session, Database database) throws NotesException {
		String server = database.getServer();
		String filePath = database.getFilePath();

		try {
			String fileName = md5(server + filePath) + ".nsf"; //$NON-NLS-1$
			
			Path tempDir = getTempDirectory();
			Path dest = tempDir.resolve(getClass().getPackage().getName());
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
	
	private Path getTempDirectory() {
		String osName = System.getProperty("os.name"); //$NON-NLS-1$
		if (osName.startsWith("Linux") || osName.startsWith("LINUX")) { //$NON-NLS-1$ //$NON-NLS-2$
			return Paths.get("/tmp"); //$NON-NLS-1$
		} else {
			return Paths.get(System.getProperty("java.io.tmpdir")); //$NON-NLS-1$
		}
	}
	
	private String md5(String value) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5"); //$NON-NLS-1$
			md.update(String.valueOf(value).getBytes());
			byte[] digest = md.digest();
			StringBuilder sb = new StringBuilder(digest.length * 2);
			for (byte b : digest) {
				sb.append(String.format("%02x", b)); //$NON-NLS-1$
			}
			return sb.toString();
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException(e);
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
	
	private ClassMapping getClassMapping(String entityName) {
		ClassMappings mappings = CDI.current().select(ClassMappings.class).get();
		try {
			return mappings.findByName(entityName);
		} catch(ClassInformationNotFoundException e) {
			// Shouldn't happen, but we should account for it
			return null;
		}
	}
}
