/**
 * Copyright © 2018-2022 Contributors to the XPages Jakarta EE Support Project
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

import org.openntf.xsp.nosql.communication.driver.DominoDocumentCollectionManager;
import org.openntf.xsp.nosql.communication.driver.impl.DefaultDominoDocumentCollectionManager;

import com.ibm.domino.xsp.module.nsf.NotesContext;

import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.nosql.mapping.Database;
import jakarta.nosql.mapping.DatabaseType;
import lotus.domino.NotesException;

@RequestScoped
public class NamesRepositoryBean {
	@Produces
	@Database(value = DatabaseType.DOCUMENT, provider = "names")
	public DominoDocumentCollectionManager getNamesManager() {
		return new DefaultDominoDocumentCollectionManager(
			() -> {
				try {
					return NotesContext.getCurrent().getSessionAsSigner().getDatabase("", "names.nsf");
				} catch (NotesException e) {
					throw new RuntimeException(e);
				}
			},
			() -> NotesContext.getCurrent().getSessionAsSigner()
		);
	}
}