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
package it.org.openntf.xsp.jakartaee.nsf.validation;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.StringReader;

import org.junit.jupiter.api.Test;

import it.org.openntf.xsp.jakartaee.AbstractWebClientTest;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;

@SuppressWarnings("nls")
public class TestValidation extends AbstractWebClientTest {
	@Test
	public void testValid() {
		Client client = getAnonymousClient();
		WebTarget target = client.target(getRestUrl(null) + "/validation/valid");
		Response response = target.request().get();
		
		String json = response.readEntity(String.class);
		JsonArray violations = Json.createReader(new StringReader(json)).readArray();
		assertTrue(violations.isEmpty());
	}
	
	@Test
	public void testInvalid() {
		Client client = getAnonymousClient();
		WebTarget target = client.target(getRestUrl(null) + "/validation/invalid");
		Response response = target.request().get();
		
		String body = response.readEntity(String.class);
		assertTrue(body.startsWith("[ConstraintViolation"), () -> body);
	}
}
