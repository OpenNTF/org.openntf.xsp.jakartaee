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
package it.org.openntf.xsp.jakartaee.servlet.jaxrs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.junit.jupiter.api.Test;

import it.org.openntf.xsp.jakartaee.AbstractWebClientTest;

public class TestServletRest extends AbstractWebClientTest {
	@Test
	public void testSample() {
		Client client = getAnonymousClient();
		WebTarget target = client.target(getServletRestUrl(null));
		Response response = target.request().get();
		
		String output = response.readEntity(String.class);
		
		assertEquals("I am root resource.", output); //$NON-NLS-1$
	}

	@Test
	public void testNsfPathSample() {
		Client client = getAnonymousClient();
		WebTarget target = client.target(getBudleNsfRootUrl(null) + "/exampleservlet"); //$NON-NLS-1$
		System.out.println("testing " + target.getUri());
		Response response = target.request().get();
		
		String output = response.readEntity(String.class);
		
		assertEquals("I am root resource.", output); //$NON-NLS-1$
	}
}
