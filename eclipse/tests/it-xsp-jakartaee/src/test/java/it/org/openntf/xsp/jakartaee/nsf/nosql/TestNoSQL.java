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
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataOutput;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
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
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;

@SuppressWarnings("nls")
public class TestNoSQL extends AbstractWebClientTest {
	public static final Pattern ETAG_PATTERN = Pattern.compile("^W\\/\"[\\d\\w]{32}\"$");
	
	@ParameterizedTest
	@ArgumentsSource(MainAndModuleProvider.EnumOnly.class)
	public void testNoSql(TestDatabase db) {
		Client client = getAnonymousClient();
		String lastName = "CreatedUnitTest" + System.currentTimeMillis();
		WebTarget target = client.target(getRestUrl(null, db) + "/nosql?lastName=" + lastName); //$NON-NLS-1$
		
		{
			Response response = target.request().get();
			
			String json = response.readEntity(String.class);
			JsonObject jsonObject = Json.createReader(new StringReader(json)).readObject();
			try {
				JsonArray existing = jsonObject.getJsonArray("byQueryLastName");
				assertTrue(existing.isEmpty());
			} catch(Exception e) {
				fail("Encounted unexpected JSON: " + jsonObject, e);
			}
		}
		
		// Now use the MVC endpoint to create one, which admittedly is outside this test
		{
			MultivaluedMap<String, String> payload = new MultivaluedHashMap<>();
			payload.putSingle("firstName", "foo"); //$NON-NLS-1$ //$NON-NLS-2$
			payload.putSingle("lastName", lastName); //$NON-NLS-1$ //$NON-NLS-2$
			payload.putSingle("customProperty", "i am custom property"); //$NON-NLS-1$ //$NON-NLS-2$
			WebTarget postTarget = client.target(getRestUrl(null, db) + "/nosql/create"); //$NON-NLS-1$
			Response response = postTarget.request()
				.accept(MediaType.TEXT_HTML_TYPE) // Ensure that it routes to MVC
				.post(Entity.form(payload));
			assertEquals(303, response.getStatus());
		}
		
		// There should be at least one now
		{
			Response response = target.request().get();
			
			String json = response.readEntity(String.class);
			try {
				JsonObject jsonObject = Json.createReader(new StringReader(json)).readObject();
				
				JsonArray byQueryLastName = jsonObject.getJsonArray("byQueryLastName"); //$NON-NLS-1$
				assertFalse(byQueryLastName.isEmpty());
				Optional<JsonObject> optEntry = byQueryLastName.stream()
					.map(JsonValue::asJsonObject)
					.filter(o -> lastName.equals(o.getString("lastName")))
					.findFirst();
				assertFalse(optEntry.isEmpty(), "Should have found an entry with the expected last name");
				JsonObject entry = optEntry.get();
				assertEquals(lastName, entry.getString("lastName")); //$NON-NLS-1$ //$NON-NLS-2$
				{
					JsonObject customProp = entry.getJsonObject("customProperty"); //$NON-NLS-1$
					String val = customProp.getString("value"); //$NON-NLS-1$
					assertEquals("i am custom property", val); //$NON-NLS-1$
				}
				assertFalse(entry.getString("unid").isEmpty()); //$NON-NLS-1$
				
				int size = entry.getInt("size"); //$NON-NLS-1$
				assertTrue(size > 0);
			} catch(Exception e) {
				fail("Encountered exception with JSON " + json, e);
			}
		}
	}
	
	/**
	 * Tests to make sure a missing firstName is caught, which is enforced at the JAX-RS level.
	 */
	@ParameterizedTest
	@ArgumentsSource(MainAndModuleProvider.EnumOnly.class)
	public void testMissingFirstName(TestDatabase db) {
		Client client = getAnonymousClient();
		
		MultivaluedMap<String, String> payload = new MultivaluedHashMap<>();
		payload.putSingle("lastName", "CreatedUnitTest"); //$NON-NLS-1$ //$NON-NLS-2$
		payload.putSingle("customProperty", "i am custom property"); //$NON-NLS-1$ //$NON-NLS-2$
		WebTarget postTarget = client.target(getRestUrl(null, db) + "/nosql/create"); //$NON-NLS-1$
		Response response = postTarget.request().post(Entity.form(payload));
		String html = response.readEntity(String.class);
		assertEquals(400, response.getStatus(), () -> "Unexpected response code " + response.getStatus() + " with content: " + html);
	}
	
	/**
	 * Tests to make sure a missing lastName is caught, which is enforced at the JNoSQL level.
	 */
	@ParameterizedTest
	@ArgumentsSource(MainAndModuleProvider.EnumOnly.class)
	public void testMissingLastName(TestDatabase db) {
		Client client = getAnonymousClient();
		
		MultivaluedMap<String, String> payload = new MultivaluedHashMap<>();
		payload.putSingle("firstName", "CreatedUnitTest"); //$NON-NLS-1$ //$NON-NLS-2$
		payload.putSingle("customProperty", "i am custom property"); //$NON-NLS-1$ //$NON-NLS-2$
		WebTarget postTarget = client.target(getRestUrl(null, db) + "/nosql/create"); //$NON-NLS-1$
		Response response = postTarget.request().post(Entity.form(payload));
		// NB: this currently throws a 500 due to the exception being UndeclaredThrowableException (Issue #211)
		assertTrue(response.getStatus() >= 400, () -> "Response code should be an error; got " + response.getStatus()); //$NON-NLS-1$
	}

	@ParameterizedTest
	@ArgumentsSource(MainAndModuleProvider.EnumOnly.class)
	public void testNoSqlNames(TestDatabase db) {
		Client client = getAnonymousClient();
		WebTarget target = client.target(getRestUrl(null, db) + "/nosql/servers"); //$NON-NLS-1$
		
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

	@ParameterizedTest
	@ArgumentsSource(MainAndModuleProvider.EnumOnly.class)
	public void testQueryNoteID(TestDatabase db) throws UnsupportedEncodingException {
		Client client = getAdminClient();
		
		String lastName;
		String unid;
		{
			WebTarget postTarget = client.target(getRestUrl(null, db) + "/nosql/create"); //$NON-NLS-1$
			
			lastName = "Fooson" + System.nanoTime();
			MultipartFormDataOutput payload = new MultipartFormDataOutput();
			payload.addFormData("firstName", "Foo" + System.nanoTime(), MediaType.TEXT_PLAIN_TYPE);
			payload.addFormData("lastName", lastName, MediaType.TEXT_PLAIN_TYPE);
			
			Response response = postTarget.request()
				.accept(MediaType.APPLICATION_JSON_TYPE)
				.post(Entity.entity(payload, MediaType.MULTIPART_FORM_DATA_TYPE));
			checkResponse(200, response);
			String json = response.readEntity(String.class);

			JsonObject person = Json.createReader(new StringReader(json)).readObject();
			unid = person.getString("unid");
			assertNotNull(unid);
			assertFalse(unid.isEmpty());
		}
		
		int noteId;
		// Fetch it again to get the note ID
		{
			WebTarget getTarget = client.target(getRestUrl(null, db) + "/nosql/" + unid);
			
			Response response = getTarget.request()
				.accept(MediaType.APPLICATION_JSON_TYPE)
				.get();
			checkResponse(200, response);

			String json = response.readEntity(String.class);
			JsonObject jsonObject = Json.createReader(new StringReader(json)).readObject();
			String getUnid = jsonObject.getString("unid");
			assertEquals(unid, getUnid);
			noteId = jsonObject.getInt("noteId");
			assertNotEquals(0, noteId);
		}
		
		// Find by note ID
		WebTarget queryTarget = client.target(getRestUrl(null, db) + "/nosql/byNoteId/" + Integer.toHexString(noteId));
		
		Response response = queryTarget.request()
			.accept(MediaType.APPLICATION_JSON_TYPE)
			.get();
		checkResponse(200, response);
		String json = response.readEntity(String.class);

		JsonObject result = Json.createReader(new StringReader(json)).readObject();
		assertEquals(noteId, result.getInt("noteId"));
		assertEquals(lastName, result.getString("lastName"));
	}

	@ParameterizedTest
	@ArgumentsSource(MainAndModuleProvider.EnumOnly.class)
	public void testQueryEmail(TestDatabase db) throws UnsupportedEncodingException {
		Client client = getAdminClient();
		
		String lastName;
		String email;
		String unid;
		{
			WebTarget postTarget = client.target(getRestUrl(null, db) + "/nosql/create"); //$NON-NLS-1$
			
			lastName = "Fooson" + System.nanoTime();
			email = "foo" + System.nanoTime() + "@foo.com";
			MultipartFormDataOutput payload = new MultipartFormDataOutput();
			payload.addFormData("firstName", "Foo" + System.nanoTime(), MediaType.TEXT_PLAIN_TYPE);
			payload.addFormData("lastName", lastName, MediaType.TEXT_PLAIN_TYPE);
			payload.addFormData("email", email, MediaType.TEXT_PLAIN_TYPE);
			
			Response response = postTarget.request()
				.accept(MediaType.APPLICATION_JSON_TYPE)
				.post(Entity.entity(payload, MediaType.MULTIPART_FORM_DATA_TYPE));
			checkResponse(200, response);
			String json = response.readEntity(String.class);

			JsonObject person = Json.createReader(new StringReader(json)).readObject();
			unid = person.getString("unid");
			assertNotNull(unid);
			assertFalse(unid.isEmpty());
			assertEquals(lastName, person.getString("lastName", null));
			assertEquals(email, person.getString("email", null));
		}
		
		// Find by note ID
		WebTarget queryTarget = client.target(getRestUrl(null, db) + "/nosql/byEmail/" + URLEncoder.encode(email, "UTF-8"));
		
		Response response = queryTarget.request()
			.accept(MediaType.APPLICATION_JSON_TYPE)
			.get();
		checkResponse(200, response);
		String json = response.readEntity(String.class);

		JsonObject result = Json.createReader(new StringReader(json)).readObject();
		assertEquals(email, result.getString("email"));
		assertEquals(lastName, result.getString("lastName"));
	}

	@ParameterizedTest
	@ArgumentsSource(MainAndModuleProvider.EnumOnly.class)
	public void testQueryNoteIDInt(TestDatabase db) throws UnsupportedEncodingException {
		Client client = getAdminClient();
		
		String lastName;
		String unid;
		{
			WebTarget postTarget = client.target(getRestUrl(null, db) + "/nosql/create"); //$NON-NLS-1$
			
			lastName = "Fooson" + System.nanoTime();
			MultipartFormDataOutput payload = new MultipartFormDataOutput();
			payload.addFormData("firstName", "Foo" + System.nanoTime(), MediaType.TEXT_PLAIN_TYPE);
			payload.addFormData("lastName", lastName, MediaType.TEXT_PLAIN_TYPE);
			
			Response response = postTarget.request()
				.accept(MediaType.APPLICATION_JSON_TYPE)
				.post(Entity.entity(payload, MediaType.MULTIPART_FORM_DATA_TYPE));
			checkResponse(200, response);
			String json = response.readEntity(String.class);

			JsonObject person = Json.createReader(new StringReader(json)).readObject();
			unid = person.getString("unid");
			assertNotNull(unid);
			assertFalse(unid.isEmpty());
		}
		
		int noteId;
		// Fetch it again to get the note ID
		{
			WebTarget getTarget = client.target(getRestUrl(null, db) + "/nosql/" + unid);
			
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
		WebTarget queryTarget = client.target(getRestUrl(null, db) + "/nosql/byNoteIdInt/" + noteId);
		
		Response response = queryTarget.request()
			.accept(MediaType.APPLICATION_JSON_TYPE)
			.get();
		checkResponse(200, response);
		String json = response.readEntity(String.class);

		JsonObject result = Json.createReader(new StringReader(json)).readObject();
		assertEquals(noteId, result.getInt("noteId"));
		assertEquals(lastName, result.getString("lastName"));
	}

	@ParameterizedTest
	@ArgumentsSource(MainAndModuleProvider.EnumOnly.class)
	public void testQueryModTime(TestDatabase db) throws UnsupportedEncodingException, InterruptedException {
		Client client = getAdminClient();
		
		String lastName;
		String firstName;
		String unid;
		{
			WebTarget postTarget = client.target(getRestUrl(null, db) + "/nosql/create"); //$NON-NLS-1$
			
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
			
			WebTarget postTarget = client.target(getRestUrl(null, db) + "/nosql/" + unid);
			
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
			WebTarget getTarget = client.target(getRestUrl(null, db) + "/nosql/" + unid);
			
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
			
			// Modified in this file should be close to the same, since it's the same NSF
			String modifiedInThisFile = jsonObject.getString("modifiedInFile");
			assertNotNull(modifiedInThisFile);
			Instant modifiedInst = Instant.from(DateTimeFormatter.ISO_INSTANT.parse(modified));
			Instant modifiedInThisFileInst = Instant.from(DateTimeFormatter.ISO_INSTANT.parse(modifiedInThisFile));
			assertEquals(modifiedInst.truncatedTo(ChronoUnit.SECONDS), modifiedInThisFileInst.truncatedTo(ChronoUnit.SECONDS));
			
			String created = jsonObject.getString("created");
			assertNotNull(created);
			assertFalse(created.isEmpty());
			assertNotEquals(modified, created);
			
			// Added should match created
			String added = jsonObject.getString("addedToFile");
			assertNotNull(added);
			assertFalse(added.isEmpty());
			assertInstantsCloseEnough(created, added);
		}
		
		// Find by modified
		WebTarget queryTarget = client.target(getRestUrl(null, db) + "/nosql/modifiedSince/" + URLEncoder.encode(modified, "UTF-8"));
		
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

	@ParameterizedTest
	@ArgumentsSource(MainAndModuleProvider.EnumOnly.class)
	public void testQueryByNoteIdNotFound(TestDatabase db) {
		Client client = getAdminClient();
		WebTarget getTarget = client.target(getRestUrl(null, db) + "/nosql/byNoteId/doesNotExist");
		
		Response response = getTarget.request()
			.accept(MediaType.APPLICATION_JSON_TYPE)
			.get();
		assertEquals(404, response.getStatus());
	}

	@ParameterizedTest
	@ArgumentsSource(MainAndModuleProvider.EnumOnly.class)
	public void testMultipartCreate(TestDatabase db) {
		Client client = getAnonymousClient();
		String unid;
		String lastName = "Fooson" + System.nanoTime();
		{
			WebTarget postTarget = client.target(getRestUrl(null, db) + "/nosql/create"); //$NON-NLS-1$
			
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
			WebTarget getTarget = client.target(getRestUrl(null, db) + "/nosql/" + unid);
			
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

	@ParameterizedTest
	@ArgumentsSource(MainAndModuleProvider.EnumOnly.class)
	public void testAttachmentCreate(TestDatabase db) {
		Client client = getAnonymousClient();
		String unid;
		String lastName = "Fooson" + System.nanoTime();
		{
			WebTarget postTarget = client.target(getRestUrl(null, db) + "/nosql/create"); //$NON-NLS-1$
			
			MultipartFormDataOutput payload = new MultipartFormDataOutput();
			payload.addFormData("firstName", "Foo", MediaType.TEXT_PLAIN_TYPE);
			payload.addFormData("lastName", lastName, MediaType.TEXT_PLAIN_TYPE);
			payload.addFormData("attachment", "<p>I am foo HTML</p>", MediaType.TEXT_HTML_TYPE, "foo.html");
			
			Response response = postTarget.request()
				.accept(MediaType.APPLICATION_JSON_TYPE)
				.post(Entity.entity(payload, MediaType.MULTIPART_FORM_DATA_TYPE));
			checkResponse(200, response);

			String json = response.readEntity(String.class);
			JsonObject jsonObject = Json.createReader(new StringReader(json)).readObject();
			unid = jsonObject.getString("unid");
			assertNotNull(unid);
			assertFalse(unid.isEmpty());
		}
		
		// Fetch the doc by UNID
		{
			WebTarget getTarget = client.target(getRestUrl(null, db) + "/nosql/" + unid);
			
			Response response = getTarget.request()
				.accept(MediaType.APPLICATION_JSON_TYPE)
				.get();
			checkResponse(200, response);

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
			WebTarget getTarget = client.target(getRestUrl(null, db) + "/nosql/" + unid + "/attachment/foo.html");

			Response response = getTarget.request().get();
			checkResponse(200, response);

			String html = response.readEntity(String.class);
			assertEquals("<p>I am foo HTML</p>", html);
			

			// Make sure the ETag exists and looks like what we'd expect
			String etag = response.getHeaderString("ETag");
			assertNotNull(etag);
			assertTrue(ETAG_PATTERN.matcher(etag).matches(), () -> "ETag didn't match format: " + etag);
		}
	}
	
	public static class EnumAndFolderPathsProvider implements ArgumentsProvider {
		@Override
		public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
			return new MainAndModuleProvider.EnumAndBrowser().provideArguments(context)
				.map(args -> args.get()[0])
				.flatMap(e ->
					Stream.of("inFolder", "inFolderManual")
						.map(page -> Arguments.of(e, page))
				);
		}
	}
	
	/**
	 * Tests both the /inFolder and /inFolderManual endpoints, which will
	 * exercise both the {@code @ViewEntries} annotation and the
	 * {@code readViewEntries} method on {@code Repository}.
	 * 
	 * @param endpoint the endpoint tested in this run
	 */
	@ParameterizedTest
	@ArgumentsSource(EnumAndFolderPathsProvider.class)
	public void testFolderOperations(TestDatabase db, String endpoint) {
		Client client = getAdminClient();
		String unid;
		String lastName = "Fooson" + System.nanoTime();
		{
			WebTarget postTarget = client.target(getRestUrl(null, db) + "/nosql/create"); //$NON-NLS-1$
			
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
			WebTarget getTarget = client.target(getRestUrl(null, db) + "/nosql/" + endpoint); //$NON-NLS-1$
			
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
			WebTarget postTarget = client.target(getRestUrl(null, db) + "/nosql/" + unid + "/putInFolder");

			Response response = postTarget.request().post(Entity.form(new MultivaluedHashMap<>()));
			String json = response.readEntity(String.class);
			assertEquals(200, response.getStatus(), () -> "Received unexpected response code " + response.getStatus() + ": " + json);
		}
		
		// Make sure it's in the folder now
		assertTrue(isInFolder.test(unid));
		
		// Remove it from the folder
		{
			WebTarget postTarget = client.target(getRestUrl(null, db) + "/nosql/" + unid + "/removeFromFolder");

			Response response = postTarget.request().post(Entity.form(new MultivaluedHashMap<>()));
			String json = response.readEntity(String.class);
			assertEquals(200, response.getStatus(), () -> "Received unexpected response code " + response.getStatus() + ": " + json);
		}
		
		// Make sure it's not in the folder
		assertFalse(isInFolder.test(unid));
	}
	
	public static class EnumAndEmailPathsProvider implements ArgumentsProvider {
		@Override
		public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
			return new MainAndModuleProvider.EnumAndBrowser().provideArguments(context)
				.map(args -> args.get()[0])
				.flatMap(e ->
					Stream.of("queryByEmail", "queryByEmailEntries", "queryByEmailOneKey", "queryByEmailOneKey?resort=true")
						.map(page -> Arguments.of(e, page))
				);
		}
	}
	
	/**
	 * @see <a href="https://github.com/OpenNTF/org.openntf.xsp.jakartaee/issues/463">Issue #463</a>
	 */
	@ParameterizedTest
	@ArgumentsSource(EnumAndEmailPathsProvider.class)
	public void testListByKey(TestDatabase db, String endpoint) throws UnsupportedEncodingException {
		Client client = getAnonymousClient();
		String email = "Foo" + System.currentTimeMillis();
		WebTarget target = client.target(getRestUrl(null, db) + "/nosql/" + endpoint + (endpoint.contains("?") ? "&" : "?") + "q=" + URLEncoder.encode(email, "UTF-8")); //$NON-NLS-1$
		
		{
			Response response = target.request().get();
			
			String json = response.readEntity(String.class);
			try {
				JsonArray result = Json.createReader(new StringReader(json)).readArray();
				
				assertTrue(result.isEmpty(), () -> "Unexpected JSON: " + json);
			} catch(Exception e) {
				fail("Unexpected JSON: " + json, e);
			}
		}
		
		// Now use the MVC endpoint to create one, which admittedly is outside this test
		{
			MultivaluedMap<String, String> payload = new MultivaluedHashMap<>();
			payload.putSingle("firstName", "foo"); //$NON-NLS-1$ //$NON-NLS-2$
			payload.putSingle("lastName", "CreatedUnitTest"); //$NON-NLS-1$ //$NON-NLS-2$
			payload.putSingle("email", email); //$NON-NLS-1$ //$NON-NLS-2$
			WebTarget postTarget = client.target(getRestUrl(null, db) + "/nosql/create"); //$NON-NLS-1$
			Response response = postTarget.request()
				.accept(MediaType.TEXT_HTML_TYPE) // Ensure that it routes to MVC
				.post(Entity.form(payload));
			assertEquals(303, response.getStatus(), () -> {
				String res = response.readEntity(String.class);
				return "Unexpected response: " + res;
			});
		}
		
		// There should be at least one now
		{
			Response response = target.request().get();
			
			String json = response.readEntity(String.class);
			try {
				JsonArray result = Json.createReader(new StringReader(json)).readArray();
				
				assertFalse(result.isEmpty(), () -> "Unexpected JSON: " + json);
				JsonObject entry = result.getJsonObject(0);
				assertEquals("CreatedUnitTest", entry.getString("lastName")); //$NON-NLS-1$ //$NON-NLS-2$
				assertEquals(email, entry.getString("email"));
				assertFalse(entry.getString("unid").isEmpty()); //$NON-NLS-1$
			} catch(Exception e) {
				fail("Unexpected JSON: " + json, e);
			}
		}
	}
	
	@ParameterizedTest
	@ArgumentsSource(MainAndModuleProvider.EnumOnly.class)
	public void testAccessRights(TestDatabase db) {
		{
			Client client = getAnonymousClient();
			WebTarget target = client.target(getRestUrl(null, db) + "/nosql/accessRights"); //$NON-NLS-1$

			Response response = target.request().get();
			checkResponse(200, response);
			
			JsonObject result = response.readEntity(JsonObject.class);
			assertEquals("Anonymous", result.getString("name"));
			assertEquals("AUTHOR", result.getString("level"));
			
			assertIterableEquals(Json.createArrayBuilder(List.of("CREATE_DOCS","READ_PUBLIC_DOCS","REPLICATE_COPY_DOCS","WRITE_PUBLIC_DOCS")).build(), result.getJsonArray("privileges"), () -> "Unexpected privileges: " + result.getJsonArray("privileges"));
			assertIterableEquals(Json.createArrayBuilder().build(), result.getJsonArray("roles"), () -> "Unexpected roles: " + result.getJsonArray("roles"));
		}
		{
			Client client = getAdminClient();
			WebTarget target = client.target(getRestUrl(null, db) + "/nosql/accessRights"); //$NON-NLS-1$

			Response response = target.request().get();
			checkResponse(200, response);
			
			JsonObject result = response.readEntity(JsonObject.class);
			assertEquals("CN=Jakarta EE Test/O=OpenNTFTest", result.getString("name"));
			assertEquals("EDITOR", result.getString("level"));
			assertIterableEquals(Json.createArrayBuilder(List.of("CREATE_DOCS","CREATE_PRIV_AGENTS","CREATE_PRIV_FOLDERS_VIEWS","CREATE_SCRIPT_AGENTS","CREATE_SHARED_FOLDERS_VIEWS","DELETE_DOCS","READ_PUBLIC_DOCS","REPLICATE_COPY_DOCS","WRITE_PUBLIC_DOCS")).build(), result.getJsonArray("privileges"), () -> "Unexpected privileges: " + result.getJsonArray("privileges"));
			assertIterableEquals(Json.createArrayBuilder().add("[Admin]").build(), result.getJsonArray("roles"), () -> "Unexpected roles: " + result.getJsonArray("roles"));
		}
	}
	
	@ParameterizedTest
	@ArgumentsSource(MainAndModuleProvider.EnumOnly.class)
	public void testAccessRightsNames(TestDatabase db) {
		{
			Client client = getAnonymousClient();
			WebTarget target = client.target(getRestUrl(null, db) + "/nosql/accessRightsNames"); //$NON-NLS-1$

			Response response = target.request().get();
			checkResponse(200, response);
			
			JsonObject result = response.readEntity(JsonObject.class);
			assertEquals("CN=JakartaEE/O=OpenNTFTest", result.getString("name"));
			assertEquals("EDITOR", result.getString("level"));
			assertIterableEquals(Json.createArrayBuilder(List.of("CREATE_DOCS","CREATE_PRIV_AGENTS","CREATE_PRIV_FOLDERS_VIEWS","CREATE_SCRIPT_AGENTS","CREATE_SHARED_FOLDERS_VIEWS","DELETE_DOCS","READ_PUBLIC_DOCS","REPLICATE_COPY_DOCS","WRITE_PUBLIC_DOCS")).build(), result.getJsonArray("privileges"), () -> "Unexpected privileges: " + result.getJsonArray("privileges"));
			assertIterableEquals(Json.createArrayBuilder(List.of("[DenyAccessRead]","[GroupCreator]","[GroupModifier]","[NetCreator]","[NetModifier]","[PolicyCreator]","[PolicyModifier]","[PolicyReader]","[ServerCreator]","[ServerModifier]","[UserCreator]","[UserModifier]")).build(), result.getJsonArray("roles"), () -> "Unexpected roles: " + result.getJsonArray("roles"));
		}
		{
			Client client = getAdminClient();
			WebTarget target = client.target(getRestUrl(null, db) + "/nosql/accessRightsNames"); //$NON-NLS-1$

			Response response = target.request().get();
			checkResponse(200, response);
			
			JsonObject result = response.readEntity(JsonObject.class);
			assertEquals("CN=JakartaEE/O=OpenNTFTest", result.getString("name"));
			assertEquals("EDITOR", result.getString("level"));
			assertIterableEquals(Json.createArrayBuilder(List.of("CREATE_DOCS","CREATE_PRIV_AGENTS","CREATE_PRIV_FOLDERS_VIEWS","CREATE_SCRIPT_AGENTS","CREATE_SHARED_FOLDERS_VIEWS","DELETE_DOCS","READ_PUBLIC_DOCS","REPLICATE_COPY_DOCS","WRITE_PUBLIC_DOCS")).build(), result.getJsonArray("privileges"), () -> "Unexpected privileges: " + result.getJsonArray("privileges"));
			assertIterableEquals(Json.createArrayBuilder(List.of("[DenyAccessRead]","[GroupCreator]","[GroupModifier]","[NetCreator]","[NetModifier]","[PolicyCreator]","[PolicyModifier]","[PolicyReader]","[ServerCreator]","[ServerModifier]","[UserCreator]","[UserModifier]")).build(), result.getJsonArray("roles"), () -> "Unexpected roles: " + result.getJsonArray("roles"));
		}
	}
}
