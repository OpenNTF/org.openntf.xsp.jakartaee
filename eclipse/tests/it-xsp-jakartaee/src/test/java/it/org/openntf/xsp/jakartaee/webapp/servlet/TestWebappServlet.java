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
package it.org.openntf.xsp.jakartaee.webapp.servlet;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import it.org.openntf.xsp.jakartaee.AbstractWebClientTest;
import it.org.openntf.xsp.jakartaee.TestDatabase;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;

@SuppressWarnings("nls")
public class TestWebappServlet extends AbstractWebClientTest {

	@Test
	public void testExampleServlet() {
		Client client = getAnonymousClient();
		WebTarget target = client.target(getRootUrl(null, TestDatabase.OSGI_WEBAPP) + "/exampleServlet");
		Response response = target.request().get();
		
		String body = response.readEntity(String.class);
		String expected = "Hello from ExampleServlet. context=/jeeExample, path=/exampleServlet";
		assertTrue(body.startsWith("Hello from ExampleServlet. context="), () -> "Body should start with <" + expected + ">, got <" + body + ">");
	}
	
	// Tests the functionality of ComponentModuleLocator in an OSGi Webapp context
	@Test
	public void testLocatorServlet() {
		Client client = getAnonymousClient();
		WebTarget target = client.target(getRootUrl(null, TestDatabase.OSGI_WEBAPP) + "/locatorTestServlet");
		Response response = target.request().get();
		
		String body = response.readEntity(String.class);
		
		assertFalse(body.contains("Module: null"), () -> "Received unexpected body: " + body);
		assertFalse(body.contains("Request: Optional.empty"), () -> "Received unexpected body: " + body);
		assertFalse(body.contains("Context: Optional.empty"), () -> "Received unexpected body: " + body);
	}
	
	// Tests the functionality of ComponentModuleLocator in an OSGi Webapp context when run within an NSF path
	@Test
	public void testLocatorContextualServlet() {
		Client client = getAnonymousClient();
		WebTarget target = client.target(getWebappContextualRootUrl(null) + "/locatorTestServlet");
		Response response = target.request().get();
		
		String body = response.readEntity(String.class);
		
		assertFalse(body.contains("Module: null"), () -> "Received unexpected body: " + body);
		assertFalse(body.contains("Request: Optional.empty"), () -> "Received unexpected body: " + body);
		assertFalse(body.contains("Context: Optional.empty"), () -> "Received unexpected body: " + body);
	}
}
