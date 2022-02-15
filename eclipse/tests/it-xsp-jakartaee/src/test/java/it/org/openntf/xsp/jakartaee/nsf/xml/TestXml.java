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
package it.org.openntf.xsp.jakartaee.nsf.xml;

import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.junit.jupiter.api.Test;

import it.org.openntf.xsp.jakartaee.AbstractWebClientTest;

@SuppressWarnings("nls")
public class TestXml extends AbstractWebClientTest {
	
	@Test
	public void testXml() {
		Client client = getAnonymousClient();
		WebTarget target = client.target(getRestUrl(null) + "/sample/xml");
		Response response = target.request().get();
		
		String xml = response.readEntity(String.class);
		
		assertTrue(
			xml.startsWith("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><application-guy>"),
			() -> "Got unexpected content: " + xml
		);
	}
}
