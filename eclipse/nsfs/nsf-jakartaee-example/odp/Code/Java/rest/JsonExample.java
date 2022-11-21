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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.openntf.xsp.jsonapi.JSONBindUtil;

import bean.ApplicationGuy;

@Path("/jsonExample")
public class JsonExample {
	
	@Inject
	private ApplicationGuy applicationGuy;
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(description="Example showing a basic Map being returned")
	public Map<String, Object> get() {
		
		return Collections.singletonMap("foo", "bar");
	}
	
	@GET
	@Path("/jsonp")
	@Produces(MediaType.APPLICATION_JSON)
	public JsonObject getJsonp() {
		return Json.createObjectBuilder()
			.add("bar", "baz")
			.build();
	}
	
	@GET
	@Path("/jsonb")
	@Produces(MediaType.APPLICATION_JSON)
	public Object getJsonb() {
		return applicationGuy;
	}
	
	public static class ExampleBean {
		private String foo;
		
		public String getFoo() {
			return foo;
		}
		public void setFoo(String foo) {
			this.foo = foo;
		}
	}
	
	@GET
	@Path("/jsonbSlashMap")
	@Produces(MediaType.APPLICATION_JSON)
	public Object getJsonbSlashMap() throws Exception {
		try(Jsonb jsonb = JsonbBuilder.create()) {
			Map<String, String> x = new HashMap<>();
			x.put("test\\pom.xml", "hello");
			String json = JSONBindUtil.toJson(x, jsonb);
			
			@SuppressWarnings("unchecked")
			Map<String, String> x2 = JSONBindUtil.fromJson(json, jsonb, Map.class);
			return x2;
		}
	}
}