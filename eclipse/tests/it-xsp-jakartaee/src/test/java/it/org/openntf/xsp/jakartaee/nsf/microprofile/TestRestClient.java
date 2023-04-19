/**
 * Copyright (c) 2018-2023 Contributors to the XPages Jakarta EE Support Project
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

import java.io.StringReader;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;

import org.junit.jupiter.api.Test;

import it.org.openntf.xsp.jakartaee.AbstractWebClientTest;

@SuppressWarnings("nls")
public class TestRestClient extends AbstractWebClientTest {
	@Test
	public void testRestClient() {
		Client client = getAnonymousClient();
		WebTarget target = client.target(getRestUrl(null) + "/restClient");
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
		WebTarget target = client.target(getRestUrl(null) + "/restClient/async");
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
		WebTarget target = client.target(getRestUrl(null) + "/restClient/jaxRsClient");
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
}
