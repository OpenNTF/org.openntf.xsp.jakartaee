package it.org.openntf.xsp.jakartaee.nsf.jaxrs;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Map;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.junit.jupiter.api.Test;

import com.ibm.commons.util.io.json.JsonException;
import com.ibm.commons.util.io.json.JsonJavaFactory;
import com.ibm.commons.util.io.json.JsonParser;

import it.org.openntf.xsp.jakartaee.AbstractWebClientTest;

@SuppressWarnings("nls")
public class TestRestDomino extends AbstractWebClientTest {
	/**
	 * Tests rest.DominoObjectsSample, which uses JAX-RS and CDI with Domino context objects.
	 * @throws JsonException 
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void testSample() throws JsonException {
		Client client = getAnonymousClient();
		WebTarget target = client.target(getRestUrl(null) + "/dominoObjects");
		Response response = target.request().get();
		
		String json = response.readEntity(String.class);
		Map<String, Object> jsonObject = (Map<String, Object>)JsonParser.fromJson(JsonJavaFactory.instance, json);
		
		String database = (String)jsonObject.get("database");
		assertNotNull(database);
		assertTrue(database.contains("XPagesDatabase"));
		
		String dominoSession = (String)jsonObject.get("dominoSession");
		assertNotNull(dominoSession);
		assertTrue(dominoSession.startsWith("lotus.domino.local.Session"));

		String sessionAsSigner = (String)jsonObject.get("sessionAsSigner");
		assertNotNull(sessionAsSigner);
		assertTrue(sessionAsSigner.startsWith("lotus.domino.local.Session"));

		String sessionAsSignerWithFullAccess = (String)jsonObject.get("sessionAsSignerWithFullAccess");
		assertNotNull(sessionAsSignerWithFullAccess);
		assertTrue(sessionAsSignerWithFullAccess.startsWith("lotus.domino.local.Session"));
	}
}
