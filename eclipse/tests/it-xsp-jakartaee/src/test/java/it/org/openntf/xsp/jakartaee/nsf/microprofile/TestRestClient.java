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
package it.org.openntf.xsp.jakartaee.nsf.microprofile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.StringReader;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;

import org.junit.jupiter.api.Test;

import it.org.openntf.xsp.jakartaee.AbstractWebClientTest;
import it.org.openntf.xsp.jakartaee.TestDatabase;

@SuppressWarnings("nls")
public class TestRestClient extends AbstractWebClientTest {
	@Test
	public void testRestClient() {
		Client client = getAnonymousClient();
		WebTarget target = client.target(getRestUrl(null, TestDatabase.MAIN) + "/restClient");
		Response response = target.request()
			.header("Host", "localhost:80")
			.get();
		
		String json = response.readEntity(String.class);
		JsonObject jsonObject = Json.createReader(new StringReader(json)).readObject();
		JsonObject responseObj = jsonObject.getJsonObject("response");
		assertEquals("bar", responseObj.getString("foo"), () -> json);
	}

	@Test
	public void testRestClientAsync() {
		Client client = getAnonymousClient();
		WebTarget target = client.target(getRestUrl(null, TestDatabase.MAIN) + "/restClient/async");
		Response response = target.request()
			.header("Host", "localhost:80")
			.get();
		
		String json = response.readEntity(String.class);
		JsonObject jsonObject = Json.createReader(new StringReader(json)).readObject();
		JsonObject responseObj = jsonObject.getJsonObject("response");
		assertNotNull(responseObj, () -> json);
		assertEquals("bar", responseObj.getString("foo"), () -> json);
	}
	
	@Test
	public void testJaxRsRestClient() {
		Client client = getAnonymousClient();
		WebTarget target = client.target(getRestUrl(null, TestDatabase.MAIN) + "/restClient/jaxRsClient");
		Response response = target.request()
			.header("Host", "localhost:80")
			.get();
		
		String json = response.readEntity(String.class);
		JsonObject jsonObject = Json.createReader(new StringReader(json)).readObject();
		assertTrue(jsonObject.containsKey("response"), () -> json);
		JsonObject responseObj = jsonObject.getJsonObject("response");
		assertTrue(responseObj.containsKey("foo"), () -> json);
		assertEquals("bar", responseObj.getString("foo"), () -> json);
	}
	
	/**
	 * @see <a href="https://github.com/OpenNTF/org.openntf.xsp.jakartaee/issues/492">Issue #492</a>
	 */
	@Test
	public void testConflictingNsfs() {
		Client client = getAnonymousClient();
		for(int i = 0; i < 5; i++) {
			WebTarget target = client.target(getRestUrl(null, TestDatabase.JSONB_CONFIG) + "/restClient");
			Response response = target.request()
				.header("Host", "localhost:80")
				.get();
			
			String json = response.readEntity(String.class);
			try {
				JsonObject jsonObject = Json.createReader(new StringReader(json)).readObject();
				JsonObject payload = jsonObject.getJsonObject("response");
				assertEquals("foo", payload.getString("setInJsonNsf", null), () -> json);
				assertEquals(null, payload.getString("setInNormalNsf", null), () -> json);
				assertEquals(null, payload.getString("shouldNeverBeSet", null), () -> json);
				assertEquals(null, payload.getString("shouldBeSetInNormal", null), () -> json);
			} catch(Exception e) {
				fail("Encountered exception parsing " + json, e);
			}
		}
		// Now test in the main NSF
		for(int i = 0; i < 5; i++) {
			WebTarget target = client.target(getRestUrl(null, TestDatabase.MAIN) + "/restClient/fetchEcho");
			Response response = target.request()
				.header("Host", "localhost:80")
				.get();
			
			String json = response.readEntity(String.class);
			try {
				JsonObject jsonObject = Json.createReader(new StringReader(json)).readObject();
				JsonObject payload = jsonObject.getJsonObject("response");
				assertEquals(null, payload.getString("setInJsonNsf", null), () -> json);
				assertEquals("foo", payload.getString("setInNormalNsf", null), () -> json);
				assertEquals(null, payload.getString("shouldNeverBeSet", null), () -> json);
				assertEquals("set", payload.getString("shouldBeSetInNormal", null), () -> json);
			} catch(Exception e) {
				fail("Encountered exception parsing " + json, e);
			}
		}
		// Try the JSON one again
		for(int i = 0; i < 5; i++) {
			WebTarget target = client.target(getRestUrl(null, TestDatabase.JSONB_CONFIG) + "/restClient");
			Response response = target.request()
				.header("Host", "localhost:80")
				.get();
			
			String json = response.readEntity(String.class);
			try {
				JsonObject jsonObject = Json.createReader(new StringReader(json)).readObject();
				JsonObject payload = jsonObject.getJsonObject("response");
				assertEquals("foo", payload.getString("setInJsonNsf", null), () -> json);
				assertEquals(null, payload.getString("setInNormalNsf", null), () -> json);
				assertEquals(null, payload.getString("shouldNeverBeSet", null), () -> json);
				assertEquals(null, payload.getString("shouldBeSetInNormal", null), () -> json);
			} catch(Exception e) {
				fail("Encountered exception parsing " + json, e);
			}
		}
		// Test the second call in the main NSF, which should NOT apply setInNormalNsf
		for(int i = 0; i < 5; i++) {
			WebTarget target = client.target(getRestUrl(null, TestDatabase.MAIN) + "/restClient/fetchEcho2");
			Response response = target.request()
				.header("Host", "localhost:80")
				.get();
			
			String json = response.readEntity(String.class);
			try {
				JsonObject jsonObject = Json.createReader(new StringReader(json)).readObject();
				JsonObject payload = jsonObject.getJsonObject("response");
				assertEquals(null, payload.getString("setInJsonNsf", null), () -> json);
				assertEquals(null, payload.getString("setInNormalNsf", null), () -> json);
				assertEquals(null, payload.getString("shouldNeverBeSet", null), () -> json);
				assertEquals("set", payload.getString("shouldBeSetInNormal", null), () -> json);
			} catch(Exception e) {
				fail("Encountered exception parsing " + json, e);
			}
		}
	}
}
