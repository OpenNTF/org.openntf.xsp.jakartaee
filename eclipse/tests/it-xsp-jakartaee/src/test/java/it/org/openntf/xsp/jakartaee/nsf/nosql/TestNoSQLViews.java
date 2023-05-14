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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import it.org.openntf.xsp.jakartaee.AbstractWebClientTest;
import it.org.openntf.xsp.jakartaee.TestDatabase;
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
		WebTarget queryTarget = client.target(getRestUrl(null, TestDatabase.MAIN) + "/nosql/byViewKey/" + URLEncoder.encode(lastName, "UTF-8"));
		
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
		WebTarget queryTarget = client.target(getRestUrl(null, TestDatabase.MAIN) + "/nosql/byViewTwoKeys"
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
		WebTarget queryTarget = client.target(getRestUrl(null, TestDatabase.MAIN) + "/nosql/byViewKeyMulti/" + URLEncoder.encode(lastName, "UTF-8"));
		
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
	 * <p>This also tests both the /findCategorized and /findCategorizedManual
	 * endpoints, which will exercise both the {@code @ViewDocuments} annotation
	 * and the {@code readViewDocuments} method on {@code Repository}.</p>
	 * 
	 * @param endpoint the endpoint tested in this run
	 * @see <a href="https://github.com/OpenNTF/org.openntf.xsp.jakartaee/issues/391">Issue #391</a>
	 */
	@ParameterizedTest
	@ValueSource(strings = { "findCategorized", "findCategorizedManual" })
	public void testQueryDocumentsCategorized(String endpoint) throws UnsupportedEncodingException {
		Client client = getAdminClient();
		
		JsonObject person = createTwoPersonDocuments(true);
		
		// Find by the last name of the second person
		String lastName = person.getString("lastName");
		assertNotNull(lastName);
		String firstName = person.getString("firstName");
		assertNotNull(firstName);
		WebTarget queryTarget = client.target(
			getRestUrl(null, TestDatabase.MAIN) + "/nosql/" + endpoint
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
	
	/**
	 * Tests for Issue #404, which adds the ability to only return distinct
	 * documents when using {@code @ViewDocuments}.
	 * 
	 * @see <a href="https://github.com/OpenNTF/org.openntf.xsp.jakartaee/issues/404">Issue #404</a>
	 */
	@Test
	public void testQueryDocumentsCategorizedDistinct() throws UnsupportedEncodingException {
		Client client = getAdminClient();
		
		JsonObject person = createTwoPersonDocuments(true);
		
		// Find by the last name of the second person
		String lastName = person.getString("lastName");
		assertNotNull(lastName);
		String firstName = person.getString("firstName");
		assertNotNull(firstName);
		WebTarget queryTarget = client.target(
			getRestUrl(null, TestDatabase.MAIN) + "/nosql/findCategorizedDistinct"
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
			WebTarget queryTarget = client.target(getRestUrl(null, TestDatabase.MAIN) + "/nosql/ftSearch?search=" + URLEncoder.encode(query, "UTF-8"));
			
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
			WebTarget queryTarget = client.target(getRestUrl(null, TestDatabase.MAIN) + "/nosql/ftSearchPaginated?page=1&size=1&search=" + URLEncoder.encode(query, "UTF-8"));
			
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
			WebTarget queryTarget = client.target(getRestUrl(null, TestDatabase.MAIN) + "/nosql/ftSearchPaginated?page=2&size=1&search=" + URLEncoder.encode(query, "UTF-8"));
			
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
			getRestUrl(null, TestDatabase.MAIN) + "/nosql/ftSearch?"
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
		WebTarget queryTarget = client.target(getRestUrl(null, TestDatabase.MAIN) + "/nosql/ftSearchSorted?search=" + URLEncoder.encode(query, "UTF-8"));
		
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
	
	@Test
	public void testListViews() {
		Client client = getAdminClient();
		WebTarget queryTarget = client.target(getRestUrl(null, TestDatabase.MAIN) + "/nosql/listViews");
		Response response = queryTarget.request()
			.accept(MediaType.APPLICATION_JSON_TYPE)
			.get();
		String json = response.readEntity(String.class);
		assertEquals(200, response.getStatus(), () -> "Received unexpected result: " + json);

		JsonArray result = Json.createReader(new StringReader(json)).readArray();
		assertFalse(result.isEmpty());
		{
			JsonObject view = result.stream()
				.map(JsonValue::asJsonObject)
				.filter(obj -> "Persons".equals(obj.getString("title")))
				.findFirst()
				.orElse(null);
			assertNotNull(view, "Coult not find Persons view");
			assertEquals("VIEW", view.getString("type"));
			assertTrue(view.getJsonArray("aliases").isEmpty());
			assertEquals("SELECT Form=\"Person\"", view.getString("selectionFormula"));
			
			JsonArray columns = view.getJsonArray("columnInfo");
			assertEquals(8, columns.size());
			{
				JsonObject lastName = columns.getJsonObject(0);
				assertEquals("LastName", lastName.getString("title"));
				assertEquals("LastName", lastName.getString("programmaticName"));
				assertEquals("ASCENDING", lastName.getString("sortOrder"));
				assertTrue(lastName.getJsonArray("resortOrders").isEmpty());
			}
			{
				JsonObject firstName = columns.getJsonObject(1);
				assertEquals("FirstName", firstName.getString("title"));
				assertEquals("FirstName", firstName.getString("programmaticName"));
				assertEquals("ASCENDING", firstName.getString("sortOrder"));
				JsonArray resortOrders = firstName.getJsonArray("resortOrders");
				assertEquals(2, resortOrders.size());
				assertEquals("ASCENDING", resortOrders.getString(0));
				assertEquals("DESCENDING", resortOrders.getString(1));
			}
			{
				JsonObject favoriteTime = columns.getJsonObject(3);
				assertEquals("Favorite Time", favoriteTime.getString("title"));
			}
		}
		{
			JsonObject folder = result.stream()
				.map(JsonValue::asJsonObject)
				.filter(obj -> "Persons Folder".equals(obj.getString("title")))
				.findFirst()
				.orElse(null);
			assertNotNull(folder, "Coult not find Persons Folder");
			assertEquals("FOLDER", folder.getString("type"));
			JsonArray aliases = folder.getJsonArray("aliases");
			assertEquals(1, aliases.size());
			assertEquals("PersonsFolder", aliases.getString(0));
		}
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
		WebTarget postTarget = client.target(getRestUrl(null, TestDatabase.MAIN) + "/nosql/create"); //$NON-NLS-1$

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
