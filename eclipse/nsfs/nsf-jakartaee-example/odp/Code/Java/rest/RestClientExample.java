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

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.microprofile.rest.client.RestClientBuilder;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;

@Path("restClient")
public class RestClientExample {
	public static class JsonExampleObject {
		private String foo;
		
		public String getFoo() {
			return foo;
		}
		public void setFoo(String foo) {
			this.foo = foo;
		}
	}
	
	public interface JsonExampleService {
		@GET
		@Produces(MediaType.APPLICATION_JSON)
		JsonExampleObject get();
	}
	
	@Context
	HttpServletRequest request;
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Object get() {
		URI uri = URI.create(request.getRequestURL().toString());
		URI serviceUri = uri.resolve("jsonExample");
		JsonExampleService service = RestClientBuilder.newBuilder()
			.baseUri(serviceUri)
			.build(JsonExampleService.class);
		JsonExampleObject responseObj = service.get();
		Map<String, Object> result = new LinkedHashMap<>();
		result.put("called", serviceUri);
		result.put("response", responseObj);
		return result;
	}
}