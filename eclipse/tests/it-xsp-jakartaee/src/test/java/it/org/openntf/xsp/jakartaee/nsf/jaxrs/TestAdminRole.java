/**
 * Copyright © 2018-2022 Jesse Gallagher
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

import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import it.org.openntf.xsp.jakartaee.AbstractWebClientTest;

@SuppressWarnings("nls")
public class TestAdminRole extends AbstractWebClientTest {
	/**
	 * Tests rest.AdminRoleExample, which uses {@code @RolesAllowed} to
	 * require [Admin].
	 */
	@Test
	@Disabled("This is currently unreliable - likely a timing issue in server launch")
	public void testAdminRole() {
		// Anonymous should get a login form
		{
			Client client = getAnonymousClient();
			WebTarget target = client.target(getRestUrl(null) + "/adminrole");
			Response response = target.request().get();
			
			String html = response.readEntity(String.class);
			assertNotNull(html);
			assertTrue(html.contains("/names.nsf?Login"));
		}
		// Admin should get basic text
		{
			Client client = getAdminClient();
			WebTarget target = client.target(getRestUrl(null) + "/adminrole");
			Response response = target.request().get();
			
			String html = response.readEntity(String.class);
			assertNotNull(html);
			assertEquals("I think you're an admin!", html);
		}
	}
	
	/**
	 * Tests rest.AdminRoleExample, which uses {@code @RolesAllowed} to
	 * require an invalid user. 
	 */
	@Test
	public void testInvalidUser() {
		// Anonymous should get a login form
		{
			Client client = getAnonymousClient();
			WebTarget target = client.target(getRestUrl(null) + "/adminrole/invaliduser");
			Response response = target.request().get();
			
			String html = response.readEntity(String.class);
			assertNotNull(html);
			assertTrue(html.contains("/names.nsf?Login"));
		}
		// Admin should also get a login form
		{
			Client client = getAdminClient();
			WebTarget target = client.target(getRestUrl(null) + "/adminrole/invaliduser");
			Response response = target.request().get();
			
			String html = response.readEntity(String.class);
			assertNotNull(html);
			assertTrue(html.contains("/names.nsf?Login"));
		}
	}
}
