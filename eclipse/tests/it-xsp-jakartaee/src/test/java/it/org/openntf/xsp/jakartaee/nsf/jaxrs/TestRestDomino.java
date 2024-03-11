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
package it.org.openntf.xsp.jakartaee.nsf.jaxrs;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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
public class TestRestDomino extends AbstractWebClientTest {
	/**
	 * Tests rest.DominoObjectsSample, which uses JAX-RS and CDI with Domino context objects.
	 */
	@Test
	public void testSample() {
		Client client = getAnonymousClient();
		WebTarget target = client.target(getRestUrl(null, TestDatabase.MAIN) + "/dominoObjects");
		Response response = target.request().get();
		
		String json = response.readEntity(String.class);
		JsonObject jsonObject = Json.createReader(new StringReader(json)).readObject();
		
		String database = jsonObject.getString("database");
		assertTrue(database.contains("XPagesDatabase"), () -> "Received unexpected JSON: " + json);
		
		String dominoSession = jsonObject.getString("dominoSession");
		assertTrue(dominoSession.startsWith("lotus.domino.local.Session"), () -> "Received unexpected JSON: " + json);

		String sessionAsSigner = jsonObject.getString("dominoSessionAsSigner");
		assertTrue(sessionAsSigner.startsWith("lotus.domino.local.Session"), () -> "Received unexpected JSON: " + json);

		String sessionAsSignerWithFullAccess = jsonObject.getString("dominoSessionAsSignerWithFullAccess");
		assertNotNull(sessionAsSignerWithFullAccess);
		assertTrue(sessionAsSignerWithFullAccess.startsWith("lotus.domino.local.Session"), () -> "Received unexpected JSON: " + json);
	}
}
