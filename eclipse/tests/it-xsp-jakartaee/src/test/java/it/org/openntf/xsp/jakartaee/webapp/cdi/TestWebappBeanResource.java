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
package it.org.openntf.xsp.jakartaee.webapp.cdi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
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

@SuppressWarnings({ "nls" })
public class TestWebappBeanResource extends AbstractWebClientTest {
	
	@Test
	public void testBundleBean() {
		int expectedIdentity = 0;
		// First NSF - uses .cdibundle
		{
			JsonObject obj = getBean(getRootUrl(null, TestDatabase.OSGI_WEBAPP) + "/cdiServlet");
			assertEquals("Hello from webappBean", obj.getString("hello"));
			expectedIdentity = obj.getInt("identity");
			assertNotEquals(0, expectedIdentity);
		}
		// Call this again to ensure that it uses the same bean
		{
			JsonObject obj = getBean(getRootUrl(null, TestDatabase.OSGI_WEBAPP) + "/cdiServlet");
			assertEquals("Hello from webappBean", obj.getString("hello"));
			int identity = obj.getInt("identity");
			assertEquals(expectedIdentity, identity);
		}
	}
	
	private JsonObject getBean(String base) {
		Client client = getAnonymousClient();
		WebTarget target = client.target(base);
		Response response = target.request().get();
		
		checkResponse(200, response);
		String output = response.readEntity(String.class);
		try {
			return Json.createReader(new StringReader(output)).readObject();
		} catch(Exception e) {
			fail("Exception parsing JSON: " + output, e);
			return null;
		}
	}
}
