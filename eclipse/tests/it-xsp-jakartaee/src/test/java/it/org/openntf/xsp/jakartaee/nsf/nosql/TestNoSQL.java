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
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;

import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataOutput;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import it.org.openntf.xsp.jakartaee.AbstractWebClientTest;

@SuppressWarnings("nls")
public class TestNoSQL extends AbstractWebClientTest {
	@Test
	public void testNoSql() {
		Client client = getAnonymousClient();
		WebTarget target = client.target(getRestUrl(null) + "/nosql?lastName=CreatedUnitTest"); //$NON-NLS-1$
		
		{
			Response response = target.request().get();
			
			String json = response.readEntity(String.class);
			JsonObject jsonObject = Json.createReader(new StringReader(json)).readObject();
			
			JsonArray byQueryLastName = jsonObject.getJsonArray("byQueryLastName"); //$NON-NLS-1$
			assertTrue(byQueryLastName.isEmpty(), () -> String.valueOf(jsonObject));
		}
		
		// Now use the MVC endpoint to create one, which admittedly is outside this test
		{
			MultivaluedMap<String, String> payload = new MultivaluedHashMap<>();
			payload.putSingle("firstName", "foo"); //$NON-NLS-1$ //$NON-NLS-2$
			payload.putSingle("lastName", "CreatedUnitTest"); //$NON-NLS-1$ //$NON-NLS-2$
			payload.putSingle("customProperty", "i am custom property"); //$NON-NLS-1$ //$NON-NLS-2$
			WebTarget postTarget = client.target(getRestUrl(null) + "/nosql/create"); //$NON-NLS-1$
			Response response = postTarget.request()
				.accept(MediaType.TEXT_HTML_TYPE) // Ensure that it routes to MVC
				.post(Entity.form(payload));
			assertEquals(303, response.getStatus());
		}
		
		// There should be at least one now
		{
			Response response = target.request().get();
			
			String json = response.readEntity(String.class);
			JsonObject jsonObject = Json.createReader(new StringReader(json)).readObject();
			
			JsonArray byQueryLastName = jsonObject.getJsonArray("byQueryLastName"); //$NON-NLS-1$
			assertFalse(byQueryLastName.isEmpty());
			JsonObject entry = byQueryLastName.getJsonObject(0);
			assertEquals("CreatedUnitTest", entry.getString("lastName")); //$NON-NLS-1$ //$NON-NLS-2$
			{
				JsonObject customProp = entry.getJsonObject("customProperty"); //$NON-NLS-1$
				String val = customProp.getString("value"); //$NON-NLS-1$
				assertEquals("i am custom property", val); //$NON-NLS-1$
			}
			assertFalse(entry.getString("unid").isEmpty()); //$NON-NLS-1$
			
			int size = entry.getInt("size"); //$NON-NLS-1$
			assertTrue(size > 0);
		}
	}
	
	/**
	 * Tests to make sure a missing firstName is caught, which is enforced at the JAX-RS level.
	 */
	@Test
	public void testMissingFirstName() {
		Client client = getAnonymousClient();
		
		MultivaluedMap<String, String> payload = new MultivaluedHashMap<>();
		payload.putSingle("lastName", "CreatedUnitTest"); //$NON-NLS-1$ //$NON-NLS-2$
		payload.putSingle("customProperty", "i am custom property"); //$NON-NLS-1$ //$NON-NLS-2$
		WebTarget postTarget = client.target(getRestUrl(null) + "/nosql/create"); //$NON-NLS-1$
		Response response = postTarget.request().post(Entity.form(payload));
		assertEquals(400, response.getStatus());
	}
	
	/**
	 * Tests to make sure a missing lastName is caught, which is enforced at the JNoSQL level.
	 */
	@Test
	public void testMissingLastName() {
		Client client = getAnonymousClient();
		
		MultivaluedMap<String, String> payload = new MultivaluedHashMap<>();
		payload.putSingle("firstName", "CreatedUnitTest"); //$NON-NLS-1$ //$NON-NLS-2$
		payload.putSingle("customProperty", "i am custom property"); //$NON-NLS-1$ //$NON-NLS-2$
		WebTarget postTarget = client.target(getRestUrl(null) + "/nosql/create"); //$NON-NLS-1$
		Response response = postTarget.request().post(Entity.form(payload));
		// NB: this currently throws a 500 due to the exception being UndeclaredThrowableException (Issue #211)
		assertTrue(response.getStatus() >= 400, () -> "Response code should be an error; got " + response.getStatus()); //$NON-NLS-1$
	}
	
	@Test
	@Disabled("QRP#executeToView is currently broken on Linux (12.0.1IF2)")
	public void testNoSqlNames() {
		Client client = getAnonymousClient();
		WebTarget target = client.target(getRestUrl(null) + "/nosql/servers"); //$NON-NLS-1$
		
		Response response = target.request().get();
		
		String json = response.readEntity(String.class);
		JsonObject jsonObject = Json.createReader(new StringReader(json)).readObject();
		
		JsonArray all = jsonObject.getJsonArray("all"); //$NON-NLS-1$
		assertNotNull(all, () -> json);
		assertFalse(all.isEmpty(), () -> json);
		JsonObject entry = all.getJsonObject(0);
		assertEquals("CN=JakartaEE/O=OpenNTFTest", entry.getString("serverName"), () -> json); //$NON-NLS-1$ //$NON-NLS-2$
		assertFalse(entry.getString("unid").isEmpty(), () -> json); //$NON-NLS-1$
		assertEquals(1d, jsonObject.getJsonNumber("totalCount").doubleValue(), () -> json); //$NON-NLS-1$
	}
	
	@Test
	public void testQueryNoteID() throws UnsupportedEncodingException {
		Client client = getAdminClient();
		
		String lastName;
		String unid;
		{
			WebTarget postTarget = client.target(getRestUrl(null) + "/nosql/create"); //$NON-NLS-1$
			
			lastName = "Fooson" + System.nanoTime();
			MultipartFormDataOutput payload = new MultipartFormDataOutput();
			payload.addFormData("firstName", "Foo" + System.nanoTime(), MediaType.TEXT_PLAIN_TYPE);
			payload.addFormData("lastName", lastName, MediaType.TEXT_PLAIN_TYPE);
			
			Response response = postTarget.request()
				.accept(MediaType.APPLICATION_JSON_TYPE)
				.post(Entity.entity(payload, MediaType.MULTIPART_FORM_DATA_TYPE));
			String json = response.readEntity(String.class);
			assertEquals(200, response.getStatus(), () -> "Received unexpected result: " + json);

			JsonObject person = Json.createReader(new StringReader(json)).readObject();
			unid = person.getString("unid");
			assertNotNull(unid);
			assertFalse(unid.isEmpty());
		}
		
		int noteId;
		// Fetch it again to get the note ID
		{
			WebTarget getTarget = client.target(getRestUrl(null) + "/nosql/" + unid);
			
			Response response = getTarget.request()
				.accept(MediaType.APPLICATION_JSON_TYPE)
				.get();
			assertEquals(200, response.getStatus());

			String json = response.readEntity(String.class);
			JsonObject jsonObject = Json.createReader(new StringReader(json)).readObject();
			String getUnid = jsonObject.getString("unid");
			assertEquals(unid, getUnid);
			noteId = jsonObject.getInt("noteId");
			assertNotEquals(0, noteId);
		}
		
		// Find by note ID
		WebTarget queryTarget = client.target(getRestUrl(null) + "/nosql/byNoteId/" + Integer.toHexString(noteId));
		
		Response response = queryTarget.request()
			.accept(MediaType.APPLICATION_JSON_TYPE)
			.get();
		String json = response.readEntity(String.class);
		assertEquals(200, response.getStatus(), () -> "Received unexpected result: " + json);

		JsonObject result = Json.createReader(new StringReader(json)).readObject();
		assertEquals(noteId, result.getInt("noteId"));
		assertEquals(lastName, result.getString("lastName"));
	}
	
	@Test
	public void testQueryNoteIDInt() throws UnsupportedEncodingException {
		Client client = getAdminClient();
		
		String lastName;
		String unid;
		{
			WebTarget postTarget = client.target(getRestUrl(null) + "/nosql/create"); //$NON-NLS-1$
			
			lastName = "Fooson" + System.nanoTime();
			MultipartFormDataOutput payload = new MultipartFormDataOutput();
			payload.addFormData("firstName", "Foo" + System.nanoTime(), MediaType.TEXT_PLAIN_TYPE);
			payload.addFormData("lastName", lastName, MediaType.TEXT_PLAIN_TYPE);
			
			Response response = postTarget.request()
				.accept(MediaType.APPLICATION_JSON_TYPE)
				.post(Entity.entity(payload, MediaType.MULTIPART_FORM_DATA_TYPE));
			String json = response.readEntity(String.class);
			assertEquals(200, response.getStatus(), () -> "Received unexpected result: " + json);

			JsonObject person = Json.createReader(new StringReader(json)).readObject();
			unid = person.getString("unid");
			assertNotNull(unid);
			assertFalse(unid.isEmpty());
		}
		
		int noteId;
		// Fetch it again to get the note ID
		{
			WebTarget getTarget = client.target(getRestUrl(null) + "/nosql/" + unid);
			
			Response response = getTarget.request()
				.accept(MediaType.APPLICATION_JSON_TYPE)
				.get();
			assertEquals(200, response.getStatus());

			String json = response.readEntity(String.class);
			JsonObject jsonObject = Json.createReader(new StringReader(json)).readObject();
			String getUnid = jsonObject.getString("unid");
			assertEquals(unid, getUnid);
			noteId = jsonObject.getInt("noteId");
			assertNotEquals(0, noteId);
		}
		
		// Find by note ID
		WebTarget queryTarget = client.target(getRestUrl(null) + "/nosql/byNoteIdInt/" + noteId);
		
		Response response = queryTarget.request()
			.accept(MediaType.APPLICATION_JSON_TYPE)
			.get();
		String json = response.readEntity(String.class);
		assertEquals(200, response.getStatus(), () -> "Received unexpected result: " + json);

		JsonObject result = Json.createReader(new StringReader(json)).readObject();
		assertEquals(noteId, result.getInt("noteId"));
		assertEquals(lastName, result.getString("lastName"));
	}
	
	@Test
	public void testQueryModTime() throws UnsupportedEncodingException, InterruptedException {
		Client client = getAdminClient();
		
		String lastName;
		String firstName;
		String unid;
		{
			WebTarget postTarget = client.target(getRestUrl(null) + "/nosql/create"); //$NON-NLS-1$
			
			lastName = "Fooson" + System.nanoTime();
			firstName = "Foo" + System.nanoTime();
			MultipartFormDataOutput payload = new MultipartFormDataOutput();
			payload.addFormData("firstName", firstName, MediaType.TEXT_PLAIN_TYPE);
			payload.addFormData("lastName", lastName, MediaType.TEXT_PLAIN_TYPE);
			
			Response response = postTarget.request()
				.accept(MediaType.APPLICATION_JSON_TYPE)
				.post(Entity.entity(payload, MediaType.MULTIPART_FORM_DATA_TYPE));
			String json = response.readEntity(String.class);
			assertEquals(200, response.getStatus(), () -> "Received unexpected result: " + json);

			JsonObject person = Json.createReader(new StringReader(json)).readObject();
			unid = person.getString("unid");
			assertNotNull(unid);
			assertFalse(unid.isEmpty());
		}
		
		// Update it to set the mod time
		{
			TimeUnit.SECONDS.sleep(1);
			
			WebTarget postTarget = client.target(getRestUrl(null) + "/nosql/" + unid);
			
			JsonObject payload = Json.createObjectBuilder()
					.add("firstName", firstName)
					.add("lastName", lastName + "_mod")
					.build();
			Response response = postTarget.request()
				.accept(MediaType.APPLICATION_JSON_TYPE)
				.put(Entity.json(payload.toString()));
			String json = response.readEntity(String.class);
			assertEquals(200, response.getStatus(), () -> "Received unexpected result: " + json);

			JsonObject person = Json.createReader(new StringReader(json)).readObject();
			String patchUnid = person.getString("unid");
			assertEquals(unid, patchUnid);
		}
		
		String modified;
		// Fetch it again to get the mod time
		{
			WebTarget getTarget = client.target(getRestUrl(null) + "/nosql/" + unid);
			
			Response response = getTarget.request()
				.accept(MediaType.APPLICATION_JSON_TYPE)
				.get();
			assertEquals(200, response.getStatus());

			String json = response.readEntity(String.class);
			JsonObject jsonObject = Json.createReader(new StringReader(json)).readObject();
			String getUnid = jsonObject.getString("unid");
			assertEquals(unid, getUnid);
			modified = jsonObject.getString("modified");
			assertNotNull(modified);
			assertFalse(modified.isEmpty());
			
			// Modified in this file should be the same, since it's the same NSF
			String modifiedInThisFile = jsonObject.getString("modifiedInFile");
			assertNotNull(modifiedInThisFile);
			assertEquals(modified, modifiedInThisFile);
			
			String created = jsonObject.getString("created");
			assertNotNull(created);
			assertFalse(created.isEmpty());
			assertNotEquals(modified, created);
			
			// Added should match created
			String added = jsonObject.getString("addedToFile");
			assertNotNull(added);
			assertFalse(added.isEmpty());
			assertEquals(created, added);
		}
		
		// Find by modified
		WebTarget queryTarget = client.target(getRestUrl(null) + "/nosql/modifiedSince/" + URLEncoder.encode(modified, "UTF-8"));
		
		Response response = queryTarget.request()
			.accept(MediaType.APPLICATION_JSON_TYPE)
			.get();
		String json = response.readEntity(String.class);
		assertEquals(200, response.getStatus(), () -> "Received unexpected result: " + json);

		JsonArray result = Json.createReader(new StringReader(json)).readArray();
		JsonObject person = result.stream()
			.map(JsonValue::asJsonObject)
			.filter(p -> modified.equals(p.getString("modified")) && unid.equals(p.getString("unid")))
			.findFirst()
			.get();
		// Test to make sure the modification actually worked, too
		assertEquals(lastName + "_mod", person.getString("lastName"));
	}
	
	@Test
	public void testQueryByNoteIdNotFound() {
		Client client = getAdminClient();
		WebTarget getTarget = client.target(getRestUrl(null) + "/nosql/byNoteId/doesNotExist");
		
		Response response = getTarget.request()
			.accept(MediaType.APPLICATION_JSON_TYPE)
			.get();
		assertEquals(404, response.getStatus());
	}
	
	@Test
	public void testMultipartCreate() {
		Client client = getAnonymousClient();
		String unid;
		String lastName = "Fooson" + System.nanoTime();
		{
			WebTarget postTarget = client.target(getRestUrl(null) + "/nosql/create"); //$NON-NLS-1$
			
			MultipartFormDataOutput payload = new MultipartFormDataOutput();
			payload.addFormData("firstName", "Foo", MediaType.TEXT_PLAIN_TYPE);
			payload.addFormData("lastName", lastName, MediaType.TEXT_PLAIN_TYPE);
			
			Response response = postTarget.request()
				.accept(MediaType.APPLICATION_JSON_TYPE)
				.post(Entity.entity(payload, MediaType.MULTIPART_FORM_DATA_TYPE));
			assertEquals(200, response.getStatus());

			String json = response.readEntity(String.class);
			JsonObject jsonObject = Json.createReader(new StringReader(json)).readObject();
			unid = jsonObject.getString("unid");
			assertNotNull(unid);
			assertFalse(unid.isEmpty());
		}
		
		// Fetch the doc by UNID
		{
			WebTarget getTarget = client.target(getRestUrl(null) + "/nosql/" + unid);
			
			Response response = getTarget.request()
				.accept(MediaType.APPLICATION_JSON_TYPE)
				.get();
			assertEquals(200, response.getStatus());

			String json = response.readEntity(String.class);
			JsonObject jsonObject = Json.createReader(new StringReader(json)).readObject();
			String getUnid = jsonObject.getString("unid");
			assertEquals(unid, getUnid);
			assertEquals(lastName, jsonObject.getString("lastName"));
			
			int noteId = jsonObject.getInt("noteId");
			assertNotEquals(0, noteId);
			
			// Make sure it has sensible date values
			Instant.parse(jsonObject.getString("created"));
			Instant.parse(jsonObject.getString("modified"));
			Instant.parse(jsonObject.getString("accessed"));
		}
	}
	
	@Test
	public void testAttachmentCreate() {
		Client client = getAnonymousClient();
		String unid;
		String lastName = "Fooson" + System.nanoTime();
		{
			WebTarget postTarget = client.target(getRestUrl(null) + "/nosql/create"); //$NON-NLS-1$
			
			MultipartFormDataOutput payload = new MultipartFormDataOutput();
			payload.addFormData("firstName", "Foo", MediaType.TEXT_PLAIN_TYPE);
			payload.addFormData("lastName", lastName, MediaType.TEXT_PLAIN_TYPE);
			payload.addFormData("attachment", "<p>I am foo HTML</p>", MediaType.TEXT_HTML_TYPE, "foo.html");
			
			Response response = postTarget.request()
				.accept(MediaType.APPLICATION_JSON_TYPE)
				.post(Entity.entity(payload, MediaType.MULTIPART_FORM_DATA_TYPE));
			assertEquals(200, response.getStatus());

			String json = response.readEntity(String.class);
			JsonObject jsonObject = Json.createReader(new StringReader(json)).readObject();
			unid = jsonObject.getString("unid");
			assertNotNull(unid);
			assertFalse(unid.isEmpty());
		}
		
		// Fetch the doc by UNID
		{
			WebTarget getTarget = client.target(getRestUrl(null) + "/nosql/" + unid);
			
			Response response = getTarget.request()
				.accept(MediaType.APPLICATION_JSON_TYPE)
				.get();
			assertEquals(200, response.getStatus());

			String json = response.readEntity(String.class);
			JsonObject jsonObject = Json.createReader(new StringReader(json)).readObject();
			String getUnid = jsonObject.getString("unid");
			assertEquals(unid, getUnid);
			assertEquals(lastName, jsonObject.getString("lastName"));
			
			JsonArray attachments = jsonObject.getJsonArray("attachments");
			assertNotNull(attachments);
			assertFalse(attachments.isEmpty());
			assertTrue(attachments.stream().map(JsonValue::asJsonObject).anyMatch(att -> "foo.html".equals(att.getString("name"))));
		}
		
		// Fetch the attachment
		{
			WebTarget getTarget = client.target(getRestUrl(null) + "/nosql/" + unid + "/attachment/foo.html");

			Response response = getTarget.request().get();
			assertEquals(200, response.getStatus());

			String html = response.readEntity(String.class);
			assertEquals("<p>I am foo HTML</p>", html);
		}
	}
	
	@Test
	public void testFolderOperations() {
		Client client = getAdminClient();
		String unid;
		String lastName = "Fooson" + System.nanoTime();
		{
			WebTarget postTarget = client.target(getRestUrl(null) + "/nosql/create"); //$NON-NLS-1$
			
			MultipartFormDataOutput payload = new MultipartFormDataOutput();
			payload.addFormData("firstName", "Foo", MediaType.TEXT_PLAIN_TYPE);
			payload.addFormData("lastName", lastName, MediaType.TEXT_PLAIN_TYPE);
			
			Response response = postTarget.request()
				.accept(MediaType.APPLICATION_JSON_TYPE)
				.post(Entity.entity(payload, MediaType.MULTIPART_FORM_DATA_TYPE));
			assertEquals(200, response.getStatus());

			String json = response.readEntity(String.class);
			JsonObject jsonObject = Json.createReader(new StringReader(json)).readObject();
			unid = jsonObject.getString("unid");
			assertNotNull(unid);
			assertFalse(unid.isEmpty());
		}
		
		Predicate<String> isInFolder = documentId -> {
			WebTarget getTarget = client.target(getRestUrl(null) + "/nosql/inFolder"); //$NON-NLS-1$
			
			Response response = getTarget.request().get();
			String json = response.readEntity(String.class);
			assertEquals(200, response.getStatus(), () -> "Received unexpected response code " + response.getStatus() + ": " + json);

			JsonArray result = Json.createReader(new StringReader(json)).readArray();
			
			return result.stream().map(JsonValue::asJsonObject).anyMatch(person -> documentId.equals(person.getString("unid")));
		};
		
		// Make sure it's not in the folder
		assertFalse(isInFolder.test(unid));
		
		// Add it to the folder
		{
			WebTarget postTarget = client.target(getRestUrl(null) + "/nosql/" + unid + "/putInFolder");

			Response response = postTarget.request().post(Entity.form(new MultivaluedHashMap<>()));
			String json = response.readEntity(String.class);
			assertEquals(200, response.getStatus(), () -> "Received unexpected response code " + response.getStatus() + ": " + json);
		}
		
		// Make sure it's in the folder now
		assertTrue(isInFolder.test(unid));
		
		// Remove it from the folder
		{
			WebTarget postTarget = client.target(getRestUrl(null) + "/nosql/" + unid + "/removeFromFolder");

			Response response = postTarget.request().post(Entity.form(new MultivaluedHashMap<>()));
			String json = response.readEntity(String.class);
			assertEquals(200, response.getStatus(), () -> "Received unexpected response code " + response.getStatus() + ": " + json);
		}
		
		// Make sure it's not in the folder
		assertFalse(isInFolder.test(unid));
	}
}
