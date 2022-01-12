package org.openntf.xsp.nosql.communication.driver;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import org.openntf.xsp.nosql.communication.driver.QueryConverter.QueryConverterResult;

import com.ibm.domino.xsp.module.nsf.NotesContext;

import jakarta.nosql.document.DocumentDeleteQuery;
import jakarta.nosql.document.DocumentEntity;
import jakarta.nosql.document.DocumentQuery;
import lotus.domino.Database;
import lotus.domino.Document;
import lotus.domino.DocumentCollection;
import lotus.domino.DominoQuery;
import lotus.domino.NotesException;

public class DefaultDominoDocumentCollectionManager implements DominoDocumentCollectionManager {
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
			Database database = NotesContext.getCurrent().getCurrentDatabase();
			DominoQuery dominoQuery = database.createDominoQuery();
			// TODO limit, skip, and sort
			DocumentCollection docs = dominoQuery.execute(queryResult.getStatement().toString());
			// TODO stream this better
			List<DocumentEntity> result = new ArrayList<>();
			Document doc = docs.getFirstDocument();
			while(doc != null) {
				result.add(EntityConverter.convert(Collections.singleton(doc.getUniversalID()), database).findFirst().get());
				
				Document tempDoc = doc;
				doc = docs.getNextDocument();
				tempDoc.recycle();
			}
			
			return result.stream();
		} catch(NotesException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public long count(String documentCollection) {
		// TODO count by form
		return 0;
	}

	@Override
	public void close() {
	
	}

}
