package it.org.openntf.xsp.jakartaee.nsf.servlet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import it.org.openntf.xsp.jakartaee.AbstractWebClientTest;
import it.org.openntf.xsp.jakartaee.TestDatabase;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;

@SuppressWarnings("nls")
public class TestInitializer extends AbstractWebClientTest {
	@Test
	public void testInitializerServlet() {
		Client client = getAnonymousClient();
		WebTarget target = client.target(getRootUrl(null, TestDatabase.MAIN_MODULE) + TestDatabase.MAIN_MODULE.getXspPrefix()+"/initializerResource");
		Response response = target.request().get();
		checkResponse(200, response);
		
		String body = response.readEntity(String.class);
		assertTrue(body.contains("I found 1 class"), () -> "Body should contain the expected message, got: " + body);
	}
	
	@Test
	public void testUnusedInitializerServlet() {
		Client client = getAnonymousClient();
		WebTarget target = client.target(getRootUrl(null, TestDatabase.JPA) + TestDatabase.JPA.getXspPrefix()+"/initializerResource");
		Response response = target.request().get();
		checkResponse(200, response);
		
		String body = response.readEntity(String.class);
		assertEquals("null", body);
	}
}
