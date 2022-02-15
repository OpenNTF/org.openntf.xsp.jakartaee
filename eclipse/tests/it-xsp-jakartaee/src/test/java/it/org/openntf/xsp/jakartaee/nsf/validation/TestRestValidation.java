package it.org.openntf.xsp.jakartaee.nsf.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.junit.jupiter.api.Test;

import it.org.openntf.xsp.jakartaee.AbstractWebClientTest;

@SuppressWarnings("nls")
public class TestRestValidation extends AbstractWebClientTest {
	@Test
	public void testValid() {
		Client client = getAnonymousClient();
		WebTarget target = client.target(getRestUrl(null) + "/validation/requestValidation?requiredField=hello");
		Response response = target.request().get();
		
		String body = response.readEntity(String.class);
		assertEquals("Required field is: hello", body);
	}
	
	@Test
	public void testInvalid() {
		Client client = getAnonymousClient();
		WebTarget target = client.target(getRestUrl(null) + "/validation/requestValidation");
		Response response = target.request("text/html","application/xhtml+xml","application/xml","*/*").get();
		
		String body = response.readEntity(String.class);
		assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
				+ "<violationReport>"
				+ "<parameterViolations>"
				+ "<constraintType>PARAMETER</constraintType>"
				+ "<path>getRequestParam.requiredField</path>"
				+ "<message>must not be empty</message>"
				+ "<value></value>"
				+ "</parameterViolations>"
				+ "</violationReport>", body);
	}
}
