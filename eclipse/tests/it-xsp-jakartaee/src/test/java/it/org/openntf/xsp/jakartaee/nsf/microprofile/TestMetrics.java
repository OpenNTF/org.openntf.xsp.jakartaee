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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.ibm.commons.util.io.json.JsonException;
import com.ibm.commons.util.io.json.JsonJavaFactory;
import com.ibm.commons.util.io.json.JsonParser;

import it.org.openntf.xsp.jakartaee.AbstractWebClientTest;

@SuppressWarnings("nls")
public class TestMetrics extends AbstractWebClientTest {
	// Ensure that a basic URL has been hit
	@BeforeEach
	public void accessSample() {
		Client client = getAnonymousClient();
		WebTarget target = client.target(getRestUrl(null) + "/sample");
		Response response = target.request().get();
		response.readEntity(String.class);
	}
	
	@Test
	public void testMetrics() {
		Client client = getAnonymousClient();
		
		WebTarget target = client.target(getRestUrl(null) + "/metrics");
		Response response = target.request().get();
		
		String metrics = response.readEntity(String.class);
		assertTrue(metrics.contains("# TYPE application_rest_Sample_hello_total counter"), () -> metrics);
		assertTrue(metrics.contains("# TYPE application_rest_Sample_hello_elapsedTime_seconds gauge"), () -> metrics);
	}
	
	@Test
	public void testOptions() throws JsonException {
		Client client = getAnonymousClient();
		
		WebTarget target = client.target(getRestUrl(null) + "/metrics");
		Response response = target.request().options();
		
		String metrics = response.readEntity(String.class);
		@SuppressWarnings("unchecked")
		Map<String, Object> json = (Map<String, Object>)JsonParser.fromJson(JsonJavaFactory.instance, metrics);
		assertTrue(json.containsKey("base"));
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testRequestJson() throws JsonException {
		Client client = getAnonymousClient();
		
		WebTarget target = client.target(getRestUrl(null) + "/metrics");
		Response response = target.request()
			.accept(MediaType.APPLICATION_JSON_TYPE)
			.get();
		
		String metrics = response.readEntity(String.class);
		Map<String, Object> json = (Map<String, Object>)JsonParser.fromJson(JsonJavaFactory.instance, metrics);
		assertFalse(json.isEmpty(), () -> metrics);
		assertTrue(json.containsKey("application"));
		Map<String, Object> application = (Map<String, Object>)json.get("application");
		assertTrue(application.containsKey("rest.Sample.hello"));
		Map<String, Object> hello = (Map<String, Object>)application.get("rest.Sample.hello");
		assertTrue(hello.containsKey("count"));
		assertTrue(hello.get("count") instanceof Number);
		assertTrue(((Number)hello.get("count")).intValue() >= 1);
	}
}
