package it.org.openntf.xsp.jakartaee.microprofile;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
public class TestRestClient extends AbstractWebClientTest {
	@SuppressWarnings("unchecked")
	@Test
//	@Disabled("Disabled pending figuring out local URLs")
	public void testRestClient() throws JsonException {
		Client client = getAnonymousClient();
		WebTarget target = client.target(getRestUrl(null) + "/restClient");
		Response response = target.request().get();
		
		String json = response.readEntity(String.class);
		Map<String, Object> jsonObject = (Map<String, Object>)JsonParser.fromJson(JsonJavaFactory.instance, json);
		Map<String, Object> responseObj = (Map<String, Object>)jsonObject.get("response");
		assertNotNull(responseObj, () -> json);
		assertEquals("bar", responseObj.get("foo"), () -> json);
	}
}
