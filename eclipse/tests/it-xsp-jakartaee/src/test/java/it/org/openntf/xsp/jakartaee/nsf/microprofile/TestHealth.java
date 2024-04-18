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
package it.org.openntf.xsp.jakartaee.nsf.microprofile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.StringReader;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;

import org.junit.jupiter.api.Test;

import it.org.openntf.xsp.jakartaee.AbstractWebClientTest;
import it.org.openntf.xsp.jakartaee.TestDatabase;

@SuppressWarnings("nls")
public class TestHealth extends AbstractWebClientTest {
	@Test
	public void testAll() {
		Client client = getAnonymousClient();
		WebTarget target = client.target(getRestUrl(null, TestDatabase.MAIN) + "/health");
		Response response = target.request().get();
		
		String result = response.readEntity(String.class);
		try {
			JsonObject jsonObject = Json.createReader(new StringReader(result)).readObject();
			assertEquals("DOWN", jsonObject.getString("status"));
			JsonArray checks = jsonObject.getJsonArray("checks");
			assertEquals(3, checks.size());
		} catch(Exception e) {
			fail("Unexpected response: " + result, e);
		}
	}
	
	@Test
	public void testReadiness() {
		Client client = getAnonymousClient();
		WebTarget target = client.target(getRestUrl(null, TestDatabase.MAIN) + "/health/ready");
		Response response = target.request().get();
		
		String result = response.readEntity(String.class);
		try {
			JsonObject jsonObject = Json.createReader(new StringReader(result)).readObject();
			assertEquals("DOWN", jsonObject.getString("status"));
			JsonArray checks = jsonObject.getJsonArray("checks");
			assertEquals(1, checks.size());
			
			assertEquals("I am a failing readiness check", checks.getJsonObject(0).getString("name"));
		} catch(Exception e) {
			fail("Unexpected response: " + result, e);
		}
	}
	
	@Test
	public void testLiveness() {
		Client client = getAnonymousClient();
		WebTarget target = client.target(getRestUrl(null, TestDatabase.MAIN) + "/health/live");
		Response response = target.request().get();
		
		String result = response.readEntity(String.class);
		try {
			JsonObject jsonObject = Json.createReader(new StringReader(result)).readObject();
			assertEquals("UP", jsonObject.getString("status"));
			JsonArray checks = jsonObject.getJsonArray("checks");
			assertEquals(1, checks.size());
			
			assertEquals("I am the liveliness check", checks.getJsonObject(0).getString("name"));
			JsonObject data = checks.getJsonObject(0).getJsonObject("data");
			assertTrue(data.getInt("noteCount") > 0);
		} catch(Exception e) {
			fail("Unexpected response: " + result, e);
		}
	}
	
	@Test
	public void testStarted() {
		Client client = getAnonymousClient();
		WebTarget target = client.target(getRestUrl(null, TestDatabase.MAIN) + "/health/started");
		Response response = target.request().get();
		
		String result = response.readEntity(String.class);
		try {
			JsonObject jsonObject = Json.createReader(new StringReader(result)).readObject();
			assertEquals("UP", jsonObject.getString("status"));
			JsonArray checks = jsonObject.getJsonArray("checks");
			assertEquals(1, checks.size());
			
			assertEquals("started up fine", checks.getJsonObject(0).getString("name"));
		} catch(Exception e) {
			fail("Unexpected response: " + result, e);
		}
	}
	
}
