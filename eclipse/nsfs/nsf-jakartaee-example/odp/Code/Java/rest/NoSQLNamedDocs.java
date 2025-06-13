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

import jakarta.inject.Inject;
import jakarta.json.JsonObject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import model.NamedDoc;

@Path("nosqlNamedDocs")
public class NoSQLNamedDocs {
	@Inject
	private NamedDoc.Repository repository;
	
	@Path("{name}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public NamedDoc getNamedDoc(@PathParam("name") String name) {
		return repository.findNamedDocument(name, null)
			.orElseThrow(() -> new NotFoundException());
	}
	
	@Path("{name}")
	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public NamedDoc updateNamedDoc(@PathParam("name") String name, NamedDoc entity) {
		entity.setNoteName(name);
		entity.setDocumentId(null);
		entity.setNoteUserName(null);
		return repository.save(entity);
	}
	
	@Path("{name}")
	@PATCH
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public NamedDoc patchNamedDoc(@PathParam("name") String name, JsonObject patch) {
		NamedDoc doc = getNamedDoc(name);
		doc.setSubject(patch.getString("subject", ""));
		return repository.save(doc);
	}
	
	@Path("{name}/{userName}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public NamedDoc getNamedDoc(@PathParam("name") String name, @PathParam("userName") String userName) {
		return repository.findNamedDocument(name, userName.replace('+', ' '))
			.orElseThrow(() -> new NotFoundException());
	}
	
	@Path("{name}/{userName}")
	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public NamedDoc updateNamedDoc(@PathParam("name") String name, @PathParam("userName") String userName, NamedDoc entity) {
		entity.setNoteName(name);
		entity.setDocumentId(null);
		entity.setNoteUserName(userName.replace('+', ' '));
		return repository.save(entity);
	}
	
	@Path("{name}/{userName}")
	@PATCH
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public NamedDoc patchNamedDoc(@PathParam("name") String name, @PathParam("userName") String userName, JsonObject patch) {
		NamedDoc doc = getNamedDoc(name, userName);
		doc.setSubject(patch.getString("subject", ""));
		return repository.save(doc);
	}
}
