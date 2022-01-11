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

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.mvc.Controller;
import jakarta.mvc.Models;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import lotus.domino.Database;
import lotus.domino.NotesException;
import lotus.domino.Session;

@Path("mvc")
@Controller
@Dependent
public class MvcExample {
	
	@Inject
	Models models;
	
	@Inject
	@Named("sessionAsSigner")
	Session session;
	
	@Inject
	Database database;
	
	@GET
	@Produces(MediaType.TEXT_HTML)
	public String get(@QueryParam("foo") String foo) throws NotesException {
		models.put("incomingFoo", foo);
		models.put("contextFromController", "s: " + session + "; db: " + database);
		return "mvc.jsp";
	}
}