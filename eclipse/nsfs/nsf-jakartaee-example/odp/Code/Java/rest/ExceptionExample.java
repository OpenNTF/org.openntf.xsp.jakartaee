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

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("exceptionExample")
public class ExceptionExample {
	@GET
	public Object get() {
		throw new RuntimeException("this is an example exception", new RuntimeException("this is a cause"));
	}
	
	@Path("html")
	@GET
	@Produces(MediaType.TEXT_HTML)
	public Object getHtml() {
		throw new RuntimeException("this is expected to be rendered as HTML", new RuntimeException("this is a cause"));
	}
	
	@Path("text")
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public Object getText() {
		throw new RuntimeException("this is expected to be rendered as text", new RuntimeException("this is a cause"));
	}
}
