package it.org.openntf.xsp.jakartaee.container.basics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import it.org.openntf.xsp.jakartaee.AbstractWebClientTest;
import it.org.openntf.xsp.jakartaee.TestDatabase;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;

@SuppressWarnings("nls")
public class TestContainerResources extends AbstractWebClientTest {

	@Test
	public void testIndexHtml() {
		Client client = getAnonymousClient();
		WebTarget target = client.target(getRootUrl(null, TestDatabase.OSGI_CONTAINER) + "/index.html");
		Response response = target.request().get();
		
		String body = response.readEntity(String.class);
		String expected = "<h1>Jakarta Web Container Example</h1>";
		assertTrue(body.contains(expected), () -> "Body should contain <" + expected + ">, got <" + body + ">");
	}
	
	@Test
	public void testMetaInfResources() {
		Client client = getAnonymousClient();
		WebTarget target = client.target(getRootUrl(null, TestDatabase.OSGI_CONTAINER) + "/outertextfile.txt");
		Response response = target.request().get();
		
		String body = response.readEntity(String.class);
		String expected = "I am stored in META-INF/resources";
		assertEquals(expected, body);
	}
	
	@Test
	public void testMetaInfResourcesEmbeddedJar() {
		Client client = getAnonymousClient();
		WebTarget target = client.target(getRootUrl(null, TestDatabase.OSGI_CONTAINER) + "/innertextfile.txt");
		Response response = target.request().get();
		
		String body = response.readEntity(String.class);
		String expected = "I am stored inside a classpath JAR in META-INF/resources";
		assertEquals(expected, body);
	}

}
