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
package model;

import java.time.Instant;
import java.util.Optional;
import java.util.stream.Stream;

import org.openntf.xsp.nosql.mapping.extension.DominoRepository;
import org.openntf.xsp.nosql.mapping.extension.ViewEntries;
import org.openntf.xsp.nosql.mapping.extension.ViewQuery;

import jakarta.nosql.mapping.Param;
import jakarta.nosql.mapping.Query;
import jakarta.nosql.mapping.Sorts;

public interface PersonRepository extends DominoRepository<Person, String> {
	String FOLDER_PERSONS = "Persons Folder";
	String VIEW_PERSONS = "Persons";
	
	Stream<Person> findAll();
	Stream<Person> findAll(Sorts sorts);
	Stream<Person> findByLastName(String lastName);
	
	@ViewEntries(FOLDER_PERSONS)
	Stream<Person> findInPersonsFolder();
	
	@ViewEntries(VIEW_PERSONS)
	Optional<Person> findByKey(ViewQuery viewQuery);
	
	@ViewEntries(VIEW_PERSONS)
	Stream<Person> findByKeyMulti(ViewQuery viewQuery);
	
	@Query("select * from Person where modified >= @modified")
	Stream<Person> findModifiedSince(@Param("modified") Instant modified);
}
