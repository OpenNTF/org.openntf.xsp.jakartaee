package org.openntf.xsp.nosql.communication.driver;

import java.time.Duration;
import java.util.stream.Stream;

import jakarta.nosql.document.DocumentDeleteQuery;
import jakarta.nosql.document.DocumentEntity;
import jakarta.nosql.document.DocumentQuery;

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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long count(String documentCollection) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub

	}

}
