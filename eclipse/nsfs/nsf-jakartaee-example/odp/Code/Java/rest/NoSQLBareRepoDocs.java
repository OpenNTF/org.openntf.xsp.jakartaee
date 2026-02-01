/*
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
package rest;

import java.util.List;
import java.util.stream.Collectors;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import model.BareRepoDoc;

@Path("bareRepoDocs")
public class NoSQLBareRepoDocs {
	@Inject
	private BareRepoDoc.Repository repository;
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public List<BareRepoDoc> get() {
		return repository.findAll().collect(Collectors.toList());
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public BareRepoDoc createJson(BareRepoDoc exampleDoc) {
		return repository.save(exampleDoc);
	}
	
	@Path("{id}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getDoc(@PathParam("id") String id) {
		BareRepoDoc doc = repository.findById(id)
			.orElseThrow(() -> new NotFoundException("Could not find example doc for ID " + id));
		return Response.ok(doc)
			.build();
	}
	
	@Path("{id}")
	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public BareRepoDoc updateDoc(@PathParam("id") String id, BareRepoDoc exampleDoc) {
		BareRepoDoc fixedDoc = new BareRepoDoc(id, exampleDoc.title());
		return repository.save(fixedDoc);
	}
}
