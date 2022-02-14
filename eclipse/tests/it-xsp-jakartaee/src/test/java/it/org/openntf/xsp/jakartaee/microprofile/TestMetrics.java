package it.org.openntf.xsp.jakartaee.microprofile;

import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.junit.jupiter.api.Test;

import it.org.openntf.xsp.jakartaee.AbstractWebClientTest;

@SuppressWarnings("nls")
public class TestMetrics extends AbstractWebClientTest {
	@Test
	public void testMetrics() {
		Client client = getAnonymousClient();
		
		// Ensure that a basic URL has been hit
		{
			WebTarget target = client.target(getRestUrl(null) + "/sample");
			Response response = target.request().get();
			response.readEntity(String.class);
		}
		
		WebTarget target = client.target(getRestUrl(null) + "/metrics");
		Response response = target.request().get();
		
		String metrics = response.readEntity(String.class);
		assertTrue(metrics.contains("# TYPE application_rest_Sample_hello_total counter"), () -> metrics);
		assertTrue(metrics.contains("# TYPE application_rest_Sample_hello_elapsedTime_seconds gauge"), () -> metrics);
	}
}
