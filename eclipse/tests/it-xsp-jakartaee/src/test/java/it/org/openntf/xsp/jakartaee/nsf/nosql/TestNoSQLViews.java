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
package it.org.openntf.xsp.jakartaee.nsf.nosql;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataOutput;
import org.junit.jupiter.api.Test;

import it.org.openntf.xsp.jakartaee.AbstractWebClientTest;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@SuppressWarnings("nls")
public class TestNoSQLViews extends AbstractWebClientTest {
	@Test
	public void testQueryByKey() throws UnsupportedEncodingException {
		Client client = getAdminClient();
		
		JsonObject person = createTwoPersonDocuments(false);
		
		// Find by the last name of the second person
		String lastName = person.getString("lastName");
		assertNotNull(lastName);
		WebTarget queryTarget = client.target(getRestUrl(null) + "/nosql/byViewKey/" + URLEncoder.encode(lastName, "UTF-8"));
		
		Response response = queryTarget.request()
			.accept(MediaType.APPLICATION_JSON_TYPE)
			.get();
		String json = response.readEntity(String.class);
		assertEquals(200, response.getStatus(), () -> "Received unexpected result: " + json);

		JsonObject result = Json.createReader(new StringReader(json)).readObject();
		assertEquals(person.getString("unid"), result.getString("unid"));
		assertEquals(person.getString("lastName"), result.getString("lastName"));
	}
	
	@Test
	public void testQueryByTwoKeys() throws UnsupportedEncodingException {
		Client client = getAdminClient();
		
		JsonObject person = createTwoPersonDocuments(true);
		
		// Find by the last name of the second person
		String lastName = person.getString("lastName");
		assertNotNull(lastName);
		String firstName = person.getString("firstName");
		assertNotNull(firstName);
		WebTarget queryTarget = client.target(getRestUrl(null) + "/nosql/byViewTwoKeys"
			+ "/" + URLEncoder.encode(lastName, "UTF-8")
			+ "/"
			+ URLEncoder.encode(firstName, "UTF-8")
		);
		
		Response response = queryTarget.request()
			.accept(MediaType.APPLICATION_JSON_TYPE)
			.get();
		String json = response.readEntity(String.class);
		assertEquals(200, response.getStatus(), () -> "Received unexpected result: " + json);

		JsonObject result = Json.createReader(new StringReader(json)).readObject();
		assertEquals(person.getString("unid"), result.getString("unid"));
		assertEquals(person.getString("lastName"), result.getString("lastName"));
	}
	
	@Test
	public void testQueryByKeyMulti() throws UnsupportedEncodingException {
		Client client = getAdminClient();
		
		// Create four documents with two distinct last names
		createTwoPersonDocuments(true);
		JsonObject person = createTwoPersonDocuments(true);
		
		// Find by the last name of the second person
		String lastName = person.getString("lastName");
		assertNotNull(lastName);
		WebTarget queryTarget = client.target(getRestUrl(null) + "/nosql/byViewKeyMulti/" + URLEncoder.encode(lastName, "UTF-8"));
		
		Response response = queryTarget.request()
			.accept(MediaType.APPLICATION_JSON_TYPE)
			.get();
		String json = response.readEntity(String.class);
		assertEquals(200, response.getStatus(), () -> "Received unexpected result: " + json);

		JsonArray result = Json.createReader(new StringReader(json)).readArray();
		assertEquals(2, result.size(), () -> "Received unexpected result: " + json);
		JsonObject resultPerson = result.getJsonObject(1);
		assertEquals(person.getString("unid"), resultPerson.getString("unid"));
		assertEquals(person.getString("lastName"), resultPerson.getString("lastName"));
	}
	
	/**
	 * Tests for Issue #391, where querying a categorized view by key
	 * threw an exception.
	 * 
	 * @see <a href="https://github.com/OpenNTF/org.openntf.xsp.jakartaee/issues/391">Issue #391</a>
	 */
	@Test
	public void testQueryDocumentsCategorized() throws UnsupportedEncodingException {
		Client client = getAdminClient();
		
		JsonObject person = createTwoPersonDocuments(true);
		
		// Find by the last name of the second person
		String lastName = person.getString("lastName");
		assertNotNull(lastName);
		String firstName = person.getString("firstName");
		assertNotNull(firstName);
		WebTarget queryTarget = client.target(
			getRestUrl(null) + "/nosql/findCategorized"
			+ "/" + URLEncoder.encode(lastName, "UTF-8")
		);
		
		Response response = queryTarget.request()
			.accept(MediaType.APPLICATION_JSON_TYPE)
			.get();
		String json = response.readEntity(String.class);
		assertEquals(200, response.getStatus(), () -> "Received unexpected result: " + json);

		JsonArray array = Json.createReader(new StringReader(json)).readArray();
		assertEquals(2, array.size());
		JsonObject result = array.getJsonObject(1);
		assertEquals(person.getString("unid"), result.getString("unid"));
		assertEquals(person.getString("lastName"), result.getString("lastName"));
	}
	
	@Test
	public void testFtSearch() throws UnsupportedEncodingException {
		Client client = getAdminClient();
		
		String prefix = "aa" + System.nanoTime();
		
		JsonObject person1 = createPersonDoc("Foo", prefix + "bar");
		JsonObject person2 = createPersonDoc("Foo", prefix + "baz");
		
		String query = "[LastName]=" + prefix + "*";
		{
			WebTarget queryTarget = client.target(getRestUrl(null) + "/nosql/ftSearch?search=" + URLEncoder.encode(query, "UTF-8"));
			
			Response response = queryTarget.request()
				.accept(MediaType.APPLICATION_JSON_TYPE)
				.get();
			String json = response.readEntity(String.class);
			assertEquals(200, response.getStatus(), () -> "Received unexpected result: " + json);
	
			JsonArray result = Json.createReader(new StringReader(json)).readArray();
			assertEquals(2, result.size());
			assertTrue(result.stream().map(JsonValue::asJsonObject).anyMatch(p -> person1.getString("unid").equals(p.getString("unid"))));
			assertTrue(result.stream().map(JsonValue::asJsonObject).anyMatch(p -> person2.getString("unid").equals(p.getString("unid"))));
		}
		
		// Test basic pagination
		String firstUnid;
		{
			WebTarget queryTarget = client.target(getRestUrl(null) + "/nosql/ftSearchPaginated?page=1&size=1&search=" + URLEncoder.encode(query, "UTF-8"));
			
			Response response = queryTarget.request()
				.accept(MediaType.APPLICATION_JSON_TYPE)
				.get();
			String json = response.readEntity(String.class);
			assertEquals(200, response.getStatus(), () -> "Received unexpected result: " + json);
	
			JsonArray result = Json.createReader(new StringReader(json)).readArray();
			assertEquals(1, result.size());
			assertTrue(
				result.stream().map(JsonValue::asJsonObject).anyMatch(p -> person1.getString("unid").equals(p.getString("unid")))
				|| result.stream().map(JsonValue::asJsonObject).anyMatch(p -> person2.getString("unid").equals(p.getString("unid")))
			);
			firstUnid = result.getJsonObject(0).getString("unid");
		}
		{
			WebTarget queryTarget = client.target(getRestUrl(null) + "/nosql/ftSearchPaginated?page=2&size=1&search=" + URLEncoder.encode(query, "UTF-8"));
			
			Response response = queryTarget.request()
				.accept(MediaType.APPLICATION_JSON_TYPE)
				.get();
			String json = response.readEntity(String.class);
			assertEquals(200, response.getStatus(), () -> "Received unexpected result: " + json);
	
			JsonArray result = Json.createReader(new StringReader(json)).readArray();
			assertEquals(1, result.size());
			assertTrue(
				result.stream().map(JsonValue::asJsonObject).anyMatch(p -> person1.getString("unid").equals(p.getString("unid")))
				|| result.stream().map(JsonValue::asJsonObject).anyMatch(p -> person2.getString("unid").equals(p.getString("unid")))
			);
			assertNotEquals(firstUnid, result.getJsonObject(0).getString("unid"));
		}
	}	

	@Test
	public void testFtSearch2() throws UnsupportedEncodingException {
		Client client = getAdminClient();
		
		String prefix = "aa" + System.nanoTime();
		
		@SuppressWarnings("unused")
		JsonObject person1 = createPersonDoc("Foo", prefix + "bar");
		JsonObject person2 = createPersonDoc("Fooness", prefix + "baz");
		
		String query = "[LastName]=" + prefix + "*";
		String query2 = "[FirstName]=Fooness";
		WebTarget queryTarget = client.target(
			getRestUrl(null) + "/nosql/ftSearch?"
			+ "search=" + URLEncoder.encode(query, "UTF-8")
			+ "&search2=" + URLEncoder.encode(query2, "UTF-8")
		);
		
		Response response = queryTarget.request()
			.accept(MediaType.APPLICATION_JSON_TYPE)
			.get();
		String json = response.readEntity(String.class);
		assertEquals(200, response.getStatus(), () -> "Received unexpected result: " + json);

		JsonArray result = Json.createReader(new StringReader(json)).readArray();
		assertEquals(1, result.size());
		assertEquals(person2.getString("unid"), result.getJsonObject(0).getString("unid"));
	}
	
	@Test
	public void testFtSearchSorted() throws UnsupportedEncodingException {
		Client client = getAdminClient();
		
		String prefix = "aa" + System.nanoTime();
		
		JsonObject person1 = createPersonDoc("Foo", prefix + "bar");
		JsonObject person2 = createPersonDoc("Zarg", prefix + "baz");
		
		String query = "[LastName]=" + prefix + "*";
		WebTarget queryTarget = client.target(getRestUrl(null) + "/nosql/ftSearchSorted?search=" + URLEncoder.encode(query, "UTF-8"));
		
		Response response = queryTarget.request()
			.accept(MediaType.APPLICATION_JSON_TYPE)
			.get();
		String json = response.readEntity(String.class);
		assertEquals(200, response.getStatus(), () -> "Received unexpected result: " + json);

		JsonArray result = Json.createReader(new StringReader(json)).readArray();
		assertEquals(2, result.size());
		assertEquals(person2.getString("unid"), result.getJsonObject(0).getString("unid"));
		assertEquals(person1.getString("unid"), result.getJsonObject(1).getString("unid"));
	}
	
	/**
	 * Creates two person documents, optionally using the same auto-generated last name
	 * for both documents.
	 * 
	 * @param retainLastName whether both docs should be created with the same last name
	 * @return the second document created
	 * @throws JsonException if there is a problem parsing the result
	 */
	private JsonObject createTwoPersonDocuments(boolean retainLastName) {
		// Create two documents to ensure that we can query by the second
		String lastName = null;
		JsonObject person = null;
		for(int i = 0; i < 2; i++) {
			if(lastName == null || !retainLastName) {
				lastName = "Fooson" + System.nanoTime();
			}
			
			person = createPersonDoc("Foo" + System.nanoTime(), lastName);
		}
		
		return person;
	}
	
	private JsonObject createPersonDoc(String firstName, String lastName) {
		Client client = getAdminClient();
		WebTarget postTarget = client.target(getRestUrl(null) + "/nosql/create"); //$NON-NLS-1$

		MultipartFormDataOutput payload = new MultipartFormDataOutput();
		payload.addFormData("firstName", firstName, MediaType.TEXT_PLAIN_TYPE);
		payload.addFormData("lastName", lastName, MediaType.TEXT_PLAIN_TYPE);
		
		Response response = postTarget.request()
			.accept(MediaType.APPLICATION_JSON_TYPE)
			.post(Entity.entity(payload, MediaType.MULTIPART_FORM_DATA_TYPE));
		String json = response.readEntity(String.class);
		assertEquals(200, response.getStatus(), () -> "Received unexpected result: " + json);

		JsonObject person = Json.createReader(new StringReader(json)).readObject();
		String unid = person.getString("unid");
		assertNotNull(unid);
		assertFalse(unid.isEmpty());
		return person;
	}
}
