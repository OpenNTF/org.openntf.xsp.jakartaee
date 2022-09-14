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
package it.org.openntf.xsp.jakartaee.nsf.json;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.StringReader;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;

import org.junit.jupiter.api.Test;

import it.org.openntf.xsp.jakartaee.AbstractWebClientTest;

@SuppressWarnings("nls")
public class TestRestJson extends AbstractWebClientTest {
	@Test
	public void testJsonp() {
		Client client = getAnonymousClient();
		WebTarget target = client.target(getRestUrl(null) + "/jsonExample/jsonp");
		Response response = target.request().get();
		
		String json = response.readEntity(String.class);
		JsonObject jsonObject = Json.createReader(new StringReader(json)).readObject();
		assertEquals("baz", jsonObject.getString("bar"));
	}
	
	@Test
	public void testJsonb() {
		Client client = getAnonymousClient();
		WebTarget target = client.target(getRestUrl(null) + "/jsonExample");
		Response response = target.request().get();
		
		String json = response.readEntity(String.class);
		JsonObject jsonObject = Json.createReader(new StringReader(json)).readObject();
		assertEquals("bar", jsonObject.getString("foo"));
	}
	
	@Test
	public void testJsonbCdi() {
		Client client = getAnonymousClient();
		WebTarget target = client.target(getRestUrl(null) + "/jsonExample/jsonb");
		Response response = target.request().get();
		
		String json = response.readEntity(String.class);
		JsonObject jsonObject = Json.createReader(new StringReader(json)).readObject();
		String jsonMessage = jsonObject.getString("jsonMessage");
		assertTrue(jsonMessage.startsWith("I'm application guy at "));
	}
}
