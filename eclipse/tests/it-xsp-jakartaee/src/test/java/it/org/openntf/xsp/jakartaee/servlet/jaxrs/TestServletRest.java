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
package it.org.openntf.xsp.jakartaee.servlet.jaxrs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;

import org.junit.jupiter.api.Test;

import it.org.openntf.xsp.jakartaee.AbstractWebClientTest;
import it.org.openntf.xsp.jakartaee.TestDatabase;

@SuppressWarnings("nls")
public class TestServletRest extends AbstractWebClientTest {
	@Test
	public void testSample() {
		Client client = getAnonymousClient();
		WebTarget target = client.target(getServletRestUrl(null, "/exampleservlet"));
		Response response = target.request().get();
		
		String output = response.readEntity(String.class);
		
		assertEquals("I am root resource.", output);
	}
	
	@Test
	public void testOldServlet() {
		Client client = getAnonymousClient();
		WebTarget target = client.target(getServletRestUrl(null, "/oldhelloservlet"));
		Response response = target.request().get();
		
		String output = response.readEntity(String.class);
		
		assertEquals("I am so very old.", output);
	}

	@Test
	public void testNsfPathSample() {
		Client client = getAnonymousClient();
		WebTarget target = client.target(getRootUrl(null, TestDatabase.BUNDLE) + "/exampleservlet");
		Response response = target.request().get();
		
		String output = response.readEntity(String.class);
		
		assertEquals("I am root resource.", output, () -> "Encountered unexpected output for " + target.getUri() + ":\n");
	}

	@Test
	public void testNsfPathOldServlet() {
		Client client = getAnonymousClient();
		WebTarget target = client.target(getRootUrl(null, TestDatabase.BUNDLE) + "/oldhelloservlet");
		Response response = target.request().get();
		
		String output = response.readEntity(String.class);
		
		assertEquals("I am so very old.", output, () -> "Encountered unexpected output for " + target.getUri() + ":\n");
	}
}
