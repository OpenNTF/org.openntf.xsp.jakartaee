package it.org.openntf.xsp.jakartaee.nsf.validation;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.junit.jupiter.api.Test;

import com.ibm.commons.util.io.json.JsonException;
import com.ibm.commons.util.io.json.JsonJavaFactory;
import com.ibm.commons.util.io.json.JsonParser;

import it.org.openntf.xsp.jakartaee.AbstractWebClientTest;

@SuppressWarnings("nls")
public class TestValidation extends AbstractWebClientTest {
	@SuppressWarnings("unchecked")
	@Test
	public void testValid() throws JsonException {
		Client client = getAnonymousClient();
		WebTarget target = client.target(getRestUrl(null) + "/validation/valid");
		Response response = target.request().get();
		
		String json = response.readEntity(String.class);
		List<Object> violations = (List<Object>)JsonParser.fromJson(JsonJavaFactory.instance, json);
		assertTrue(violations.isEmpty());
	}
	
	@Test
	public void testInvalid() throws JsonException {
		Client client = getAnonymousClient();
		WebTarget target = client.target(getRestUrl(null) + "/validation/invalid");
		Response response = target.request().get();
		
		String body = response.readEntity(String.class);
		assertTrue(body.startsWith("[ConstraintViolation"), () -> body);
	}
}
