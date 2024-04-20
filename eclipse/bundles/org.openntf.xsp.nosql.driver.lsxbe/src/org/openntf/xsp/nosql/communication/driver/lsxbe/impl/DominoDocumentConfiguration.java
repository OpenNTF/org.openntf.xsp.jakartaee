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
package org.openntf.xsp.nosql.communication.driver.lsxbe.impl;

import org.openntf.xsp.nosql.communication.driver.lsxbe.DatabaseSupplier;
import org.openntf.xsp.nosql.communication.driver.lsxbe.SessionSupplier;

import jakarta.enterprise.inject.spi.CDI;
import org.eclipse.jnosql.communication.Settings;
import org.eclipse.jnosql.communication.document.DocumentConfiguration;

public class DominoDocumentConfiguration implements DocumentConfiguration {
	public static final String SETTING_SESSION_SUPPLIER = "sessionSupplier"; //$NON-NLS-1$
	public static final String SETTING_SUPPLIER = "databaseSupplier"; //$NON-NLS-1$
	
	public DominoDocumentCollectionManagerFactory get() {
		return new DominoDocumentCollectionManagerFactory(
			CDI.current().select(DatabaseSupplier.class).get(),
			CDI.current().select(SessionSupplier.class).get()
		);
	}

	@Override
	public DominoDocumentCollectionManagerFactory apply(Settings settings) {
		if(settings == null) {
			return get();
		}
		DatabaseSupplier supplier = settings.get(SETTING_SUPPLIER)
			.map(DatabaseSupplier.class::cast)
			.orElseGet(() -> CDI.current().select(DatabaseSupplier.class).get());
		SessionSupplier sessionSupplier = settings.get(SETTING_SESSION_SUPPLIER)
			.map(SessionSupplier.class::cast)
			.orElseGet(() -> CDI.current().select(SessionSupplier.class).get());
		return new DominoDocumentCollectionManagerFactory(supplier, sessionSupplier);
	}

}