package it.org.openntf.xsp.jakartaee.nsf.jaxrs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.text.MessageFormat;

import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataOutput;
import org.junit.jupiter.api.Test;

import it.org.openntf.xsp.jakartaee.AbstractWebClientTest;
import it.org.openntf.xsp.jakartaee.TestDatabase;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@SuppressWarnings("nls")
public class TestRestMultipart extends AbstractWebClientTest {
	@Test
	public void testMultipart() {
		Client client = getAnonymousClient();
		WebTarget target = client.target(getRestUrl(null, TestDatabase.MAIN) + "/restMultipart");
		
		String name = "Foo" + System.nanoTime();
		byte[] data = "hey hey".getBytes();
		
		MultipartFormDataOutput payload = new MultipartFormDataOutput();
		payload.addFormData("name", name, MediaType.TEXT_PLAIN_TYPE);
		payload.addFormData("part", data, MediaType.APPLICATION_OCTET_STREAM_TYPE);
		
		Response response = target.request()
			.accept(MediaType.APPLICATION_JSON_TYPE)
			.post(Entity.entity(payload, MediaType.MULTIPART_FORM_DATA_TYPE));
		checkResponse(200, response);
		String result = response.readEntity(String.class);
		String expected = MessageFormat.format("You sent me name={0} and a part of {1} bytes", name, data.length);
		assertEquals(expected, result);
	}
}
