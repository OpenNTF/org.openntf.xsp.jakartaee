/**
 * Copyright (c) 2018-2024 Contributors to the XPages Jakarta EE Support Project
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

import java.util.ArrayList;
import java.util.List;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.MediaType;
import model.LogDoc;
import jakarta.ws.rs.Produces;

@Path("nosql/logdoc")
public class NoSQLLogDocs {
	@Inject
	private LogDoc.Repository logDocs;
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public LogDoc create(@Valid LogDoc logDoc) {
		return logDocs.save(logDoc, true);
	}
	
	@Path("{unid}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public LogDoc get(@PathParam("unid") String unid) {
		return logDocs.findById(unid)
			.orElseThrow(() -> new NotFoundException());
	}
	
	@Path("{unid}")
	@PATCH
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.APPLICATION_JSON)
	public LogDoc patch(@PathParam("unid") String unid, String newName) {
		LogDoc doc = logDocs.findById(unid)
				.orElseThrow(() -> new NotFoundException());
		doc.setName(newName);
		List<String> log = new ArrayList<>(doc.getLog());
		log.add("{\"foo\":\"added at " + System.currentTimeMillis() + "\"}");
		doc.setLog(log);
		return logDocs.save(doc, true);
	}
}
