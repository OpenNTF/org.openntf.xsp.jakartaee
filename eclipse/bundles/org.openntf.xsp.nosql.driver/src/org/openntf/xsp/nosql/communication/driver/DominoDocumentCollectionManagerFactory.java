package org.openntf.xsp.nosql.communication.driver;

import jakarta.nosql.document.DocumentCollectionManagerFactory;

public class DominoDocumentCollectionManagerFactory implements DocumentCollectionManagerFactory {
	private final DatabaseSupplier supplier;
	
	public DominoDocumentCollectionManagerFactory(DatabaseSupplier supplier) {
		this.supplier = supplier;
	}

	@SuppressWarnings("unchecked")
	@Override
	public DefaultDominoDocumentCollectionManager get(String type) {
		return new DefaultDominoDocumentCollectionManager(supplier);
	}

	@Override
	public void close() {
	}

}
