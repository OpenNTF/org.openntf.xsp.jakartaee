/**
 * Copyright © 2018-2022 Jesse Gallagher
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

import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.junit.jupiter.api.Test;

import it.org.openntf.xsp.jakartaee.AbstractWebClientTest;

@SuppressWarnings("nls")
public class TestMetrics extends AbstractWebClientTest {
	@Test
	public void testMetrics() {
		Client client = getAnonymousClient();
		
		// Ensure that a basic URL has been hit
		{
			WebTarget target = client.target(getRestUrl(null) + "/sample");
			Response response = target.request().get();
			response.readEntity(String.class);
		}
		
		WebTarget target = client.target(getRestUrl(null) + "/metrics");
		Response response = target.request().get();
		
		String metrics = response.readEntity(String.class);
		assertTrue(metrics.contains("# TYPE application_rest_Sample_hello_total counter"), () -> metrics);
		assertTrue(metrics.contains("# TYPE application_rest_Sample_hello_elapsedTime_seconds gauge"), () -> metrics);
	}
}
