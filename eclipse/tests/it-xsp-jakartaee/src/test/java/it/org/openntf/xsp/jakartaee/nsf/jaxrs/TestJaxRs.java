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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.ibm.commons.xml.DOMUtil;
import com.ibm.commons.xml.XMLException;

import it.org.openntf.xsp.jakartaee.AbstractWebClientTest;

@SuppressWarnings("nls")
public class TestJaxRs extends AbstractWebClientTest {
	/**
	 * Tests test.AdminRoleExample, which uses requires admin login
	 */
	@Test
	public void testSample() {
		Client client = getAnonymousClient();
		WebTarget target = client.target(getRestUrl(null) + "/sample");
		Response response = target.request().get();
		
		String output = response.readEntity(String.class);
		
		assertTrue(output.startsWith("I'm application guy at"), () -> "Received unexpected output: " + output);
	}
	
	/**
	 * Tests test.Sample#xml, which uses JAX-RS, CDI, and JAX-B.
	 */
	@Test
	public void testSampleXml() throws XMLException {
		Client client = getAnonymousClient();
		WebTarget target = client.target(getRestUrl(null) + "/sample/xml");
		Response response = target.request().get();
		
		String output = response.readEntity(String.class);
		
		Document xmlDoc = DOMUtil.createDocument(output);
		Element applicationGuy = xmlDoc.getDocumentElement();
		assertEquals("application-guy", applicationGuy.getTagName());
		Element time = (Element) applicationGuy.getElementsByTagName("time").item(0);
		assertFalse(time.getTextContent().isEmpty());
		Long.parseLong(time.getTextContent());
	}
}
