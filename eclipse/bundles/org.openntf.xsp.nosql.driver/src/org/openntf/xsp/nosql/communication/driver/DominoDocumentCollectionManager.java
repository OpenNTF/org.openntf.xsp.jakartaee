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
package org.openntf.xsp.nosql.communication.driver;

import java.util.stream.Stream;

import jakarta.nosql.document.DocumentCollectionManager;
import jakarta.nosql.document.DocumentEntity;
import jakarta.nosql.mapping.Pagination;

public interface DominoDocumentCollectionManager extends DocumentCollectionManager {
	Stream<DocumentEntity> viewEntryQuery(String entityName, String viewName, String category, Pagination pagination, int maxLevel);
	
	Stream<DocumentEntity> viewDocumentQuery(String entityName, String viewName, String category, Pagination pagination, int maxLevel);
}
