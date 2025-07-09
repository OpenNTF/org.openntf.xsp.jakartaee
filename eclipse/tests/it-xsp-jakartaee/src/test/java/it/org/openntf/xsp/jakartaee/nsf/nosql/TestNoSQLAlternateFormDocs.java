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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.StringReader;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import it.org.openntf.xsp.jakartaee.AbstractWebClientTest;
import it.org.openntf.xsp.jakartaee.TestDatabase;
import it.org.openntf.xsp.jakartaee.providers.MainAndModuleProvider;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;

@SuppressWarnings("nls")
public class TestNoSQLAlternateFormDocs extends AbstractWebClientTest {
	@ParameterizedTest
	@ArgumentsSource(MainAndModuleProvider.EnumOnly.class)
	public void testAlternateFormDoc(TestDatabase db) {
		Client client = getAdminClient();
		
		// Create a new doc
		String unid;
		String name = "foo" + System.currentTimeMillis();
		{
			JsonObject payload = Json.createObjectBuilder()
				.add("name", name)
				.build();
			
			WebTarget postTarget = client.target(getRestUrl(null, db) + "/exampleAlternateFormDocs");
			Response response = postTarget.request().post(Entity.json(payload));
			checkResponse(200, response);

			String json = response.readEntity(String.class);
			JsonObject jsonObject = Json.createReader(new StringReader(json)).readObject();
			unid = jsonObject.getString("unid");
			assertNotNull(unid);
			assertFalse(unid.isEmpty());
			assertEquals(name, jsonObject.getString("name"));
		}
		
		// Make sure it's in the view
		{
			WebTarget target = client.target(getRestUrl(null, db) + "/exampleAlternateFormDocs");
			Response response = target.request().get();
			checkResponse(200, response);
			String json = response.readEntity(String.class);

			JsonArray jsonArray = Json.createReader(new StringReader(json)).readArray();
			assertFalse(jsonArray.isEmpty());
			
			JsonObject jsonObject = jsonArray.stream()
				.map(JsonValue::asJsonObject)
				.filter(o -> name.equals(o.getString("name")))
				.findFirst()
				.orElseThrow(() -> new IllegalStateException("Did not find object in " + json));
			assertEquals(unid, jsonObject.getString("unid"));
			assertEquals(name, jsonObject.getString("name"));
		}
		
		// Check the count
		int count;
		{
			WebTarget target = client.target(getRestUrl(null, TestDatabase.MAIN) + "/exampleAlternateFormDocs/@count");
			Response response = target.request().get();
			checkResponse(200, response);
			String countText = response.readEntity(String.class);
			count = Integer.parseInt(countText);
			assertTrue(count > 0);
		}
		
		// Update it and make sure the UNID is the same
		{
			JsonObject payload = Json.createObjectBuilder()
				.add("name", name + "hi")
				.build();
			
			WebTarget target = client.target(getRestUrl(null, TestDatabase.MAIN) + "/exampleAlternateFormDocs/" + unid);
			Response response = target.request().put(Entity.json(payload));
			checkResponse(200, response);
			String json = response.readEntity(String.class);

			JsonObject jsonObject = Json.createReader(new StringReader(json)).readObject();
			assertEquals(unid, jsonObject.getString("unid"));
			assertEquals(name + "hi", jsonObject.getString("name"));
		}
		
		// Make sure it's in the non-view-based search
		{
			WebTarget target = client.target(getRestUrl(null, TestDatabase.MAIN) + "/exampleAlternateFormDocs");
			Response response = target.request().get();
			checkResponse(200, response);
			String json = response.readEntity(String.class);

			JsonArray jsonArray = Json.createReader(new StringReader(json)).readArray();
			assertFalse(jsonArray.isEmpty());
			
			JsonObject jsonObject = jsonArray.stream()
				.map(JsonValue::asJsonObject)
				.filter(o -> (name + "hi").equals(o.getString("name")))
				.findFirst()
				.orElseThrow(() -> new IllegalStateException("Did not find object in " + json));
			assertEquals(unid, jsonObject.getString("unid"));
			assertEquals(name + "hi", jsonObject.getString("name"));
		}
		
		// Check the count again
		{
			WebTarget target = client.target(getRestUrl(null, TestDatabase.MAIN) + "/exampleAlternateFormDocs/@count");
			Response response = target.request().get();
			checkResponse(200, response);
			String countText = response.readEntity(String.class);
			assertEquals(Integer.toString(count), countText);
		}
		
		// Create a second to ensure the count was true
		{
			JsonObject payload = Json.createObjectBuilder()
				.add("name", name + "two")
				.build();
			
			WebTarget postTarget = client.target(getRestUrl(null, TestDatabase.MAIN) + "/exampleAlternateFormDocs");
			Response response = postTarget.request().post(Entity.json(payload));
			checkResponse(200, response);

			String json = response.readEntity(String.class);
			JsonObject jsonObject = Json.createReader(new StringReader(json)).readObject();
			unid = jsonObject.getString("unid");
			assertNotNull(unid);
			assertFalse(unid.isEmpty());
			assertEquals(name + "two", jsonObject.getString("name"));
		}
		{
			WebTarget target = client.target(getRestUrl(null, TestDatabase.MAIN) + "/exampleAlternateFormDocs/@count");
			Response response = target.request().get();
			checkResponse(200, response);
			String countText = response.readEntity(String.class);
			assertEquals(count+1, Integer.valueOf(countText));
		}
	}
}
