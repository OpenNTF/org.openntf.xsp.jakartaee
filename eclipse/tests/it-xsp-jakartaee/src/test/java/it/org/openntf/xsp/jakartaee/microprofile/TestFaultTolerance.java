package it.org.openntf.xsp.jakartaee.microprofile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
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
public class TestFaultTolerance extends AbstractWebClientTest {
	@Test
	public void testRetry() throws JsonException {
		Client client = getAnonymousClient();
		WebTarget target = client.target(getRestUrl(null) + "/faultTolerance/retry");
		Response response = target.request().get();
		
		String result = response.readEntity(String.class);
		assertEquals("I am the fallback response.", result);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testTimeout() throws JsonException {
		Client client = getAnonymousClient();
		WebTarget target = client.target(getRestUrl(null) + "/faultTolerance/timeout");
		Response response = target.request().get();
		
		String result = response.readEntity(String.class);
		assertNotEquals("I should have stopped.", result);

		Map<String, Object> jsonObject = (Map<String, Object>)JsonParser.fromJson(JsonJavaFactory.instance, result);
		assertTrue(jsonObject.containsKey("stackTrace"));
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testCircuitBreaker() throws JsonException {
		Client client = getAnonymousClient();
		WebTarget target = client.target(getRestUrl(null) + "/faultTolerance/circuitBreaker");
		
		// First try
		{
			Response response = target.request().get();
			
			String result = response.readEntity(String.class);
			assertNotEquals("I should have stopped.", result);

			Map<String, Object> jsonObject = (Map<String, Object>)JsonParser.fromJson(JsonJavaFactory.instance, result);
			assertEquals("java.lang.RuntimeException: I am a circuit-breaking failure - I should stop after two attempts", jsonObject.get("message"));
		}
		
		// Second try - also "success"
		{
			Response response = target.request().get();
			
			String result = response.readEntity(String.class);
			assertNotEquals("I should have stopped.", result);

			Map<String, Object> jsonObject = (Map<String, Object>)JsonParser.fromJson(JsonJavaFactory.instance, result);
			assertEquals("java.lang.RuntimeException: I am a circuit-breaking failure - I should stop after two attempts", jsonObject.get("message"));
		}
		
		// Third try - open breaker
		{
			Response response = target.request().get();
			
			String result = response.readEntity(String.class);
			assertNotEquals("I should have stopped.", result);

			Map<String, Object> jsonObject = (Map<String, Object>)JsonParser.fromJson(JsonJavaFactory.instance, result);
			assertEquals("org.eclipse.microprofile.faulttolerance.exceptions.CircuitBreakerOpenException: CircuitBreaker[bean.FaultToleranceBean#getCircuitBreaker] circuit breaker is open", jsonObject.get("message"));
		}
	}
}
