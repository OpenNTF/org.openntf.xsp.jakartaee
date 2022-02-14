package it.org.openntf.xsp.jakartaee.xml;

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
