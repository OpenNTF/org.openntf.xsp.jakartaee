package it.org.openntf.xsp.jakartaee.nsf.jaxrs;

import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.junit.jupiter.api.Test;

import it.org.openntf.xsp.jakartaee.AbstractWebClientTest;

@SuppressWarnings("nls")
public class TestMailApi extends AbstractWebClientTest {
	/**
	 * Tests test.MailExample, which uses requires admin login
	 */
	@Test
	public void testDirect() {
		Client client = getAnonymousClient();
		WebTarget target = client.target(getRestUrl(null) + "/mail/multipart");
		Response response = target.request().get();
		
		String output = response.readEntity(String.class);
		
		assertTrue(output.startsWith("I am preamble"), () -> "Received unexpected output: " + output);
	}
}
