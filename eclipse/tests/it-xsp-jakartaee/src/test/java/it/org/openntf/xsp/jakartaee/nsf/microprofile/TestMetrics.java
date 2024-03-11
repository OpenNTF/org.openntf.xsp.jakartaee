/**
 * Copyright (c) 2018-2024 Contributors to the XPages Jakarta EE Support Project
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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.StringReader;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import it.org.openntf.xsp.jakartaee.AbstractWebClientTest;
import it.org.openntf.xsp.jakartaee.TestDatabase;

@SuppressWarnings("nls")
public class TestMetrics extends AbstractWebClientTest {
	// Ensure that a basic URL has been hit
	@BeforeEach
	public void accessSample() {
		Client client = getAnonymousClient();
		WebTarget target = client.target(getRestUrl(null, TestDatabase.MAIN) + "/sample");
		Response response = target.request().get();
		response.readEntity(String.class);
	}
	
	@Test
	public void testMetrics() {
		Client client = getAnonymousClient();
		
		WebTarget target = client.target(getRestUrl(null, TestDatabase.MAIN) + "/metrics");
		Response response = target.request().get();
		
		String metrics = response.readEntity(String.class);
		assertTrue(metrics.contains("# TYPE application_rest_Sample_hello_total counter"), () -> metrics);
		assertTrue(metrics.contains("# TYPE application_rest_Sample_hello_elapsedTime_seconds gauge"), () -> metrics);
	}
	
	@Test
	public void testOptions() {
		Client client = getAnonymousClient();
		
		WebTarget target = client.target(getRestUrl(null, TestDatabase.MAIN) + "/metrics");
		Response response = target.request().options();
		
		String metrics = response.readEntity(String.class);
		JsonObject json = Json.createReader(new StringReader(metrics)).readObject();
		assertTrue(json.containsKey("base"));
	}
	
	@Test
	public void testRequestJson() {
		Client client = getAnonymousClient();
		
		WebTarget target = client.target(getRestUrl(null, TestDatabase.MAIN) + "/metrics");
		Response response = target.request()
			.accept(MediaType.APPLICATION_JSON_TYPE)
			.get();
		
		String metrics = response.readEntity(String.class);
		JsonObject json = Json.createReader(new StringReader(metrics)).readObject();
		assertFalse(json.isEmpty(), () -> metrics);
		assertTrue(json.containsKey("application"));
		JsonObject application = json.getJsonObject("application");
		assertTrue(application.containsKey("rest.Sample.hello"));
		JsonObject hello = application.getJsonObject("rest.Sample.hello");
		assertTrue(hello.containsKey("count"));
		assertTrue(hello.getInt("count") >= 1);
	}
}
