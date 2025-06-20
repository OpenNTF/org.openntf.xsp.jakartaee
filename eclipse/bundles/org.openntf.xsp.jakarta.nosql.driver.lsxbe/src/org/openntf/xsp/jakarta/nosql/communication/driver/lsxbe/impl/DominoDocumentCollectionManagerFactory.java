/**
 * Copyright (c) 2018-2025 Contributors to the XPages Jakarta EE Support Project
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
package org.openntf.xsp.jakarta.nosql.communication.driver.lsxbe.impl;

import java.util.function.Supplier;

import org.eclipse.jnosql.communication.semistructured.DatabaseManagerFactory;
import org.openntf.xsp.jakarta.nosql.communication.driver.impl.AbstractDominoDocumentCollectionManager;

import lotus.domino.Database;
import lotus.domino.Session;

public class DominoDocumentCollectionManagerFactory implements DatabaseManagerFactory {
	private final Supplier<Database> databaseSupplier;
	private final Supplier<Session> sessionSupplier;

	public DominoDocumentCollectionManagerFactory(final Supplier<Database> databaseSupplier, final Supplier<Session> sessionSupplier) {
		this.databaseSupplier = databaseSupplier;
		this.sessionSupplier = sessionSupplier;
	}

	@Override
	public AbstractDominoDocumentCollectionManager apply(final String type) {
		return new DefaultDominoDocumentCollectionManager(databaseSupplier, sessionSupplier);
	}

	@Override
	public void close() {
	}

}
