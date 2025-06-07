package it.org.openntf.xsp.jakartaee.nsf.basics;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import it.org.openntf.xsp.jakartaee.AbstractWebClientTest;
import it.org.openntf.xsp.jakartaee.TestDatabase;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;

@SuppressWarnings("nls")
public class TestNSFJakartaModuleBasics extends AbstractWebClientTest {
	/**
	 * Tests that a module registered in the config but marked as disabled is
	 * not loaded
	 */
	@Test
	public void testDisabledModuleApp() {
		Client client = getAdminClient();
		WebTarget target = client.target(getRootUrl(null, TestDatabase.DISABLED_MODULE));
		Response response = target.request().get();
		checkResponse(404, response);
		String content = response.readEntity(String.class);
		assertTrue(content.contains("File not found or unable to read file"), () -> "Did not receive expected 404 page; got content: " + content);
	}
}
