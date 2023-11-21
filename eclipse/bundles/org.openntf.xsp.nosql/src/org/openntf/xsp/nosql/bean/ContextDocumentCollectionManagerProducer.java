/**
 * Copyright (c) 2018-2023 Contributors to the XPages Jakarta EE Support Project
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
package org.openntf.xsp.nosql.bean;

import org.openntf.xsp.nosql.communication.driver.DominoDocumentManager;
import org.openntf.xsp.nosql.communication.driver.lsxbe.impl.DominoDocumentConfiguration;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Default;
import jakarta.enterprise.inject.Produces;
import org.eclipse.jnosql.communication.document.DocumentManagerFactory;
import org.eclipse.jnosql.communication.document.DocumentConfiguration;
import org.eclipse.jnosql.mapping.Database;
import org.eclipse.jnosql.mapping.DatabaseType;

@RequestScoped
public class ContextDocumentCollectionManagerProducer {
	private DocumentConfiguration configuration;
	private DocumentManagerFactory managerFactory;

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
