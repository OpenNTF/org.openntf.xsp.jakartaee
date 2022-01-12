package org.openntf.xsp.nosql.communication.driver;

import jakarta.enterprise.inject.spi.CDI;
import jakarta.nosql.Settings;
import jakarta.nosql.document.DocumentConfiguration;

public class DominoDocumentConfiguration implements DocumentConfiguration {
	public static final String SETTING_SUPPLIER = "databaseSupplier"; //$NON-NLS-1$
	
	@SuppressWarnings("unchecked")
	@Override
	public DominoDocumentCollectionManagerFactory get() {
		return new DominoDocumentCollectionManagerFactory(CDI.current().select(DatabaseSupplier.class).get());
	}

	@SuppressWarnings("unchecked")
	@Override
	public DominoDocumentCollectionManagerFactory get(Settings settings) {
		DatabaseSupplier supplier = settings.get(SETTING_SUPPLIER)
			.map(DatabaseSupplier.class::cast)
			.orElseGet(() -> CDI.current().select(DatabaseSupplier.class).get());
		return new DominoDocumentCollectionManagerFactory(supplier);
	}

}