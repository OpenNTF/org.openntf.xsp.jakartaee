/**
 * Copyright © 2018-2022 Jesse Gallagher
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
import jakarta.inject.Named;
import jakarta.mvc.Controller;
import jakarta.mvc.Models;
import jakarta.validation.constraints.NotEmpty;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import lotus.domino.Database;
import lotus.domino.NotesException;
import lotus.domino.Session;

@Path("mvc")
@Controller
public class MvcExample {
	
	@Inject
	Models models;
	
	@Inject
	@Named("dominoSessionAsSigner")
	Session session;
	
	@Inject
	Database database;
	
	@GET
	@Produces(MediaType.TEXT_HTML)
	public String get(@QueryParam("foo") String foo) throws NotesException {
		models.put("incomingFoo", foo);
		models.put("contextFromController", "s: " + session + " (hash code " + session.hashCode() + "); db: " + database);
		return "mvc.jsp";
	}
	
	@GET
	@Path("fooRequired")
	@Produces(MediaType.TEXT_HTML)
	public String getRequired(@QueryParam("foo") @NotEmpty String foo) throws NotesException {
		models.put("incomingFoo", foo);
		models.put("contextFromController", "s: " + session + " (hash code " + session.hashCode() + "); db: " + database);
		return "mvc.jsp";
	}

	@GET
	@Path("exception")
	@Produces(MediaType.TEXT_HTML)
	public String getException() {
		throw new RuntimeException("I am an exception from an MVC resource");
	}

	@GET
	@Path("notFound")
	@Produces(MediaType.TEXT_HTML)
	public String getNotFound() {
		throw new NotFoundException("I am a programmatic not-found exception from MVC");
	}
	
	@GET
	@Path("xpage")
	@Produces(MediaType.TEXT_HTML)
	public String getXPage() {
		return "el.xsp";
	}
}