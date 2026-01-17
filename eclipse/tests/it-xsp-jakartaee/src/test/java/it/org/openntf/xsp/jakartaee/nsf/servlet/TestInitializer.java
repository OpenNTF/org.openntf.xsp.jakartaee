/**
 * Copyright (c) 2018-2026 Contributors to the XPages Jakarta EE Support Project
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
package it.org.openntf.xsp.jakartaee.nsf.servlet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import it.org.openntf.xsp.jakartaee.AbstractWebClientTest;
import it.org.openntf.xsp.jakartaee.TestDatabase;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;

@SuppressWarnings("nls")
public class TestInitializer extends AbstractWebClientTest {
	@Test
	public void testInitializerServlet() {
		Client client = getAnonymousClient();
		WebTarget target = client.target(getRootUrl(null, TestDatabase.MAIN_MODULE) + TestDatabase.MAIN_MODULE.getXspPrefix()+"/initializerResource");
		Response response = target.request().get();
		checkResponse(200, response);
		
		String body = response.readEntity(String.class);
		assertTrue(body.contains("I found 1 class"), () -> "Body should contain the expected message, got: " + body);
	}
	
	@Test
	public void testUnusedInitializerServlet() {
		Client client = getAnonymousClient();
		WebTarget target = client.target(getRootUrl(null, TestDatabase.JPA) + TestDatabase.JPA.getXspPrefix()+"/initializerResource");
		Response response = target.request().get();
		checkResponse(200, response);
		
		String body = response.readEntity(String.class);
		assertEquals("null", body);
	}
}
