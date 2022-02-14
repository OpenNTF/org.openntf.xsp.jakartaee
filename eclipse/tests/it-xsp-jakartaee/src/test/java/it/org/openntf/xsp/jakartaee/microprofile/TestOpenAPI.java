package it.org.openntf.xsp.jakartaee.microprofile;

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
