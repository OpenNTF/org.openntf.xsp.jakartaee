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
package org.openntf.xsp.jakarta.nosql.bean;

import org.eclipse.jnosql.communication.semistructured.DatabaseManagerFactory;
import org.eclipse.jnosql.mapping.Database;
import org.eclipse.jnosql.mapping.DatabaseType;
import org.openntf.xsp.jakarta.nosql.communication.driver.DominoDocumentManager;
import org.openntf.xsp.jakarta.nosql.communication.driver.lsxbe.impl.DominoDocumentConfiguration;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Default;
import jakarta.enterprise.inject.Produces;

@RequestScoped
public class ContextDocumentCollectionManagerProducer {
	private DominoDocumentConfiguration configuration;
	private DatabaseManagerFactory managerFactory;

	@PostConstruct
	public void init() {
		configuration = new DominoDocumentConfiguration();
		managerFactory = configuration.apply(null);
	}

	@Produces
	@Database(value = DatabaseType.DOCUMENT, provider = "")
	public DominoDocumentManager getManager() {
		return (DominoDocumentManager)managerFactory.apply(null);
	}

	@Produces
	@Default
	public DominoDocumentManager getManagerDefault() {
		return (DominoDocumentManager)managerFactory.apply(null);
	}
}
