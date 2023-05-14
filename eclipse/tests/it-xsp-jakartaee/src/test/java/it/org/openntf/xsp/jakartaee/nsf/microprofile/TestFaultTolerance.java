/**
 * Copyright (c) 2018-2023 Contributors to the XPages Jakarta EE Support Project
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

import org.junit.jupiter.api.Test;

import it.org.openntf.xsp.jakartaee.AbstractWebClientTest;
import it.org.openntf.xsp.jakartaee.TestDatabase;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;

@SuppressWarnings("nls")
public class TestFaultTolerance extends AbstractWebClientTest {
	@Test
	public void testRetry() {
		Client client = getAnonymousClient();
		WebTarget target = client.target(getRestUrl(null, TestDatabase.MAIN) + "/faultTolerance/retry");
		Response response = target.request().get();
		
		String result = response.readEntity(String.class);
		assertEquals("I am the fallback response.", result);
	}
	
	@Test
	public void testTimeout() {
		Client client = getAnonymousClient();
		WebTarget target = client.target(getRestUrl(null, TestDatabase.MAIN) + "/faultTolerance/timeout");
		Response response = target.request().get();
		
		String result = response.readEntity(String.class);
		assertFalse(result.contains("I should have stopped."));
		
		assertTrue(result.startsWith("org.eclipse.microprofile.faulttolerance.exceptions.TimeoutException: bean.FaultToleranceBean#getTimeout timed out"), () -> "Actual: " + result);
	}
	
	@Test
	public void testCircuitBreaker() {
		Client client = getAnonymousClient();
		WebTarget target = client.target(getRestUrl(null, TestDatabase.MAIN) + "/faultTolerance/circuitBreaker");
		
		// First try
		{
			Response response = target.request().get();
			
			String result = response.readEntity(String.class);
			assertNotEquals("I should have stopped.", result);

			assertTrue(result.startsWith("java.lang.RuntimeException: I am a circuit-breaking failure - I should stop after two attempts"), () -> "Actual:" + result);
		}
		
		// Second try - also "success"
		{
			Response response = target.request().get();
			
			String result = response.readEntity(String.class);
			assertNotEquals("I should have stopped.", result);

			assertTrue(result.startsWith("java.lang.RuntimeException: I am a circuit-breaking failure - I should stop after two attempts"), () -> "Actual:" + result);
		}
		
		// Third try - open breaker
		{
			Response response = target.request().get();
			
			String result = response.readEntity(String.class);
			assertNotEquals("I should have stopped.", result);

			assertTrue(result.startsWith("org.eclipse.microprofile.faulttolerance.exceptions.CircuitBreakerOpenException: bean.FaultToleranceBean#getCircuitBreaker circuit breaker is open"), () -> "Actual:" + result);
		}
	}
}
