/**
 * Copyright © 2018-2022 Contributors to the XPages Jakarta EE Support Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package it.org.openntf.xsp.jakartaee.nsf.microprofile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.StringReader;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;

import org.junit.jupiter.api.Test;

import it.org.openntf.xsp.jakartaee.AbstractWebClientTest;

@SuppressWarnings("nls")
public class TestFaultTolerance extends AbstractWebClientTest {
	@Test
	public void testRetry() {
		Client client = getAnonymousClient();
		WebTarget target = client.target(getRestUrl(null) + "/faultTolerance/retry");
		Response response = target.request().get();
		
		String result = response.readEntity(String.class);
		assertEquals("I am the fallback response.", result);
	}
	
	@Test
	public void testTimeout() {
		Client client = getAnonymousClient();
		WebTarget target = client.target(getRestUrl(null) + "/faultTolerance/timeout");
		Response response = target.request().get();
		
		String result = response.readEntity(String.class);
		assertFalse(result.contains("I should have stopped."));

		JsonObject jsonObject = Json.createReader(new StringReader(result)).readObject();
		assertTrue(jsonObject.containsKey("stackTrace"));
	}
	
	@Test
	public void testCircuitBreaker() {
		Client client = getAnonymousClient();
		WebTarget target = client.target(getRestUrl(null) + "/faultTolerance/circuitBreaker");
		
		// First try
		{
			Response response = target.request().get();
			
			String result = response.readEntity(String.class);
			assertNotEquals("I should have stopped.", result);

			JsonObject jsonObject = Json.createReader(new StringReader(result)).readObject();
			assertEquals("java.lang.RuntimeException: I am a circuit-breaking failure - I should stop after two attempts", jsonObject.getString("message"));
		}
		
		// Second try - also "success"
		{
			Response response = target.request().get();
			
			String result = response.readEntity(String.class);
			assertNotEquals("I should have stopped.", result);

			JsonObject jsonObject = Json.createReader(new StringReader(result)).readObject();
			assertEquals("java.lang.RuntimeException: I am a circuit-breaking failure - I should stop after two attempts", jsonObject.getString("message"));
		}
		
		// Third try - open breaker
		{
			Response response = target.request().get();
			
			String result = response.readEntity(String.class);
			assertNotEquals("I should have stopped.", result);

			JsonObject jsonObject = Json.createReader(new StringReader(result)).readObject();
			assertEquals("org.eclipse.microprofile.faulttolerance.exceptions.CircuitBreakerOpenException: CircuitBreaker[bean.FaultToleranceBean#getCircuitBreaker] circuit breaker is open", jsonObject.getString("message"));
		}
	}
}
