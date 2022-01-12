package org.openntf.xsp.nosql.communication.driver;

import java.time.Duration;
import java.util.stream.Stream;

import org.openntf.xsp.nosql.communication.driver.DQL.DQLTerm;
import org.openntf.xsp.nosql.communication.driver.QueryConverter.QueryConverterResult;

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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DocumentEntity insert(DocumentEntity entity, Duration ttl) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Iterable<DocumentEntity> insert(Iterable<DocumentEntity> entities) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Iterable<DocumentEntity> insert(Iterable<DocumentEntity> entities, Duration ttl) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DocumentEntity update(DocumentEntity entity) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Iterable<DocumentEntity> update(Iterable<DocumentEntity> entities) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void delete(DocumentDeleteQuery query) {
		// TODO Auto-generated method stub

	}

	@Override
	public Stream<DocumentEntity> select(DocumentQuery query) {
		QueryConverterResult queryResult = QueryConverter.select(query);
		try {
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
