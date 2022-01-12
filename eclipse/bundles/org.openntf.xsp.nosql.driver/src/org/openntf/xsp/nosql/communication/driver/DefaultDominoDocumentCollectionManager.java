package org.openntf.xsp.nosql.communication.driver;

import java.text.MessageFormat;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.openntf.xsp.nosql.communication.driver.DQL.DQLTerm;
import org.openntf.xsp.nosql.communication.driver.QueryConverter.QueryConverterResult;

import jakarta.nosql.document.Document;
import jakarta.nosql.document.DocumentDeleteQuery;
import jakarta.nosql.document.DocumentEntity;
import jakarta.nosql.document.DocumentQuery;
import lotus.domino.Database;
import lotus.domino.DocumentCollection;
import lotus.domino.DominoQuery;
import lotus.domino.NotesException;

public class DefaultDominoDocumentCollectionManager implements DominoDocumentCollectionManager {

	private final DatabaseSupplier supplier;
	
	public DefaultDominoDocumentCollectionManager(DatabaseSupplier supplier) {
		this.supplier = supplier;
	}
	
	@Override
	public DocumentEntity insert(DocumentEntity entity) {
		try {
			Database database = supplier.get();
			lotus.domino.Document target = database.createDocument();
			
			Optional<Document> maybeId = entity.find(EntityConverter.ID_FIELD);
			if(maybeId.isPresent()) {
				target.setUniversalID(maybeId.get().get().toString());
			} else {
				// Write the generated UNID into the entity
				entity.add(Document.of(EntityConverter.ID_FIELD, target.getUniversalID()));
			}
			
			EntityConverter.convert(entity, target);
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
			
			Document id = entity.find(EntityConverter.ID_FIELD)
				.orElseThrow(() -> new IllegalArgumentException(MessageFormat.format("Unable to find {0} in entity", EntityConverter.ID_FIELD)));
			
			lotus.domino.Document target = database.getDocumentByUNID((String)id.get());
			
			EntityConverter.convert(entity, target);
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
			QueryConverterResult queryResult = QueryConverter.select(query);
			Database database = supplier.get();
			DominoQuery dominoQuery = database.createDominoQuery();
			// TODO limit, skip, and sort efficiently
			DocumentCollection docs = dominoQuery.execute(queryResult.getStatement().toString());
			Stream<DocumentEntity> result = EntityConverter.convert(docs);
			if(queryResult.getSkip() > 0) {
				result = result.skip(queryResult.getSkip());
			}
			if(queryResult.getLimit() > 0) {
				result = result.limit(queryResult.getLimit());
			}
			return result;
		} catch(NotesException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public long count(String documentCollection) {
		try {
			Database database = supplier.get();
			DominoQuery dominoQuery = database.createDominoQuery();
			DQLTerm dql = DQL.item(EntityConverter.NAME_FIELD).isEqualTo(documentCollection);
			DocumentCollection result = dominoQuery.execute(dql.toString());
			return result.getCount();
		} catch(NotesException e) {
			throw new RuntimeException(e);
		}
		
	}

	@Override
	public void close() {
	
	}

}
