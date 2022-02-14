package it.org.openntf.xsp.jakartaee.jaxrs;

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
