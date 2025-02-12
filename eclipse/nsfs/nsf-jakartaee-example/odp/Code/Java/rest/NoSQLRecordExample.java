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
package rest;

import java.util.Map;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import lotus.domino.Database;
import lotus.domino.Document;
import lotus.domino.NotesException;
import model.RecordExample;

@SuppressWarnings("nls")
@Path("nosqlRecordDocs")
public class NoSQLRecordExample {
	@Inject
	private RecordExample.Repository repository;
	
	@Inject
	private Database database;
	
	@Path("byName/{name}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public RecordExample getByKey(@PathParam("name") String name) {
		return repository.findByName(name.replace('+', ' '))
			.orElseThrow(() -> new NotFoundException("No record found for name " + name));
	}
	
	@Path("createPartial")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String, Object> createPartial() throws NotesException {
		Document doc = database.createDocument();
		doc.replaceItemValue("Form", "RecordExample");
		doc.replaceItemValue("name", "PartialDoc" + System.currentTimeMillis());
		doc.save();
		return Map.of("name", doc.getItemValueString("name"));
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public RecordExample create(RecordExample entity) {
		return repository.insert(entity);
	}
}
