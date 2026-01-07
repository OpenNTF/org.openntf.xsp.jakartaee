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
package it.org.openntf.xsp.jakartaee.nsf.jaxrs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import it.org.openntf.xsp.jakartaee.AbstractWebClientTest;
import it.org.openntf.xsp.jakartaee.TestDatabase;
import it.org.openntf.xsp.jakartaee.providers.MainAndModuleProvider;

@SuppressWarnings("nls")
public class TestAdminRole extends AbstractWebClientTest {
	/**
	 * Tests rest.AdminRoleExample, which uses {@code @RolesAllowed} to
	 * require [Admin].
	 */
	@ParameterizedTest
	@ArgumentsSource(MainAndModuleProvider.EnumOnly.class)
	public void testAdminRole(TestDatabase db) {
		// Anonymous should get a login form
		{
			Client client = getAnonymousClient();
			WebTarget target = client.target(getRestUrl(null, db) + "/adminrole");
			Response response = target.request().get();
			checkResponse(401, response);
			
			String html = response.readEntity(String.class);
			assertNotNull(html);
			assertTrue(html.contains("/names.nsf?Login"));
		}
		// Admin should get basic text
		{
			Client client = getAdminClient();
			WebTarget target = client.target(getRestUrl(null, db) + "/adminrole");
			Response response = target.request().get();
			checkResponse(200, response);
			
			String html = response.readEntity(String.class);
			assertNotNull(html);
			assertTrue(html.startsWith("I think you're an admin"), () -> "Received: " + html);
		}
	}
	
	/**
	 * Tests rest.AdminRoleExample, which uses {@code @RolesAllowed} to
	 * require an invalid user. 
	 */
	@ParameterizedTest
	@ArgumentsSource(MainAndModuleProvider.EnumOnly.class)
	public void testInvalidUser(TestDatabase db) {
		// Anonymous should get a login form
		{
			Client client = getAnonymousClient();
			WebTarget target = client.target(getRestUrl(null, db) + "/adminrole/invaliduser");
			Response response = target.request().get();
			checkResponse(401, response);
			
			String html = response.readEntity(String.class);
			assertNotNull(html);
			assertTrue(html.contains("/names.nsf?Login"), () -> "Received unexpected HTML " + html);
		}
		// Admin should also get a login form
		{
			Client client = getAdminClient();
			WebTarget target = client.target(getRestUrl(null, db) + "/adminrole/invaliduser");
			Response response = target.request().get();
			checkResponse(401, response);
			
			String html = response.readEntity(String.class);
			assertNotNull(html);
			assertTrue(html.contains("/names.nsf?Login"), () -> "Received unexpected HTML " + html);
		}
	}
	
	/**
	 * Tests rest.AdminRoleExample#getLoginRole, which uses {@code @RolesAllowed} to
	 * require any authenticated user
	 */
	@ParameterizedTest
	@ArgumentsSource(MainAndModuleProvider.EnumOnly.class)
	public void testLoginRole(TestDatabase db) {
		// Anonymous should get a login form
		{
			Client client = getAnonymousClient();
			WebTarget target = client.target(getRestUrl(null, db) + "/adminrole/login");
			Response response = target.request().get();
			checkResponse(401, response);
			
			String html = response.readEntity(String.class);
			assertNotNull(html);
			assertTrue(html.contains("/names.nsf?Login"));
		}
		// Admin should get basic text
		{
			Client client = getAdminClient();
			WebTarget target = client.target(getRestUrl(null, db) + "/adminrole/login");
			Response response = target.request().get();
			checkResponse(200, response);
			
			String html = response.readEntity(String.class);
			assertNotNull(html);
			assertEquals("I think you're an authenticated user", html);
		}
	}
}
