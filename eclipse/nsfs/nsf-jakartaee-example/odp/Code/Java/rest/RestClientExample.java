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

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;

import jakarta.enterprise.concurrent.ManagedExecutorService;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.json.JsonObject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientRequestFilter;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("restClient")
public class RestClientExample {
	public static class JsonExampleObject {
		private String foo;
		private String setInNormalNsf;
		private String setInJsonNsf;
		private String shouldNeverBeSet;
		private String shouldBeSetInNormal;
		
		public String getFoo() {
			return foo;
		}
		public void setFoo(String foo) {
			this.foo = foo;
		}
		
		public String getSetInNormalNsf() {
			return setInNormalNsf;
		}
		public void setSetInNormalNsf(String setInNormalNsf) {
			this.setInNormalNsf = setInNormalNsf;
		}
		
		public String getSetInJsonNsf() {
			return setInJsonNsf;
		}
		public void setSetInJsonNsf(String setInJsonNsf) {
			this.setInJsonNsf = setInJsonNsf;
		}
		
		public String getShouldNeverBeSet() {
			return shouldNeverBeSet;
		}
		public void setShouldNeverBeSet(String shouldNeverBeSet) {
			this.shouldNeverBeSet = shouldNeverBeSet;
		}
		
		public String getShouldBeSetInNormal() {
			return shouldBeSetInNormal;
		}
		public void setShouldBeSetInNormal(String shouldBeSetInNormal) {
			this.shouldBeSetInNormal = shouldBeSetInNormal;
		}
	}

	@RegisterProvider(AddHeaderProvider.class)
	public interface JsonExampleService {
		@GET
		@Produces(MediaType.APPLICATION_JSON)
		JsonExampleObject get();
	}

	/**
	 * Exists to ensure that the AddHeaderProvider is not applied to this service
	 */
	public interface JsonExampleService2 {
		@GET
		@Produces(MediaType.APPLICATION_JSON)
		JsonExampleObject get();
	}
	
	public static class AddHeaderProvider implements ClientRequestFilter {
		@Override
		public void filter(ClientRequestContext requestContext) throws IOException {
			requestContext.getHeaders().add("X-SetInNormalNSF", "foo");
		}
	}
	
	@Context
	HttpServletRequest request;
	
	@Inject @Named("java:comp/DefaultManagedExecutorService")
	ManagedExecutorService exec;

	@Path("echo")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String, Object> getEcho(
		@HeaderParam("X-SetInJsonNSF") String setInJsonNsf,
		@HeaderParam("X-SetInNormalNSF") String setInNormalNsf,
		@HeaderParam("X-ShouldNeverBeSet") String shouldNeverBeSet,
		@HeaderParam("X-ShouldBeSetInNormal") String shouldBeSetInNormal
	) {
		Map<String, Object> result = new HashMap<>();
		result.put("setInJsonNsf", setInJsonNsf);
		result.put("setInNormalNsf", setInNormalNsf);
		result.put("foo", "hi from normal NSF");
		result.put("shouldNeverBeSet", shouldNeverBeSet);
		result.put("shouldBeSetInNormal", shouldBeSetInNormal);
		return result;
	}
	
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
	
	@Path("fetchEcho")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Object getFetchEcho() {
		URI uri = URI.create(request.getRequestURL().toString());
		URI serviceUri = uri.resolve("echo");
		JsonExampleService service = RestClientBuilder.newBuilder()
			.baseUri(serviceUri)
			.build(JsonExampleService.class);
		JsonExampleObject responseObj = service.get();
		Map<String, Object> result = new LinkedHashMap<>();
		result.put("called", serviceUri);
		result.put("response", responseObj);
		return result;
	}
	
	@Path("fetchEcho2")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Object getFetchEcho2() {
		URI uri = URI.create(request.getRequestURL().toString());
		URI serviceUri = uri.resolve("echo");
		JsonExampleService2 service = RestClientBuilder.newBuilder()
			.baseUri(serviceUri)
			.build(JsonExampleService2.class);
		JsonExampleObject responseObj = service.get();
		Map<String, Object> result = new LinkedHashMap<>();
		result.put("called", serviceUri);
		result.put("response", responseObj);
		return result;
	}
	
	@Path("async")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Object getAsync() throws InterruptedException, ExecutionException {
		URI uri = URI.create(request.getRequestURL().toString());
		return exec.submit(() -> {
			URI serviceUri = uri.resolve("../jsonExample");
			JsonExampleService service = RestClientBuilder.newBuilder()
				.baseUri(serviceUri)
				.build(JsonExampleService.class);
			JsonExampleObject responseObj = service.get();
			Map<String, Object> result = new LinkedHashMap<>();
			result.put("called", serviceUri);
			result.put("response", responseObj);
			return result;
		}).get();
	}
	
	@Path("jaxRsClient")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Object getJaxRsClient() {
		URI uri = URI.create(request.getRequestURL().toString());
		URI serviceUri = uri.resolve("../jsonExample");
		Client client = ClientBuilder.newBuilder().build();
		WebTarget target = client.target(serviceUri);
		Response response = target.request().get();
		JsonObject responseObj = response.readEntity(JsonObject.class);
		Map<String, Object> result = new LinkedHashMap<>();
		result.put("called", serviceUri);
		result.put("response", responseObj);
		return result;
	}
}