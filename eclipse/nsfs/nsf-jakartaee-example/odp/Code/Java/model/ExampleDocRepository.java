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
package model;

import java.util.stream.Stream;

import org.openntf.xsp.jakarta.nosql.mapping.extension.DominoRepository;
import org.openntf.xsp.jakarta.nosql.mapping.extension.ViewEntries;

import jakarta.data.Sort;
import jakarta.data.repository.Find;
import jakarta.data.repository.OrderBy;

public interface ExampleDocRepository extends DominoRepository<ExampleDoc, String> {
	String VIEW_DOCS = "Example Docs";
	
	Stream<ExampleDoc> findAll();
	
	@ViewEntries(VIEW_DOCS)
	Stream<ExampleDoc> getViewEntries();
	
	@ViewEntries(value=VIEW_DOCS, documentsOnly=true)
	Stream<ExampleDoc> getViewEntriesDocsOnly();

	@ViewEntries(value=VIEW_DOCS, maxLevel=0)
	Stream<ExampleDoc> getViewCategories();
	
	@Find
	@OrderBy("title")
	@OrderBy("numberGuy")
	Stream<ExampleDoc> findAllSorted();
	
	@ViewEntries(value=VIEW_DOCS)
	Stream<ExampleDoc> getViewEntriesCustom(Sort<ExampleDoc> sorts);
}
