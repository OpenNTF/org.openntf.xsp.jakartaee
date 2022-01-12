package org.openntf.xsp.nosql.communication.driver;

import jakarta.nosql.document.DocumentCollectionManagerFactory;

public class DominoDocumentCollectionManagerFactory implements DocumentCollectionManagerFactory {

	@SuppressWarnings("unchecked")
	@Override
	public DefaultDominoDocumentCollectionManager get(String database) {
		return new DefaultDominoDocumentCollectionManager();
	}

	@Override
	public void close() {
	}

}
