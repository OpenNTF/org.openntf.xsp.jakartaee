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
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.eclipse.microprofile.metrics.annotation.SimplyTimed;

import bean.ApplicationGuy;
import bean.RequestGuy;

@Path("/sample")
public class Sample {
	@Inject
	private ApplicationGuy applicationGuy;
	
	@Inject
	private RequestGuy requestGuy;

	@GET
	@SimplyTimed
	public Response hello() {
		try {
			String message = applicationGuy.getMessage() + "\n" + requestGuy.getMessage();;
			return Response.ok().type(MediaType.TEXT_PLAIN).entity(message).build();
		} catch (Throwable t) {
			return Response.serverError().build();
		}
	}
	
	@GET
	@Path("/xml")
	@Produces(MediaType.APPLICATION_XML)
	public Object xml() {
		return applicationGuy;
	}
	
	@GET
	@Path("inputstream")
	@Produces(MediaType.TEXT_PLAIN)
	public InputStream getInputStream() {
		return new ByteArrayInputStream("hello there from an input stream".getBytes());
	}
}