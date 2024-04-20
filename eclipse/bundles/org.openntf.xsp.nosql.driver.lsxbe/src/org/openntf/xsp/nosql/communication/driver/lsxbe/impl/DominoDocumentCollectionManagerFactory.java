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

import org.eclipse.jnosql.communication.document.DocumentManagerFactory;
import org.openntf.xsp.nosql.communication.driver.impl.AbstractDominoDocumentCollectionManager;
import org.openntf.xsp.nosql.communication.driver.lsxbe.DatabaseSupplier;
import org.openntf.xsp.nosql.communication.driver.lsxbe.SessionSupplier;

public class DominoDocumentCollectionManagerFactory implements DocumentManagerFactory {
	private final DatabaseSupplier databaseSupplier;
	private final SessionSupplier sessionSupplier;
	
	public DominoDocumentCollectionManagerFactory(DatabaseSupplier databaseSupplier, SessionSupplier sessionSupplier) {
		this.databaseSupplier = databaseSupplier;
		this.sessionSupplier = sessionSupplier;
	}

	@Override
	public AbstractDominoDocumentCollectionManager apply(String type) {
		return new DefaultDominoDocumentCollectionManager(databaseSupplier, sessionSupplier);
	}

	@Override
	public void close() {
	}

}
