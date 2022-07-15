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
package rest;

import java.util.List;
import java.util.stream.Collectors;

import jakarta.inject.Inject;
import jakarta.transaction.UserTransaction;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import model.ExampleDoc;
import model.ExampleDocRepository;

@Path("exampleDocs")
public class NoSQLExampleDocs {
	@Inject
	private ExampleDocRepository repository;
	
	@Inject
	private UserTransaction transaction;
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public List<ExampleDoc> get() {
		return repository.findAll().collect(Collectors.toList());
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
	public ExampleDoc create(@FormParam("title") String title, @FormParam("categories") List<String> categories, @FormParam("authors") List<String> authors, @FormParam("intentionallyRollBack") boolean intentionallyRollBack) throws Exception {
		transaction.begin();
		try {
			ExampleDoc exampleDoc = new ExampleDoc();
			exampleDoc.setTitle(title);
			exampleDoc.setCategories(categories);
			exampleDoc.setAuthors(authors);
			ExampleDoc result = repository.save(exampleDoc, true);
			if(intentionallyRollBack) {
				throw new RuntimeException("I was asked to intentionally roll back");
			}
			transaction.commit();
			return result;
		} catch(Exception e) {
			transaction.rollback();
			throw e;
		}
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public ExampleDoc createJson(ExampleDoc exampleDoc) {
		return repository.save(exampleDoc, true);
	}
	
	@Path("{id}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public ExampleDoc getDoc(@PathParam("id") String id) {
		return repository.findById(id)
			.orElseThrow(() -> new NotFoundException("Could not find example doc for ID " + id));
	}
	
	@Path("{id}")
	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public ExampleDoc updateDoc(@PathParam("id") String id, ExampleDoc exampleDoc) {
		exampleDoc.setUnid(id);
		return repository.save(exampleDoc, true);
	}
	
	@Path("inView")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public List<ExampleDoc> getInView(@QueryParam("docsOnly") boolean docsOnly) {
		if(docsOnly) {
			return repository.getViewEntriesDocsOnly().collect(Collectors.toList());
		} else {
			return repository.getViewEntries().collect(Collectors.toList());
		}
	}
	
	@Path("viewCategories")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public List<ExampleDoc> getViewCategories() {
		return repository.getViewCategories().collect(Collectors.toList());
	}
}
