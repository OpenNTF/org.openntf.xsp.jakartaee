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
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import it.org.openntf.xsp.jakartaee.AbstractWebClientTest;
import it.org.openntf.xsp.jakartaee.TestDatabase;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;

@SuppressWarnings("nls")
public class TestNoSQLNamedAndProfileDocs extends AbstractWebClientTest {
	private static final String PART_NAMED = "nosqlNamedDocs";
	private static final String NAME_NAMED = "NamedDoc";
	private static final String USERNAME_NAMED = "CN=Foo Fooson/O=SomeOrg";
	private static final String USERNAME2_NAMED = "CN=Bar Barson/O=SomeOrg";
	private static final String FIELD_NAMED = "noteName";

	private static final String PART_PROFILE = "nosqlProfileDocs";
	private static final String NAME_PROFILE = "ProfileDoc";
	private static final String USERNAME_PROFILE = "CN=Profile Fooson/O=SomeOrg";
	private static final String USERNAME2_PROFILE = "CN=Profile Barson/O=SomeOrg";
	private static final String FIELD_PROFILE = "profileName";
	
	@ParameterizedTest
	@CsvSource({
		PART_NAMED + "," + NAME_NAMED + "," + USERNAME_NAMED + "," + USERNAME2_NAMED + "," + FIELD_NAMED,
		PART_PROFILE + "," + NAME_PROFILE + "," + USERNAME_PROFILE + "," + USERNAME2_PROFILE + "," + FIELD_PROFILE
	})
	public void testDoc(String part, String name, String username, String username2, String field) throws UnsupportedEncodingException {
		Client client = getAnonymousClient();

		WebTarget target = client.target(getRestUrl(null, TestDatabase.MAIN) + "/" + part + "/" + name);
		
		// Make sure it exists in a blank form at first
		{
			Response response = target.request().get();
			checkResponse(200, response);
			
			String json = response.readEntity(String.class);
			JsonObject jsonObject = Json.createReader(new StringReader(json)).readObject();
			assertEquals(name, jsonObject.getString(field, ""), () -> "Received unexpected JSON: " + json);
			assertEquals("", jsonObject.getString("noteUserName", ""), () -> "Received unexpected JSON: " + json);
			assertEquals("", jsonObject.getString("subject", ""), () -> "Received unexpected JSON: " + json);
		}
		
		// Update with new data
		{
			JsonObject entity = Json.createObjectBuilder()
				.add("subject", "Hey I'm subject")
				.build();
			
			Response response = target.request().put(Entity.json(entity));
			checkResponse(200, response);
			
			String json = response.readEntity(String.class);
			JsonObject jsonObject = Json.createReader(new StringReader(json)).readObject();
			assertEquals(name, jsonObject.getString(field, ""));
			assertEquals("", jsonObject.getString("noteUserName", ""));
			assertEquals("Hey I'm subject", jsonObject.getString("subject", ""));
		}
		
		// Fetch it again to make sure it saved
		{
			Response response = target.request().get();
			checkResponse(200, response);
			
			String json = response.readEntity(String.class);
			JsonObject jsonObject = Json.createReader(new StringReader(json)).readObject();
			assertEquals(name, jsonObject.getString(field, ""));
			assertEquals("", jsonObject.getString("noteUserName", ""));
			assertEquals("Hey I'm subject", jsonObject.getString("subject", ""));
		}
		
		// Update it, as it should go through a different code path
		{
			JsonObject patch = Json.createObjectBuilder()
					.add("subject", "I am the patched subject")
					.build();
			
			Response response = target.request().method("PATCH", Entity.json(patch));
			checkResponse(200, response);
			
			String json = response.readEntity(String.class);
			JsonObject jsonObject = Json.createReader(new StringReader(json)).readObject();
			assertEquals(name, jsonObject.getString(field, ""));
			assertEquals("", jsonObject.getString("noteUserName", ""), () -> "Received unexpected JSON: " + json);
			assertEquals("I am the patched subject", jsonObject.getString("subject", ""));
		}
		
		// Fetch it again to make sure it the patch worked
		{
			Response response = target.request().get();
			checkResponse(200, response);
			
			String json = response.readEntity(String.class);
			JsonObject jsonObject = Json.createReader(new StringReader(json)).readObject();
			assertEquals(name, jsonObject.getString(field, ""));
			assertEquals("", jsonObject.getString("noteUserName", ""), () -> "Received unexpected JSON: " + json);
			assertEquals("I am the patched subject", jsonObject.getString("subject", ""));
		}
		
		// Fetch a qualifying name to make sure that's distinct
		{
			WebTarget target2 = client.target(getRestUrl(null, TestDatabase.MAIN) + "/" + part + "/" + name + "/" + URLEncoder.encode(username2, "UTF-8"));
			Response response = target2.request().get();
			checkResponse(200, response);
			
			String json = response.readEntity(String.class);
			JsonObject jsonObject = Json.createReader(new StringReader(json)).readObject();
			assertEquals(name, jsonObject.getString(field, ""), () -> "Received unexpected JSON: " + json);
			assertEquals(username2, jsonObject.getString("noteUserName", ""), () -> "Received unexpected JSON: " + json);
			assertEquals("", jsonObject.getString("subject", ""), () -> "Received unexpected JSON: " + json);
		}
	}

	@ParameterizedTest
	@CsvSource({
		PART_NAMED + "," + NAME_NAMED + "," + USERNAME_NAMED + "," + USERNAME2_NAMED + "," + FIELD_NAMED,
		PART_PROFILE + "," + NAME_PROFILE + "," + USERNAME_PROFILE + "," + USERNAME2_PROFILE + "," + FIELD_PROFILE
	})
	public void testQualifiedDoc(String part, String name, String username, String username2, String field) throws UnsupportedEncodingException {
		Client client = getAnonymousClient();

		WebTarget target = client.target(getRestUrl(null, TestDatabase.MAIN) + "/" + part + "/" + name + "/" + URLEncoder.encode(username, "UTF-8"));
		
		// Make sure it exists in a blank form at first
		{
			Response response = target.request().get();
			checkResponse(200, response);
			
			String json = response.readEntity(String.class);
			JsonObject jsonObject = Json.createReader(new StringReader(json)).readObject();
			assertEquals(name, jsonObject.getString(field, ""), () -> "Received unexpected JSON: " + json);
			assertEquals(username, jsonObject.getString("noteUserName", ""), () -> "Received unexpected JSON: " + json);
			assertEquals("", jsonObject.getString("subject", ""), () -> "Received unexpected JSON: " + json);
		}
		
		// Update with new data
		{
			JsonObject entity = Json.createObjectBuilder()
				.add("subject", "Hey I'm qualified subject")
				.build();
			
			Response response = target.request().put(Entity.json(entity));
			checkResponse(200, response);
			
			String json = response.readEntity(String.class);
			JsonObject jsonObject = Json.createReader(new StringReader(json)).readObject();
			assertEquals(name, jsonObject.getString(field, ""));
			assertEquals(username, jsonObject.getString("noteUserName", ""), () -> "Received unexpected JSON: " + json);
			assertEquals("Hey I'm qualified subject", jsonObject.getString("subject", ""));
		}
		
		// Fetch it again to make sure it saved
		{
			Response response = target.request().get();
			checkResponse(200, response);
			
			String json = response.readEntity(String.class);
			JsonObject jsonObject = Json.createReader(new StringReader(json)).readObject();
			assertEquals(name, jsonObject.getString(field, ""));
			assertEquals(username, jsonObject.getString("noteUserName", ""), () -> "Received unexpected JSON: " + json);
			assertEquals("Hey I'm qualified subject", jsonObject.getString("subject", ""));
		}
		
		// Update it, as it should go through a different code path
		{
			JsonObject patch = Json.createObjectBuilder()
					.add("subject", "I am the patched qualified subject")
					.build();
			
			Response response = target.request().method("PATCH", Entity.json(patch));
			checkResponse(200, response);
			
			String json = response.readEntity(String.class);
			JsonObject jsonObject = Json.createReader(new StringReader(json)).readObject();
			assertEquals(name, jsonObject.getString(field, ""));
			assertEquals(username, jsonObject.getString("noteUserName", ""), () -> "Received unexpected JSON: " + json);
			assertEquals("I am the patched qualified subject", jsonObject.getString("subject", ""));
		}
		
		// Fetch it again to make sure it the patch worked
		{
			Response response = target.request().get();
			checkResponse(200, response);
			
			String json = response.readEntity(String.class);
			JsonObject jsonObject = Json.createReader(new StringReader(json)).readObject();
			assertEquals(name, jsonObject.getString(field, ""));
			assertEquals(username, jsonObject.getString("noteUserName", ""), () -> "Received unexpected JSON: " + json);
			assertEquals("I am the patched qualified subject", jsonObject.getString("subject", ""));
		}
		
		// Fetch a different qualifying name to make sure that's distinct
		{
			WebTarget target2 = client.target(getRestUrl(null, TestDatabase.MAIN) + "/" + part + "/" + name + "/" + URLEncoder.encode(username2, "UTF-8"));
			Response response = target2.request().get();
			checkResponse(200, response);
			
			String json = response.readEntity(String.class);
			JsonObject jsonObject = Json.createReader(new StringReader(json)).readObject();
			assertEquals(name, jsonObject.getString(field, ""));
			assertEquals(username2, jsonObject.getString("noteUserName", ""));
			assertEquals("", jsonObject.getString("subject", ""));
		}
		
		// Fetch a the base name to make sure that's distinct
		{
			WebTarget target2 = client.target(getRestUrl(null, TestDatabase.MAIN) + "/" + part + "/" + name);
			Response response = target2.request().get();
			checkResponse(200, response);
			
			String json = response.readEntity(String.class);
			JsonObject jsonObject = Json.createReader(new StringReader(json)).readObject();
			assertEquals(name, jsonObject.getString(field, ""));
			assertEquals("", jsonObject.getString("noteUserName", ""));
			assertNotEquals("I am the patched qualified subject", jsonObject.getString("subject", ""));
		}
	}
}
