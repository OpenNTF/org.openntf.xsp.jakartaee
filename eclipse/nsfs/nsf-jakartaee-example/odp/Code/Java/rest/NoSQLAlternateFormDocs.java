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

import java.util.List;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import model.ExampleAlternateFormDoc;

@Path("exampleAlternateFormDocs")
public class NoSQLAlternateFormDocs {
	@Inject
	private ExampleAlternateFormDoc.Repository repository;
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public ExampleAlternateFormDoc create(ExampleAlternateFormDoc doc) {
		return repository.save(doc);
	}
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public List<ExampleAlternateFormDoc> list() {
		return repository.listAll().toList();
	}
	
	@Path("{id}")
	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public ExampleAlternateFormDoc update(@PathParam("id") String id, ExampleAlternateFormDoc doc) {
		doc.setUnid(id);
		return repository.save(doc);
	}
	
	@Path("@all")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public List<ExampleAlternateFormDoc> listAll() {
		return repository.findAll().toList();
	}
	
	@Path("@count")
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String getCount() {
		return Long.toString(repository.countBy());
	}
}
