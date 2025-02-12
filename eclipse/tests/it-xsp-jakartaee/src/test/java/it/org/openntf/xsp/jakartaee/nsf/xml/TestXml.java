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
package it.org.openntf.xsp.jakartaee.nsf.xml;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;

import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import it.org.openntf.xsp.jakartaee.AbstractWebClientTest;
import it.org.openntf.xsp.jakartaee.TestDatabase;

@SuppressWarnings("nls")
public class TestXml extends AbstractWebClientTest {
	
	@Test
	public void testXml() {
		Client client = getAnonymousClient();
		WebTarget target = client.target(getRestUrl(null, TestDatabase.MAIN) + "/sample/xml");
		Response response = target.request().get();
		response.bufferEntity();
		
		String xml = String.valueOf(response.readEntity(String.class));
		
		assertTrue(
			xml.contains("<application-guy>"),
			() -> "Got unexpected content: " + xml
		);
		
		try {
			Document doc = response.readEntity(Document.class);
			Element root = doc.getDocumentElement();
			assertEquals("application-guy", root.getTagName());
			
			Element postConstruct = (Element)root.getElementsByTagName("postConstructSet").item(0);
			assertNotNull(postConstruct);
			assertEquals("I was set by postConstruct", postConstruct.getTextContent());
			
			Element startup = (Element)root.getElementsByTagName("startupSet").item(0);
			assertNotNull(startup);
			assertEquals("I was set by startup", startup.getTextContent());
		} catch(Exception e) {
			fail("Encountered exception working with response " + xml, e);
		}
	}
}
