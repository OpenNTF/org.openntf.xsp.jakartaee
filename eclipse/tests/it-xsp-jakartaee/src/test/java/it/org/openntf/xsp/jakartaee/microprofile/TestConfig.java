package it.org.openntf.xsp.jakartaee.microprofile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
public class TestConfig extends AbstractWebClientTest {
	@SuppressWarnings("unchecked")
	@Test
	public void testConfig() throws JsonException {
		Client client = getAnonymousClient();
		WebTarget target = client.target(getRestUrl(null) + "/config");
		Response response = target.request().get();
		
		String json = response.readEntity(String.class);
		Map<String, Object> jsonObject = (Map<String, Object>)JsonParser.fromJson(JsonJavaFactory.instance, json);
		assertFalse(((String)jsonObject.get("java.version")).isEmpty(), () -> json);
		assertTrue(((String)jsonObject.get("xsp.library.depends")).startsWith("org.openntf.xsp.el"), () -> json);
		assertEquals("/local/notesdata", jsonObject.get("Directory"));
		assertEquals("foo", jsonObject.get("mpconfig.example.setting"));
	}
}
