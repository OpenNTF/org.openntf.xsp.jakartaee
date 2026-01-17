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

import org.eclipse.jnosql.mapping.Database;
import org.eclipse.jnosql.mapping.DatabaseType;
import org.openntf.xsp.jakarta.nosql.communication.driver.DominoDocumentManager;
import org.openntf.xsp.jakarta.nosql.communication.driver.lsxbe.impl.DefaultDominoDocumentCollectionManager;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import lotus.domino.NotesException;
import lotus.domino.Session;

@Dependent
public class NamesRepositoryBean {
	
	@Inject @Named("dominoSessionAsSigner")
	private Session sessionAsSigner;
	
	@Produces
	@Database(value = DatabaseType.DOCUMENT, provider = "names")
	public DominoDocumentManager getNamesManager() {
		return new DefaultDominoDocumentCollectionManager(
			() -> {
				try {
					return sessionAsSigner.getDatabase("", "names.nsf"); //$NON-NLS-1$ //$NON-NLS-2$
				} catch (NotesException e) {
					throw new RuntimeException(e);
				}
			},
			() -> sessionAsSigner
		);
	}
}