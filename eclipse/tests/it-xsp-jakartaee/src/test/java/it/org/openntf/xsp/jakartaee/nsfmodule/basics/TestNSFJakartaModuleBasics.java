/*
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
package it.org.openntf.xsp.jakartaee.nsfmodule.basics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import it.org.openntf.xsp.jakartaee.AbstractWebClientTest;
import it.org.openntf.xsp.jakartaee.TestDatabase;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;

@SuppressWarnings("nls")
public class TestNSFJakartaModuleBasics extends AbstractWebClientTest {
	/**
	 * Tests that a module registered in the config but marked as disabled is
	 * not loaded
	 */
	@Test
	public void testDisabledModuleApp() {
		Client client = getAdminClient();
		WebTarget target = client.target(getRootUrl(null, TestDatabase.DISABLED_MODULE));
		Response response = target.request().get();
		checkResponse(404, response);
		String content = response.readEntity(String.class);
		assertTrue(content.contains("File not found or unable to read file"), () -> "Did not receive expected 404 page; got content: " + content);
	}
	
	/**
	 * Tests that a module registered to a specific host name only responds to that name
	 */
	@Test
	public void testHostNameMapping() {
		{
			Client client = getAdminClient();
			WebTarget target = client.target(getRootUrl(null, TestDatabase.HOST_MAPPED_MODULE) + "/chat.html");
			Response response = target.request()
				.header("Host", "somename.com")
				.get();
			checkResponse(404, response);
			assertEquals(
				"<HTML><HEAD><TITLE>Unable to Process Request</TITLE></HEAD><BODY><P>Http Status Code: 404</P><P>Reason: File not found or unable to read file</P></BODY></HTML>",
				response.readEntity(String.class)
			);
		}
		{
			Client client = getAdminClient();
			WebTarget target = client.target(getRootUrl(null, TestDatabase.HOST_MAPPED_MODULE) + "/chat.html");
			Response response = target.request()
				.header("Host", "foo.com")
				.get();
			assertEquals(200, response.getStatus(), "Host-mapped module should be available on a mapped host");
		}
	}
}
