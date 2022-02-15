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
package it.org.openntf.xsp.jakartaee.nsf.microprofile;

import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.junit.jupiter.api.Test;

import it.org.openntf.xsp.jakartaee.AbstractWebClientTest;

@SuppressWarnings("nls")
public class TestOpenAPI extends AbstractWebClientTest {
	@Test
	public void testOpenAPI() {
		Client client = getAnonymousClient();
		WebTarget target = client.target(getRestUrl(null) + "/openapi");
		Response response = target.request().get();
		
		String yaml = response.readEntity(String.class);
		assertTrue(yaml.startsWith("---\nopenapi: 3.0"), () -> yaml);
		assertTrue(yaml.contains("  /adminrole:"));
	}
}
