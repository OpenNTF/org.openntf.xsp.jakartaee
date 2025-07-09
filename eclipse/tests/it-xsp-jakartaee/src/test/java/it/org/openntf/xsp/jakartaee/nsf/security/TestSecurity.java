package it.org.openntf.xsp.jakartaee.nsf.security;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import it.org.openntf.xsp.jakartaee.AbstractWebClientTest;
import it.org.openntf.xsp.jakartaee.TestDatabase;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;

@SuppressWarnings("nls")
public class TestSecurity extends AbstractWebClientTest {

	@Test
	@Disabled("This appears to be flaky in practice, and that may be on the Domino side")
	public void testOverrideUser() {
		Client client = getAnonymousClient();
		WebTarget target = client.target(getRestUrl(null, TestDatabase.MAIN) + "/security");
		Response response = target.request()
			.header("X-MySpecialHeader", "CN=Joe Schmoe/O=SomeOrg")
			.get();
		checkResponse(200, response);
		
		String html = response.readEntity(String.class);
		assertTrue(html.equals("good for you, CN=Joe Schmoe/O=SomeOrg"), () -> "Received unexpected response: " + html);
	}

}
