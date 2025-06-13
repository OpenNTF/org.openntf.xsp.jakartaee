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
package it.org.openntf.xsp.jakartaee.nsf.nosql;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import it.org.openntf.xsp.jakartaee.AbstractWebClientTest;
import it.org.openntf.xsp.jakartaee.TestDatabase;
import it.org.openntf.xsp.jakartaee.providers.MainAndModuleProvider;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;

@SuppressWarnings("nls")
public class TestCustomPropertyTypeList extends AbstractWebClientTest {
	@ParameterizedTest
	@ArgumentsSource(MainAndModuleProvider.EnumOnly.class)
	public void testCustomPropertyList(TestDatabase db) {
		Client client = getAdminClient();
		WebTarget target = client.target(getRestUrl(null, db) + "/nosql/customPropertyList"); //$NON-NLS-1$
		
		JsonArray expected = Json.createArrayBuilder()
			.add(Json.createObjectBuilder().add("value", "foo"))
			.add(Json.createObjectBuilder().add("value", "bar"))
			.build();
		
		String id;
		{
			JsonObject payload = Json.createObjectBuilder()
				.add("customPropertyList", expected)
				.build();
			
			Response response = target.request().post(Entity.json(payload));
			JsonObject responseJson = response.readEntity(JsonObject.class);
			try {
				id = responseJson.getString("id");
				JsonArray actual = responseJson.getJsonArray("customPropertyList");
				assertEquals(expected, actual);
			} catch(Exception e) {
				fail("Encountered exception with JSON " + responseJson, e);
				throw e;
			}
		}
		
		{
			WebTarget getTarget = client.target(getRestUrl(null, db) + "/nosql/customPropertyList/" + id);
			Response response = getTarget.request().get();
			
			JsonObject responseJson = response.readEntity(JsonObject.class);
			try {
				JsonArray actual = responseJson.getJsonArray("customPropertyList");
				assertEquals(expected, actual, () -> "Failed with JSON " + responseJson);
			} catch(Exception e) {
				fail("Encountered exception with JSON " + responseJson, e);
			}
		}
	}

	@ParameterizedTest
	@ArgumentsSource(MainAndModuleProvider.EnumOnly.class)
	public void testJsonArrayStorage(TestDatabase db) {
		Client client = getAdminClient();
		WebTarget target = client.target(getRestUrl(null, db) + "/nosql/customPropertyList"); //$NON-NLS-1$
		
		JsonArray expected = Json.createArrayBuilder()
			.add(Json.createObjectBuilder().add("value", "foo"))
			.add(Json.createObjectBuilder().add("value", "bar"))
			.add("baz")
			.build();
		
		String id;
		{
			JsonObject payload = Json.createObjectBuilder()
				.add("jsonArrayStorage", expected)
				.build();
			
			Response response = target.request().post(Entity.json(payload));
			JsonObject responseJson = response.readEntity(JsonObject.class);
			try {
				id = responseJson.getString("id");
				JsonArray actual = responseJson.getJsonArray("jsonArrayStorage");
				assertEquals(expected, actual);
			} catch(Exception e) {
				fail("Encountered exception with JSON " + responseJson, e);
				throw e;
			}
		}
		
		{
			WebTarget getTarget = client.target(getRestUrl(null, db) + "/nosql/customPropertyList/" + id);
			Response response = getTarget.request().get();
			
			JsonObject responseJson = response.readEntity(JsonObject.class);
			try {
				JsonArray actual = responseJson.getJsonArray("jsonArrayStorage");
				assertEquals(expected, actual, () -> "Failed with JSON " + responseJson);
			} catch(Exception e) {
				fail("Encountered exception with JSON " + responseJson, e);
			}
		}
	}
}
