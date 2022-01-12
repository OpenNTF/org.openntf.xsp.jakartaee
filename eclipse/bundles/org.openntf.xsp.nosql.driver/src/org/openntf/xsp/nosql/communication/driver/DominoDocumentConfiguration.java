package org.openntf.xsp.nosql.communication.driver;

import jakarta.nosql.Settings;
import jakarta.nosql.document.DocumentConfiguration;

public class DominoDocumentConfiguration implements DocumentConfiguration {

	@SuppressWarnings("unchecked")
	@Override
	public DominoDocumentCollectionManagerFactory get() {
		return new DominoDocumentCollectionManagerFactory();
	}

	@SuppressWarnings("unchecked")
	@Override
	public DominoDocumentCollectionManagerFactory get(Settings settings) {
		return new DominoDocumentCollectionManagerFactory();
	}

}
