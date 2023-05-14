package it.org.openntf.xsp.jakartaee.nsf.basics;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import it.org.openntf.xsp.jakartaee.AbstractWebClientTest;
import it.org.openntf.xsp.jakartaee.TestDatabase;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;

/**
 * Tests functionality of the JavaSapi bridge in the core modules.
 * 
 * @author Jesse Gallagher
 * @since 2.12.0
 */
@SuppressWarnings("nls")
public class TestJavaSapi extends AbstractWebClientTest {
	@Test
	public void testAddHeader() {
		Client client = getAdminClient();
		WebTarget target = client.target(getRootUrl(null, TestDatabase.MAIN)); //$NON-NLS-1$
		Response response = target.request().get();
		assertEquals("Hello", response.getHeaderString("X-AddHeaderJavaSapiService"));
	}
}
