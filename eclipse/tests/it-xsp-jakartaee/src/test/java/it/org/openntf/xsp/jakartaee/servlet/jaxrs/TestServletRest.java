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
}
