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
package it.org.openntf.xsp.jakartaee.nsf.basics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import it.org.openntf.xsp.jakartaee.AbstractWebClientTest;
import it.org.openntf.xsp.jakartaee.TestDatabase;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;

/**
 * Tests functionality of the JavaSapi bridge in the core modules.
 * 
 * @author Jesse Gallagher
 * @since 2.12.0
 */
@SuppressWarnings("nls")
public class TestJavaSapi extends AbstractWebClientTest {
	@Test
	public void testAddHeader() {
		Client client = getAdminClient();
		WebTarget target = client.target(getRootUrl(null, TestDatabase.MAIN)); //$NON-NLS-1$
		Response response = target.request().get();
		assertEquals("Hello", response.getHeaderString("X-AddHeaderJavaSapiService"));
	}
	
	@Test
	public void testAddInNSFHeader() {
		Client client = getAdminClient();
		WebTarget target = client.target(getRootUrl(null, TestDatabase.MAIN)); //$NON-NLS-1$
		Response response = target.request().get();
		assertEquals("Hello from NSF", response.getHeaderString("X-InNSFCustomHeader"));
	}
	
	@Test
	public void testOverrideUser() {
		Client client = getAnonymousClient();
		WebTarget target = client.target(getRootUrl(null, TestDatabase.MAIN) + "/whoami.xsp");
		Response response = target.request()
			.header("X-OverrideName", "CN=Foo Fooson/O=IKSG")
			.get();
		checkResponse(200, response);
		
		String html = response.readEntity(String.class);
		assertTrue(html.contains("I think you are:CN=Foo Fooson/O=IKSG"), () -> "Received unexpected HTML: " + html);
	}
}
