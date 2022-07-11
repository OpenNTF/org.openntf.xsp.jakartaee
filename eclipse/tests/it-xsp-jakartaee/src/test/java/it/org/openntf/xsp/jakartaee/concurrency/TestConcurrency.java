package it.org.openntf.xsp.jakartaee.concurrency;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import it.org.openntf.xsp.jakartaee.AbstractWebClientTest;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;

@SuppressWarnings("nls")
public class TestConcurrency extends AbstractWebClientTest {
	@Test
	public void testBasics() {
		Client client = getAnonymousClient();
		WebTarget target = client.target(getRestUrl(null) + "/concurrency");
		Response response = target.request().get();
		
		String output = response.readEntity(String.class);
		
		assertTrue(output.startsWith("Hello from executor\n"), () -> "Received unexpected output: " + output);
		assertTrue(output.contains("CDI is org"), () -> "Received unexpected output: " + output);
		assertTrue(output.contains("Username is: Anonymous"), () -> "Received unexpected output: " + output);
		assertTrue(output.contains("Database is: dev"), () -> "Received unexpected output: " + output);
	}

	@Test
	public void testBasicsAuthenticated() {
		Client client = getAdminClient();
		WebTarget target = client.target(getRestUrl(null) + "/concurrency");
		Response response = target.request().get();
		
		String output = response.readEntity(String.class);
		
		assertTrue(output.startsWith("Hello from executor\n"), () -> "Received unexpected output: " + output);
		assertTrue(output.contains("CDI is org"), () -> "Received unexpected output: " + output);
		assertTrue(output.contains("Username is: CN="), () -> "Received unexpected output: " + output);
		assertTrue(output.contains("Database is: dev"), () -> "Received unexpected output: " + output);
	}
}
