package it.org.openntf.xsp.jakartaee.nsf.jsp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;

import com.ibm.commons.util.StringUtil;

import it.org.openntf.xsp.jakartaee.AbstractWebClientTest;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;

/**
 * @author Jesse Gallagher
 * @since 2.5.0
 */
@SuppressWarnings("nls")
public class TestJsp extends AbstractWebClientTest {
	/**
	 * Tests to ensure that a JSP file that doesn't exist leads to a
	 * non-empty 404 page.
	 */
	@Test
	public void testNotFound() {
		Client client = getAnonymousClient();
		WebTarget target = client.target(getRootUrl(null) + "/somefakepage.jsp");
		Response response = target.request().get();
		
		assertEquals(404, response.getStatus());
		
		String content = response.readEntity(String.class);
		assertFalse(StringUtil.isEmpty(content));
	}
}
