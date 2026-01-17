/**
 * Copyright (c) 2018-2026 Contributors to the XPages Jakarta EE Support Project
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
package bean;

import org.openntf.xsp.jakarta.nosql.communication.driver.DominoDocumentManager;
import org.openntf.xsp.jakarta.nosql.communication.driver.lsxbe.impl.DefaultDominoDocumentCollectionManager;
import org.openntf.xsp.jakartaee.module.ComponentModuleLocator;

import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Produces;
import org.eclipse.jnosql.mapping.Database;
import org.eclipse.jnosql.mapping.DatabaseType;
import lotus.domino.NotesException;

@RequestScoped
public class MailRepositoryBean {
	@Produces
	@Database(value = DatabaseType.DOCUMENT, provider = "devMail")
	public DominoDocumentManager getNamesManager() {
		return new DefaultDominoDocumentCollectionManager(
			() -> {
				try {
					return ComponentModuleLocator.getDefault().flatMap(ComponentModuleLocator::getSessionAsSigner).get().getDatabase("", "dev/jakartamail.nsf");
				} catch (NotesException e) {
					throw new RuntimeException(e);
				}
			},
			() -> ComponentModuleLocator.getDefault().flatMap(ComponentModuleLocator::getSessionAsSigner).get()
		);
	}
}
