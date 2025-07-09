/**
 * Copyright (c) 2018-2025 Contributors to the XPages Jakarta EE Support Project
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
package it.org.openntf.xsp.jakartaee.nsf.json;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.StringReader;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import it.org.openntf.xsp.jakartaee.AbstractWebClientTest;
import it.org.openntf.xsp.jakartaee.TestDatabase;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue.ValueType;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;

@SuppressWarnings("nls")
public class TestRestJsonConfig extends AbstractWebClientTest {
	@ParameterizedTest
	@ArgumentsSource(AnonymousClientProvider.class)
	public void testJsonb(Client client) {
		WebTarget target = client.target(getRestUrl(null, TestDatabase.JSONB_CONFIG) + "/json");
		Response response = target.request().get();
		
		String json = response.readEntity(String.class);
		try {
			JsonObject jsonObject = Json.createReader(new StringReader(json)).readObject();
			assertFalse(jsonObject.containsKey("jsonMessage"), () -> "JSON should not contain jsonMessage; got " + json);
			assertTrue(jsonObject.containsKey("time"));
			assertEquals(ValueType.NUMBER, jsonObject.get("time").getValueType());
		} catch(Exception e) {
			fail("Encountered exception parsing " + json, e);
		}
	}
}
