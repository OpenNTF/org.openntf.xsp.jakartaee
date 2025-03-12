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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.StringReader;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Optional;
import java.util.Random;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.w3c.dom.Element;

import it.org.openntf.xsp.jakartaee.AbstractWebClientTest;
import it.org.openntf.xsp.jakartaee.TestDatabase;
import it.org.openntf.xsp.jakartaee.TestDomUtil;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;
import jakarta.json.bind.annotation.JsonbTransient;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;

@SuppressWarnings("nls")
public class TestNoSQLExampleDocs extends AbstractWebClientTest {
	@Test
	public void testExampleDoc() {
		Client client = getAnonymousClient();
		
		// Create a new doc
		String unid;
		{
			MultivaluedMap<String, String> payload = new MultivaluedHashMap<>();
			payload.putSingle("title", "foo");
			payload.put("categories", Arrays.asList("foo", "bar"));
			
			WebTarget postTarget = client.target(getRestUrl(null, TestDatabase.MAIN) + "/exampleDocs");
			Response response = postTarget.request().post(Entity.form(payload));
			checkResponse(200, response);

			String json = response.readEntity(String.class);
			JsonObject jsonObject = Json.createReader(new StringReader(json)).readObject();
			unid = jsonObject.getString("unid");
			assertNotNull(unid);
			assertFalse(unid.isEmpty());
		}
		
		// Fetch the doc
		{
			WebTarget target = client.target(getRestUrl(null, TestDatabase.MAIN) + "/exampleDocs/" + unid);
			Response response = target.request().get();
			checkResponse(200, response);
			String json = response.readEntity(String.class);

			JsonObject jsonObject = Json.createReader(new StringReader(json)).readObject();
			
			assertEquals(unid, jsonObject.getString("unid"));
			
			String dxl = jsonObject.getString("dxl");
			assertNotNull(dxl);
			assertFalse(dxl.isEmpty());
			
			org.w3c.dom.Document xmlDoc = TestDomUtil.createDocument(dxl);
			assertNotNull(xmlDoc);
			String title = TestDomUtil.nodes(xmlDoc, "//*[name()='item'][@name='$$Title']/*[name()='text']/text()").get(0).getNodeValue();
			assertEquals("foo", title);
		}
	}
	
	@Test
	public void testExampleDocAuthors() {
		Client client = getAnonymousClient();
		
		// Create a new doc
		String unid;
		{
			MultivaluedMap<String, String> payload = new MultivaluedHashMap<>();
			payload.putSingle("title", "foo");
			payload.put("categories", Arrays.asList("foo", "bar"));
			payload.put("authors", Arrays.asList("CN=foo fooson/O=Bar"));
			
			WebTarget postTarget = client.target(getRestUrl(null, TestDatabase.MAIN) + "/exampleDocs");
			Response response = postTarget.request().post(Entity.form(payload));
			checkResponse(200, response);

			String json = response.readEntity(String.class);
			JsonObject jsonObject = Json.createReader(new StringReader(json)).readObject();
			unid = jsonObject.getString("unid");
			assertNotNull(unid);
			assertFalse(unid.isEmpty());
		}
		
		// Fetch the doc
		{
			WebTarget target = client.target(getRestUrl(null, TestDatabase.MAIN) + "/exampleDocs/" + unid);
			Response response = target.request().get();
			checkResponse(200, response);
			String json = response.readEntity(String.class);

			JsonObject jsonObject = Json.createReader(new StringReader(json)).readObject();
			
			assertEquals(unid, jsonObject.getString("unid"));
			
			String dxl = jsonObject.getString("dxl");
			assertNotNull(dxl);
			assertFalse(dxl.isEmpty());
			
			org.w3c.dom.Document xmlDoc = TestDomUtil.createDocument(dxl);
			assertNotNull(xmlDoc);
			Element authors = (Element)TestDomUtil.nodes(xmlDoc, "//*[name()='item'][@name='Authors']").get(0);
			assertNotNull(authors);
			assertEquals("true", authors.getAttribute("authors"));
		}
	}

	@Test
	public void testComputeWithForm() {
		Client client = getAnonymousClient();
		
		// Create a new doc
		String unid;
		{
			MultivaluedMap<String, String> payload = new MultivaluedHashMap<>();
			payload.putSingle("title", "foo");
			payload.put("categories", Arrays.asList("foo", "bar"));
			
			WebTarget postTarget = client.target(getRestUrl(null, TestDatabase.MAIN) + "/exampleDocs");
			Response response = postTarget.request().post(Entity.form(payload));
			checkResponse(200, response);

			String json = response.readEntity(String.class);
			JsonObject jsonObject = Json.createReader(new StringReader(json)).readObject();
			unid = jsonObject.getString("unid");
			assertNotNull(unid);
			assertFalse(unid.isEmpty());
		}
		
		// Fetch the doc
		{
			WebTarget target = client.target(getRestUrl(null, TestDatabase.MAIN) + "/exampleDocs/" + unid);
			Response response = target.request().get();
			checkResponse(200, response);
			String json = response.readEntity(String.class);

			JsonObject jsonObject = Json.createReader(new StringReader(json)).readObject();
			
			assertEquals(unid, jsonObject.getString("unid"));
			
			String dxl = jsonObject.getString("dxl");
			assertNotNull(dxl);
			assertFalse(dxl.isEmpty());
			
			org.w3c.dom.Document xmlDoc = TestDomUtil.createDocument(dxl);
			assertNotNull(xmlDoc);
			String val = TestDomUtil.nodes(xmlDoc, "//*[name()='item'][@name='DefaultValue']/*[name()='text']/text()").get(0).getNodeValue();
			assertNotNull(val);
			assertEquals("I am the default value", val);
		}
	}
	
	@Test
	public void testItemStorage() {
		Client client = getAnonymousClient();
		// Create a new doc
		String unid;
		{
			JsonObject jsonGuy = Json.createObjectBuilder()
				.add("firstName", "Foo")
				.add("lastName", "Fooson")
				.build();
			JsonObject mimeGuy = Json.createObjectBuilder()
				.add("title", "I am the title")
				.add("address", "123 Road St.")
				.build();
			JsonObject payloadJson = Json.createObjectBuilder()
				.add("title", "I am outer title")
				.add("jsonGuy", jsonGuy)
				.add("mimeGuy", mimeGuy)
				.add("body", "<p>I am body HTML</p>")
				.build();
			
			WebTarget postTarget = client.target(getRestUrl(null, TestDatabase.MAIN) + "/exampleDocs");
			Response response = postTarget.request().post(Entity.json(payloadJson.toString()));
			checkResponse(200, response);

			String json = response.readEntity(String.class);
			JsonObject jsonObject = Json.createReader(new StringReader(json)).readObject();
			unid = jsonObject.getString("unid");
			assertNotNull(unid);
			assertFalse(unid.isEmpty());
		}
		
		// Fetch the doc
		{
			WebTarget target = client.target(getRestUrl(null, TestDatabase.MAIN) + "/exampleDocs/" + unid);
			Response response = target.request().get();
			checkResponse(200, response);
			String json = response.readEntity(String.class);

			JsonObject jsonObject = Json.createReader(new StringReader(json)).readObject();
			
			assertEquals(unid, jsonObject.getString("unid"));
			
			assertEquals("I am outer title", jsonObject.getString("title"));
			assertEquals("<p>I am body HTML</p>", jsonObject.getString("body"));
			JsonObject jsonGuy = jsonObject.getJsonObject("jsonGuy");
			assertEquals("Foo", jsonGuy.getString("firstName"));
			assertEquals("Fooson", jsonGuy.getString("lastName"));
			JsonObject mimeGuy = jsonObject.getJsonObject("mimeGuy");
			assertEquals("I am the title", mimeGuy.getString("title"));
			assertEquals("123 Road St.", mimeGuy.getString("address"));

			// Make sure all the types are what we'd expect
			String dxl = jsonObject.getString("dxl");
			assertNotNull(dxl);
			assertFalse(dxl.isEmpty());
		}
	}
	
	@Test
	public void testJsonStorageReadViewEntries() {
		Client client = getAnonymousClient();
		// Create a new doc
		String unid;
		JsonObject jsonGuy = Json.createObjectBuilder()
			.add("firstName", "Foo")
			.add("lastName", "Fooson")
			.build();
		{
			JsonObject mimeGuy = Json.createObjectBuilder()
				.add("title", "I am the title")
				.add("address", "123 Road St.")
				.build();
			JsonObject payloadJson = Json.createObjectBuilder()
				.add("title", "I am outer title")
				.add("jsonGuy", jsonGuy)
				.add("mimeGuy", mimeGuy)
				.add("body", "<p>I am body HTML</p>")
				.build();
			
			WebTarget postTarget = client.target(getRestUrl(null, TestDatabase.MAIN) + "/exampleDocs");
			Response response = postTarget.request().post(Entity.json(payloadJson.toString()));
			checkResponse(200, response);

			String json = response.readEntity(String.class);
			JsonObject jsonObject = Json.createReader(new StringReader(json)).readObject();
			unid = jsonObject.getString("unid");
			assertNotNull(unid);
			assertFalse(unid.isEmpty());
		}
		
		// Make sure it shows up in the view entries
		{
			WebTarget target = client.target(getRestUrl(null, TestDatabase.MAIN) + "/exampleDocs/inView");
			Response response = target.request().get();
			checkResponse(200, response);
			
			String json = response.readEntity(String.class);
			JsonArray jsonObjects = Json.createReader(new StringReader(json)).readArray();
			assertNotNull(jsonObjects);
			assertFalse(jsonObjects.isEmpty());
			
			Optional<JsonObject> entry = jsonObjects.stream()
				.map(JsonValue::asJsonObject)
				.filter(obj -> unid.equals(obj.getString("unid")) && "DOCUMENT".equals(obj.getString("entryType")))
				.findFirst();
			assertTrue(entry.isPresent());
			JsonObject entryJsonGuy = entry.get().getJsonObject("jsonGuy");
			assertEquals(jsonGuy, entryJsonGuy);
		}
	}
	
	@Test
	public void testItemStorageJsonp() {
		Client client = getAnonymousClient();
		// Create a new doc
		String unid;
		JsonObject jsonpGuy = Json.createObjectBuilder()
			.add("firstName", "Foo")
			.add("lastName", "Fooson")
			.build();
		{
			JsonObject payloadJson = Json.createObjectBuilder()
				.add("title", "I am outer title")
				.add("jsonpGuy", jsonpGuy)
				.add("body", "<p>I am body HTML</p>")
				.build();
			
			WebTarget postTarget = client.target(getRestUrl(null, TestDatabase.MAIN) + "/exampleDocs");
			Response response = postTarget.request().post(Entity.json(payloadJson.toString()));
			checkResponse(200, response);

			String json = response.readEntity(String.class);
			JsonObject jsonObject = Json.createReader(new StringReader(json)).readObject();
			unid = jsonObject.getString("unid");
			assertNotNull(unid);
			assertFalse(unid.isEmpty());
		}
		
		// Fetch the doc
		{
			WebTarget target = client.target(getRestUrl(null, TestDatabase.MAIN) + "/exampleDocs/" + unid);
			Response response = target.request().get();
			checkResponse(200, response);
			String json = response.readEntity(String.class);

			JsonObject jsonObject = Json.createReader(new StringReader(json)).readObject();
			
			assertEquals(unid, jsonObject.getString("unid"));
			
			assertEquals("I am outer title", jsonObject.getString("title"));
			assertEquals("<p>I am body HTML</p>", jsonObject.getString("body"));
			try {
				JsonObject jsonGuy = jsonObject.getJsonObject("jsonpGuy");
				assertEquals(jsonpGuy, jsonGuy);
			} catch(ClassCastException e) {
				fail("Received unexpected JSON: " + json, e);
			}

			// Make sure all the types are what we'd expect
			String dxl = jsonObject.getString("dxl");
			assertNotNull(dxl);
			assertFalse(dxl.isEmpty());
		}
	}
	
	@Test
	public void testJsonpStorageReadViewEntries() {
		Client client = getAnonymousClient();
		// Create a new doc
		String unid;
		JsonObject jsonGuy = Json.createObjectBuilder()
			.add("firstName", "Foo")
			.add("lastName", "Fooson")
			.build();
		{
			JsonObject mimeGuy = Json.createObjectBuilder()
				.add("title", "I am the title")
				.add("address", "123 Road St.")
				.build();
			JsonObject payloadJson = Json.createObjectBuilder()
				.add("title", "I am outer title")
				.add("jsonpGuy", jsonGuy)
				.add("mimeGuy", mimeGuy)
				.add("body", "<p>I am body HTML</p>")
				.build();
			
			WebTarget postTarget = client.target(getRestUrl(null, TestDatabase.MAIN) + "/exampleDocs");
			Response response = postTarget.request().post(Entity.json(payloadJson.toString()));
			checkResponse(200, response);

			String json = response.readEntity(String.class);
			JsonObject jsonObject = Json.createReader(new StringReader(json)).readObject();
			unid = jsonObject.getString("unid");
			assertNotNull(unid);
			assertFalse(unid.isEmpty());
		}
		
		// Make sure it shows up in the view entries
		{
			WebTarget target = client.target(getRestUrl(null, TestDatabase.MAIN) + "/exampleDocs/inView");
			Response response = target.request().get();
			checkResponse(200, response);
			
			String json = response.readEntity(String.class);
			JsonArray jsonObjects = Json.createReader(new StringReader(json)).readArray();
			assertNotNull(jsonObjects);
			assertFalse(jsonObjects.isEmpty());
			
			Optional<JsonObject> entry = jsonObjects.stream()
				.map(JsonValue::asJsonObject)
				.filter(obj -> unid.equals(obj.getString("unid")) && "DOCUMENT".equals(obj.getString("entryType")))
				.findFirst();
			assertTrue(entry.isPresent());
			JsonObject entryJsonGuy = entry.get().getJsonObject("jsonpGuy");
			assertEquals(jsonGuy, entryJsonGuy);
		}
	}
	
	@Test
	@Disabled("Pending issue #482")
	public void testItemStorageJsonArray() {
		Client client = getAnonymousClient();
		// Create a new doc
		String unid;
		{
			JsonObject jsonGuy = Json.createObjectBuilder()
				.add("firstName", "Foo")
				.add("lastName", "Fooson")
				.build();
			JsonObject mimeGuy = Json.createObjectBuilder()
				.add("title", "I am the title")
				.add("address", "123 Road St.")
				.build();
			JsonObject payloadJson = Json.createObjectBuilder()
				.add("title", "I am outer title")
				.add("jsonArrayGuy", Json.createArrayBuilder().add(jsonGuy).build())
				.add("mimeGuy", mimeGuy)
				.add("body", "<p>I am body HTML</p>")
				.build();
			
			WebTarget postTarget = client.target(getRestUrl(null, TestDatabase.MAIN) + "/exampleDocs");
			Response response = postTarget.request().post(Entity.json(payloadJson.toString()));
			checkResponse(200, response);

			String json = response.readEntity(String.class);
			JsonObject jsonObject = Json.createReader(new StringReader(json)).readObject();
			unid = jsonObject.getString("unid");
			assertNotNull(unid);
			assertFalse(unid.isEmpty());
		}
		
		// Fetch the doc
		{
			WebTarget target = client.target(getRestUrl(null, TestDatabase.MAIN) + "/exampleDocs/" + unid);
			Response response = target.request().get();
			checkResponse(200, response);
			String json = response.readEntity(String.class);

			JsonObject jsonObject = Json.createReader(new StringReader(json)).readObject();
			
			assertEquals(unid, jsonObject.getString("unid"));
			
			assertEquals("I am outer title", jsonObject.getString("title"));
			assertEquals("<p>I am body HTML</p>", jsonObject.getString("body"));
			try {
				JsonArray jsonArray = jsonObject.getJsonArray("jsonArrayGuy");
				JsonObject jsonGuy = jsonArray.getJsonObject(0);
				assertEquals("Foo", jsonGuy.getString("firstName"));
				assertEquals("Fooson", jsonGuy.getString("lastName"));
			} catch(Exception e) {
				fail("Received unexpected JSON: " + json, e);
			}
			JsonObject mimeGuy = jsonObject.getJsonObject("mimeGuy");
			assertEquals("I am the title", mimeGuy.getString("title"));
			assertEquals("123 Road St.", mimeGuy.getString("address"));

			// Make sure all the types are what we'd expect
			String dxl = jsonObject.getString("dxl");
			assertNotNull(dxl);
			assertFalse(dxl.isEmpty());
		}
	}
	
	@Test
	public void testSaveToDisk() {
		Client client = getAdminClient();
		// Create a new doc
		String unid;
		{
			JsonObject payloadJson = Json.createObjectBuilder()
				.add("title", "I am saveToDisk guy")
				.add("computedValue", "I am written by the test")
				.build();
			
			WebTarget postTarget = client.target(getRestUrl(null, TestDatabase.MAIN) + "/exampleDocs");
			Response response = postTarget.request().post(Entity.json(payloadJson.toString()));
			checkResponse(200, response);

			String json = response.readEntity(String.class);
			JsonObject jsonObject = Json.createReader(new StringReader(json)).readObject();
			unid = jsonObject.getString("unid");
			assertNotNull(unid);
			assertFalse(unid.isEmpty());
		}
		
		// Fetch the doc
		{
			WebTarget target = client.target(getRestUrl(null, TestDatabase.MAIN) + "/exampleDocs/" + unid);
			Response response = target.request().get();
			checkResponse(200, response);
			String json = response.readEntity(String.class);

			JsonObject jsonObject = Json.createReader(new StringReader(json)).readObject();
			
			assertEquals(unid, jsonObject.getString("unid"));
			
			assertEquals("I am saveToDisk guy", jsonObject.getString("title"));
			assertFalse(jsonObject.containsKey("computedValue"));
		}
		
		// Update to try to set the computed value
		{
			JsonObject payloadJson = Json.createObjectBuilder()
				.add("title", "I am saveToDisk guy!")
				.add("computedValue", "I am written by the test again")
				.build();
			
			WebTarget target = client.target(getRestUrl(null, TestDatabase.MAIN) + "/exampleDocs/" + unid);
			Response response = target.request().put(Entity.json(payloadJson.toString()));
			checkResponse(200, response);
		}
		
		// Fetch it again
		{
			WebTarget target = client.target(getRestUrl(null, TestDatabase.MAIN) + "/exampleDocs/" + unid);
			Response response = target.request().get();
			checkResponse(200, response);
			String json = response.readEntity(String.class);

			JsonObject jsonObject = Json.createReader(new StringReader(json)).readObject();
			
			assertEquals(unid, jsonObject.getString("unid"));
			
			assertEquals("I am saveToDisk guy!", jsonObject.getString("title"));
			assertFalse(jsonObject.containsKey("computedValue"));
		}
	}
	
	@Test
	public void testNullDate() {
		Client client = getAdminClient();
		// Create a new doc
		String unid;
		{
			JsonObject payloadJson = Json.createObjectBuilder()
				.add("title", "I am nullDate guy")
				.add("dateGuy", "2023-09-13")
				.build();
			
			WebTarget postTarget = client.target(getRestUrl(null, TestDatabase.MAIN) + "/exampleDocs");
			Response response = postTarget.request().post(Entity.json(payloadJson.toString()));
			checkResponse(200, response);

			String json = response.readEntity(String.class);
			JsonObject jsonObject = Json.createReader(new StringReader(json)).readObject();
			unid = jsonObject.getString("unid");
			assertNotNull(unid);
			assertFalse(unid.isEmpty());
		}
		
		// Fetch the doc
		{
			WebTarget target = client.target(getRestUrl(null, TestDatabase.MAIN) + "/exampleDocs/" + unid);
			Response response = target.request().get();
			checkResponse(200, response);
			String json = response.readEntity(String.class);

			JsonObject jsonObject = Json.createReader(new StringReader(json)).readObject();
			
			assertEquals(unid, jsonObject.getString("unid"));
			
			assertEquals("I am nullDate guy", jsonObject.getString("title"));
			assertEquals("2023-09-13", jsonObject.getString("dateGuy"));
		}
		
		// Update to set the date to something else
		{
			JsonObject payloadJson = Json.createObjectBuilder()
				.add("title", "I am nullDate guy!")
				.add("dateGuy", "2023-09-14")
				.build();
			
			WebTarget target = client.target(getRestUrl(null, TestDatabase.MAIN) + "/exampleDocs/" + unid);
			Response response = target.request().put(Entity.json(payloadJson.toString()));
			checkResponse(200, response);
		}
		
		// Fetch it again
		{
			WebTarget target = client.target(getRestUrl(null, TestDatabase.MAIN) + "/exampleDocs/" + unid);
			Response response = target.request().get();
			checkResponse(200, response);
			String json = response.readEntity(String.class);

			JsonObject jsonObject = Json.createReader(new StringReader(json)).readObject();
			
			assertEquals(unid, jsonObject.getString("unid"));

			assertEquals("I am nullDate guy!", jsonObject.getString("title"));
			assertEquals("2023-09-14", jsonObject.getString("dateGuy"));
		}
		
		// Update to set null for the date value
		{
			JsonObject payloadJson = Json.createObjectBuilder()
				.add("title", "I am nullDate guy!!")
				.addNull("dateGuy")
				.build();
			
			WebTarget target = client.target(getRestUrl(null, TestDatabase.MAIN) + "/exampleDocs/" + unid);
			Response response = target.request().put(Entity.json(payloadJson.toString()));
			checkResponse(200, response);
		}

		// Fetch it again
		{
			WebTarget target = client.target(getRestUrl(null, TestDatabase.MAIN) + "/exampleDocs/" + unid);
			Response response = target.request().get();
			checkResponse(200, response);
			String json = response.readEntity(String.class);

			JsonObject jsonObject = Json.createReader(new StringReader(json)).readObject();
			
			assertEquals(unid, jsonObject.getString("unid"));

			assertEquals("I am nullDate guy!!", jsonObject.getString("title"));
			assertFalse(jsonObject.containsKey("dateGuy"), () -> "Unexpected JSON: " + json);
		}
		
		// Update to set non-null for the date value
		{
			JsonObject payloadJson = Json.createObjectBuilder()
				.add("title", "I am nullDate guy!!!")
				.add("dateGuy", "2023-09-15")
				.build();
			
			WebTarget target = client.target(getRestUrl(null, TestDatabase.MAIN) + "/exampleDocs/" + unid);
			Response response = target.request().put(Entity.json(payloadJson.toString()));
			checkResponse(200, response);
		}

		// Fetch it again
		{
			WebTarget target = client.target(getRestUrl(null, TestDatabase.MAIN) + "/exampleDocs/" + unid);
			Response response = target.request().get();
			checkResponse(200, response);
			String json = response.readEntity(String.class);

			JsonObject jsonObject = Json.createReader(new StringReader(json)).readObject();
			
			assertEquals(unid, jsonObject.getString("unid"));

			assertEquals("I am nullDate guy!!!", jsonObject.getString("title"));
			assertEquals("2023-09-15", jsonObject.getString("dateGuy"));
		}
	}
	
	@Test
	public void testInsertableUpdatable() {
		Client client = getAdminClient();
		// Create a new doc
		String unid;
		{
			JsonObject payloadJson = Json.createObjectBuilder()
				.add("title", "I am insertUpdate guy")
				.add("nonInsertable", "I should not be written during insert")
				.add("nonUpdatable", "I should be written during insert")
				.build();
			
			WebTarget postTarget = client.target(getRestUrl(null, TestDatabase.MAIN) + "/exampleDocs");
			Response response = postTarget.request().post(Entity.json(payloadJson.toString()));
			checkResponse(200, response);

			String json = response.readEntity(String.class);
			JsonObject jsonObject = Json.createReader(new StringReader(json)).readObject();
			unid = jsonObject.getString("unid");
			assertNotNull(unid);
			assertFalse(unid.isEmpty());
		}
		
		// Fetch the doc
		{
			WebTarget target = client.target(getRestUrl(null, TestDatabase.MAIN) + "/exampleDocs/" + unid);
			Response response = target.request().get();
			checkResponse(200, response);
			String json = response.readEntity(String.class);

			JsonObject jsonObject = Json.createReader(new StringReader(json)).readObject();
			
			assertEquals(unid, jsonObject.getString("unid"));
			
			assertEquals("I am insertUpdate guy", jsonObject.getString("title"));
			assertFalse(jsonObject.containsKey("nonInsertable"));
			assertEquals("I should be written during insert", jsonObject.getString("nonUpdatable"));
		}
		
		// Update to try to set the computed value
		{
			JsonObject payloadJson = Json.createObjectBuilder()
				.add("title", "I am insertUpdate guy!")
				.add("nonInsertable", "I should be written during update")
				.add("nonUpdatable", "I should not be written during update")
				.build();
			
			WebTarget target = client.target(getRestUrl(null, TestDatabase.MAIN) + "/exampleDocs/" + unid);
			Response response = target.request().put(Entity.json(payloadJson.toString()));
			checkResponse(200, response);
		}
		
		// Fetch it again
		{
			WebTarget target = client.target(getRestUrl(null, TestDatabase.MAIN) + "/exampleDocs/" + unid);
			Response response = target.request().get();
			checkResponse(200, response);
			String json = response.readEntity(String.class);

			JsonObject jsonObject = Json.createReader(new StringReader(json)).readObject();
			
			assertEquals(unid, jsonObject.getString("unid"));

			assertEquals("I am insertUpdate guy!", jsonObject.getString("title"));
			assertEquals("I should be written during update", jsonObject.getString("nonInsertable"));
			assertEquals("I should be written during insert", jsonObject.getString("nonUpdatable"));
		}
	}
	
	@Test
	public void testReadViewEntries() {
		Client client = getAnonymousClient();
		
		// Create a new doc
		String unid;
		{
			MultivaluedMap<String, String> payload = new MultivaluedHashMap<>();
			payload.putSingle("title", "foo");
			payload.put("categories", Arrays.asList("foo", "bar"));
			
			WebTarget postTarget = client.target(getRestUrl(null, TestDatabase.MAIN) + "/exampleDocs");
			Response response = postTarget.request().post(Entity.form(payload));
			checkResponse(200, response);

			String json = response.readEntity(String.class);
			JsonObject jsonObject = Json.createReader(new StringReader(json)).readObject();
			unid = jsonObject.getString("unid");
			assertNotNull(unid);
			assertFalse(unid.isEmpty());
		}
		
		// Make sure it shows up in the view entries
		{
			WebTarget target = client.target(getRestUrl(null, TestDatabase.MAIN) + "/exampleDocs/inView");
			Response response = target.request().get();
			checkResponse(200, response);
			
			String json = response.readEntity(String.class);
			JsonArray jsonObjects = Json.createReader(new StringReader(json)).readArray();
			assertNotNull(jsonObjects);
			assertFalse(jsonObjects.isEmpty());
			
			assertTrue(jsonObjects.stream().map(JsonValue::asJsonObject).anyMatch(obj -> unid.equals(obj.getString("unid")) && "DOCUMENT".equals(obj.getString("entryType"))));
			assertTrue(jsonObjects.stream().map(JsonValue::asJsonObject).anyMatch(obj -> "CATEGORY".equals(obj.getString("entryType"))));
		}
	}
	
	@Test
	public void testReadViewEntriesDocsOnly() {
		Client client = getAnonymousClient();
		
		// Create a new doc
		String unid;
		{
			MultivaluedMap<String, String> payload = new MultivaluedHashMap<>();
			payload.putSingle("title", "foo");
			payload.put("categories", Arrays.asList("foo", "bar"));
			
			WebTarget postTarget = client.target(getRestUrl(null, TestDatabase.MAIN) + "/exampleDocs");
			Response response = postTarget.request().post(Entity.form(payload));
			checkResponse(200, response);

			String json = response.readEntity(String.class);
			JsonObject jsonObject = Json.createReader(new StringReader(json)).readObject();
			unid = jsonObject.getString("unid");
			assertNotNull(unid);
			assertFalse(unid.isEmpty());
		}
		
		// Make sure it shows up in the view entries
		{
			WebTarget target = client.target(getRestUrl(null, TestDatabase.MAIN) + "/exampleDocs/inView?docsOnly=true");
			Response response = target.request().get();
			checkResponse(200, response);
			
			String json = response.readEntity(String.class);
			JsonArray jsonObjects = Json.createReader(new StringReader(json)).readArray();
			assertNotNull(jsonObjects);
			assertFalse(jsonObjects.isEmpty());
			
			assertTrue(jsonObjects.stream().map(JsonValue::asJsonObject).anyMatch(obj -> unid.equals(obj.getString("unid")) && "DOCUMENT".equals(obj.getString("entryType"))));
			assertFalse(jsonObjects.stream().map(JsonValue::asJsonObject).anyMatch(obj -> "CATEGORY".equals(obj.getString("entryType"))));
		}
	}

	@Test
	public void testReadViewEntriesMaxLevel() {
		Client client = getAnonymousClient();
		
		// Create a new doc
		String unid;
		{
			MultivaluedMap<String, String> payload = new MultivaluedHashMap<>();
			payload.putSingle("title", "foo");
			payload.put("categories", Arrays.asList("foo", "bar"));
			
			WebTarget postTarget = client.target(getRestUrl(null, TestDatabase.MAIN) + "/exampleDocs");
			Response response = postTarget.request().post(Entity.form(payload));
			checkResponse(200, response);

			String json = response.readEntity(String.class);
			JsonObject jsonObject = Json.createReader(new StringReader(json)).readObject();
			unid = jsonObject.getString("unid");
			assertNotNull(unid);
			assertFalse(unid.isEmpty());
		}
		
		// Make sure it shows up in the view entries
		{
			WebTarget target = client.target(getRestUrl(null, TestDatabase.MAIN) + "/exampleDocs/viewCategories");
			Response response = target.request().get();
			checkResponse(200, response);
			
			String json = response.readEntity(String.class);
			JsonArray jsonObjects = Json.createReader(new StringReader(json)).readArray();
			assertNotNull(jsonObjects);
			assertFalse(jsonObjects.isEmpty());
			
			assertTrue(jsonObjects.stream().map(JsonValue::asJsonObject).anyMatch(obj -> "CATEGORY".equals(obj.getString("entryType"))));
			assertFalse(jsonObjects.stream().map(JsonValue::asJsonObject).anyMatch(obj -> "DOCUMENT".equals(obj.getString("entryType"))));
		}
	}
	
	@Test
	public void testIntentionalRollBack() {
		Client client = getAnonymousClient();
		
		// Create a new doc
		String title;
		{
			MultivaluedMap<String, String> payload = new MultivaluedHashMap<>();
			title = "foo" + System.nanoTime();
			payload.putSingle("title", "foo");
			payload.put("categories", Arrays.asList("foo", "bar"));
			payload.putSingle("intentionallyRollBack", "true");
			
			WebTarget postTarget = client.target(getRestUrl(null, TestDatabase.MAIN) + "/exampleDocs");
			Response response = postTarget.request().post(Entity.form(payload));
			checkResponse(500, response);
			
			String content = response.readEntity(String.class);
			assertTrue(content.contains("I was asked to intentionally roll back"), () -> "Received unexpected content " + content);
		}
		
		// Make sure it doesn't show up in the view entries
		{
			WebTarget target = client.target(getRestUrl(null, TestDatabase.MAIN) + "/exampleDocs/inView");
			Response response = target.request().get();
			checkResponse(200, response);
			
			String json = response.readEntity(String.class);
			JsonArray jsonObjects = Json.createReader(new StringReader(json)).readArray();
			assertNotNull(jsonObjects);
			assertFalse(jsonObjects.isEmpty());
			
			assertFalse(jsonObjects.stream().map(JsonValue::asJsonObject).anyMatch(obj -> obj.containsKey("title") && title.equals(obj.getString("title"))));
		}
	}
	
	@Test
	public void testSequentialOperations() {
		Client client = getAnonymousClient();
		
		String unid;
		{
			WebTarget target = client.target(getRestUrl(null, TestDatabase.MAIN) + "/exampleDocs/exampleDocAndPersonTransaction");
			Response response = target.request().get();
			checkResponse(200, response);
			
			String json = response.readEntity(String.class);
			JsonObject result = Json.createReader(new StringReader(json)).readObject();
			assertNotNull(result);
			assertFalse(result.isEmpty());
			
			JsonObject exampleDoc = result.getJsonObject("exampleDoc");
			assertNotNull(exampleDoc);
			unid = exampleDoc.getString("unid");
			assertNotNull(unid);
			assertFalse(unid.isEmpty());
		}
		
		// Make sure it shows up in the view entries
		{
			WebTarget target = client.target(getRestUrl(null, TestDatabase.MAIN) + "/exampleDocs/inView?docsOnly=true");
			Response response = target.request().get();
			checkResponse(200, response);
			
			String json = response.readEntity(String.class);
			JsonArray jsonObjects = Json.createReader(new StringReader(json)).readArray();
			assertNotNull(jsonObjects);
			assertFalse(jsonObjects.isEmpty());
			
			assertTrue(jsonObjects.stream().map(JsonValue::asJsonObject).anyMatch(obj -> unid.equals(obj.getString("unid")) && "DOCUMENT".equals(obj.getString("entryType"))));
		}
	}
	
	@Test
	public void testNumberPrecision() {
		Client client = getAdminClient();
		
		// Create a new doc
		String unid;
		{
			JsonObject payloadJson = Json.createObjectBuilder()
				.add("title", "I am testNumberPrecision guy")
				.add("numberGuy", 3.111)
				.add("numbersGuy", Json.createArrayBuilder(Arrays.asList(4.111, 5.111)))
				.build();
			
			WebTarget postTarget = client.target(getRestUrl(null, TestDatabase.MAIN) + "/exampleDocs");
			Response response = postTarget.request().post(Entity.json(payloadJson.toString()));
			checkResponse(200, response);

			String json = response.readEntity(String.class);
			JsonObject jsonObject = Json.createReader(new StringReader(json)).readObject();
			unid = jsonObject.getString("unid");
			assertNotNull(unid);
			assertFalse(unid.isEmpty());
		}
		
		// Fetch the doc
		{
			WebTarget target = client.target(getRestUrl(null, TestDatabase.MAIN) + "/exampleDocs/" + unid);
			Response response = target.request().get();
			checkResponse(200, response);
			String json = response.readEntity(String.class);

			JsonObject jsonObject = Json.createReader(new StringReader(json)).readObject();
			
			assertEquals(unid, jsonObject.getString("unid"));
			
			assertEquals("I am testNumberPrecision guy", jsonObject.getString("title"));
			assertEquals(3.11, jsonObject.getJsonNumber("numberGuy").doubleValue());
			JsonArray numbersGuy = jsonObject.getJsonArray("numbersGuy");
			assertEquals(4.11, numbersGuy.getJsonNumber(0).doubleValue());
			assertEquals(5.11, numbersGuy.getJsonNumber(1).doubleValue());
		}
	}
	
	@ParameterizedTest
	@ValueSource(booleans = { true, false })
	public void testBooleanStorage(boolean expected) {
		Client client = getAdminClient();
		
		// Create a new doc
		String unid;
		{
			JsonObject payloadJson = Json.createObjectBuilder()
				.add("title", "I am testBooleanStorage guy")
				.add("booleanStorage", expected)
				.add("stringBooleanStorage", expected)
				.add("doubleBooleanStorage", expected)
				.build();
			
			WebTarget postTarget = client.target(getRestUrl(null, TestDatabase.MAIN) + "/exampleDocs");
			Response response = postTarget.request().post(Entity.json(payloadJson.toString()));
			checkResponse(200, response);

			String json = response.readEntity(String.class);
			JsonObject jsonObject = Json.createReader(new StringReader(json)).readObject();
			unid = jsonObject.getString("unid");
			assertNotNull(unid);
			assertFalse(unid.isEmpty());
			assertEquals(expected, jsonObject.getBoolean("booleanStorage"));
			assertEquals(expected, jsonObject.getBoolean("stringBooleanStorage"));
			assertEquals(expected, jsonObject.getBoolean("doubleBooleanStorage"));
		}
		
		// Fetch the doc
		{
			WebTarget target = client.target(getRestUrl(null, TestDatabase.MAIN) + "/exampleDocs/" + unid);
			Response response = target.request().get();
			checkResponse(200, response);
			String json = response.readEntity(String.class);

			JsonObject jsonObject = Json.createReader(new StringReader(json)).readObject();
			
			assertEquals(unid, jsonObject.getString("unid"));
			
			assertEquals("I am testBooleanStorage guy", jsonObject.getString("title"));
			
			String dxl = jsonObject.getString("dxl");
			org.w3c.dom.Document xmlDoc = TestDomUtil.createDocument(dxl);
			
			// Default storage
			assertEquals(expected, jsonObject.getBoolean("booleanStorage"), () -> "Failed round trip; dxl: " + jsonObject.getString("dxl"));
			String stored = TestDomUtil.nodes(xmlDoc, "//*[name()='item'][@name='BooleanStorage']/*/text()").get(0).getNodeValue();
			assertEquals(expected ? "Y" : "N", stored);
			
			// Stores as "true" and "false"
			assertEquals(expected, jsonObject.getBoolean("stringBooleanStorage"), () -> "Failed round trip; dxl: " + jsonObject.getString("dxl"));
			stored = TestDomUtil.nodes(xmlDoc, "//*[name()='item'][@name='StringBooleanStorage']/*/text()").get(0).getNodeValue();
			assertEquals(expected ? "true" : "false", stored);
			
			// Stores as 0 and 1 (intentionally reversed)
			assertEquals(expected, jsonObject.getBoolean("doubleBooleanStorage"), () -> "Failed round trip; dxl: " + jsonObject.getString("dxl"));
			stored = TestDomUtil.nodes(xmlDoc, "//*[name()='item'][@name='DoubleBooleanStorage']/*/text()").get(0).getNodeValue();
			assertEquals(expected ? "0" : "1", stored);
		}
	}
	
	/**
	 * Tests that a field marked with {@link JsonbTransient @JsonbTransient} is not included
	 * in the JSON output but is loaded by way of a special method included in the JSON.
	 * 
	 * @see <a href="https://github.com/OpenNTF/org.openntf.xsp.jakartaee/issues/513">Issue #513</a>
	 */
	@Test
	public void testJsonbTransientField() {
		Client client = getAdminClient();
		
		// Create a new doc
		String unid;
		{
			JsonObject payloadJson = Json.createObjectBuilder()
				.add("title", "I am testJsonbTransientField guy")
				.build();
			
			WebTarget postTarget = client.target(getRestUrl(null, TestDatabase.MAIN) + "/exampleDocs");
			Response response = postTarget.request().post(Entity.json(payloadJson.toString()));
			checkResponse(200, response);

			String json = response.readEntity(String.class);
			JsonObject jsonObject = Json.createReader(new StringReader(json)).readObject();
			unid = jsonObject.getString("unid");
			assertNotNull(unid);
			assertFalse(unid.isEmpty());
			assertFalse(jsonObject.containsKey("jsonTransientField"));
			assertFalse(jsonObject.containsKey("jsonTransientField2"));
		}
		
		// Fetch the doc
		{
			WebTarget target = client.target(getRestUrl(null, TestDatabase.MAIN) + "/exampleDocs/" + unid);
			Response response = target.request().get();
			checkResponse(200, response);
			String json = response.readEntity(String.class);

			JsonObject jsonObject = Json.createReader(new StringReader(json)).readObject();
			
			assertEquals(unid, jsonObject.getString("unid"));
			
			assertEquals("I am testJsonbTransientField guy", jsonObject.getString("title"));

			assertFalse(jsonObject.containsKey("jsonTransientField"));
			// Check the alternate way to fetch the field
			JsonArray expectedValues = Json.createArrayBuilder(Arrays.asList("i", "am", "the", "default", "value")).build();
			JsonArray alternateMethod = jsonObject.getJsonArray("alternateMethodStorage");
			assertIterableEquals(expectedValues, alternateMethod);
			
			// Same for the other field
			assertFalse(jsonObject.containsKey("jsonTransientField2"));
			// Check the alternate way to fetch the field
			expectedValues = Json.createArrayBuilder(Arrays.asList("default value")).build();
			alternateMethod = jsonObject.getJsonArray("alternateMethodStorage2");
			assertIterableEquals(expectedValues, alternateMethod);
			
		}
	}
	
	@Test
	public void testExampleDocInCategories() {
		Client client = getAnonymousClient();
		
		String prefix13 = "fooddddd";
		String title1 = prefix13 + System.currentTimeMillis();
		String title2 = "bar" + System.currentTimeMillis();
		String title3 = prefix13 + System.nanoTime();
		
		// Create two new docs
		final String unid1;
		{
			MultivaluedMap<String, String> payload = new MultivaluedHashMap<>();
			payload.putSingle("title", title1);
			
			WebTarget postTarget = client.target(getRestUrl(null, TestDatabase.MAIN) + "/exampleDocs");
			Response response = postTarget.request().post(Entity.form(payload));
			checkResponse(200, response);

			String json = response.readEntity(String.class);
			JsonObject jsonObject = Json.createReader(new StringReader(json)).readObject();
			unid1 = jsonObject.getString("unid");
			assertNotNull(unid1);
			assertFalse(unid1.isEmpty());
		}
		final String unid2;
		{
			MultivaluedMap<String, String> payload = new MultivaluedHashMap<>();
			payload.putSingle("title", title2);
			
			WebTarget postTarget = client.target(getRestUrl(null, TestDatabase.MAIN) + "/exampleDocs");
			Response response = postTarget.request().post(Entity.form(payload));
			checkResponse(200, response);

			String json = response.readEntity(String.class);
			JsonObject jsonObject = Json.createReader(new StringReader(json)).readObject();
			unid2 = jsonObject.getString("unid");
			assertNotNull(unid2);
			assertFalse(unid2.isEmpty());
		}
		final String unid3;
		{
			MultivaluedMap<String, String> payload = new MultivaluedHashMap<>();
			payload.putSingle("title", title3);
			
			WebTarget postTarget = client.target(getRestUrl(null, TestDatabase.MAIN) + "/exampleDocs");
			Response response = postTarget.request().post(Entity.form(payload));
			checkResponse(200, response);

			String json = response.readEntity(String.class);
			JsonObject jsonObject = Json.createReader(new StringReader(json)).readObject();
			unid3 = jsonObject.getString("unid");
			assertNotNull(unid3);
			assertFalse(unid3.isEmpty());
		}
		
		// Fetch the first two titles, which should include both
		{
			WebTarget target = client.target(getRestUrl(null, TestDatabase.MAIN) + "/exampleDocs/exampleDocsInTitle");
			Response response = target.queryParam("title", title1, title2).request().get();
			checkResponse(200, response);

			String json = response.readEntity(String.class);
			try {
				JsonArray entities = Json.createReader(new StringReader(json)).readArray();
				assertEquals(2, entities.size());
				assertTrue(entities.stream().map(JsonObject.class::cast).anyMatch(obj -> unid1.equals(obj.getString("unid"))));
				assertTrue(entities.stream().map(JsonObject.class::cast).anyMatch(obj -> unid2.equals(obj.getString("unid"))));
			} catch(Exception e) {
				fail("Encountered exception with JSON " + json, e);
			}
		}
		
		// Fetch the second two categories, which should also include both
		{
			WebTarget target = client.target(getRestUrl(null, TestDatabase.MAIN) + "/exampleDocs/exampleDocsInTitle");
			Response response = target.queryParam("title", title2, title3).request().get();
			checkResponse(200, response);

			String json = response.readEntity(String.class);
			try {
				JsonArray entities = Json.createReader(new StringReader(json)).readArray();
				assertEquals(2, entities.size());
				assertTrue(entities.stream().map(JsonObject.class::cast).anyMatch(obj -> unid2.equals(obj.getString("unid"))));
				assertTrue(entities.stream().map(JsonObject.class::cast).anyMatch(obj -> unid3.equals(obj.getString("unid"))));
			} catch(Exception e) {
				fail("Encountered exception with JSON " + json, e);
			}
		}
	}
	
	@Test
	public void testExampleDocInNumberGuy() {
		Client client = getAnonymousClient();
		
		String prefix13 = "fooddddd";
		String title = prefix13 + System.currentTimeMillis();
		Random rand = new SecureRandom();
		int[] guys = new int[] { rand.nextInt(), rand.nextInt(), rand.nextInt() };
		String[] unids = new String[3];
		
		// Create three new docs
		for(int i = 0; i < guys.length; i++) {
			JsonObject payload = Json.createObjectBuilder()
				.add("title", title)
				.add("numberGuy", guys[i])
				.build();
			
			WebTarget postTarget = client.target(getRestUrl(null, TestDatabase.MAIN) + "/exampleDocs");
			Response response = postTarget.request().post(Entity.json(payload));
			checkResponse(200, response);

			String json = response.readEntity(String.class);
			JsonObject jsonObject = Json.createReader(new StringReader(json)).readObject();
			unids[i] = jsonObject.getString("unid");
			assertNotNull(unids[i]);
			assertFalse(unids[i].isEmpty());
		}
		
		// Update the FT index, to make sure
		{
			WebTarget target = client.target(getRestUrl(null, TestDatabase.MAIN) + "/exampleDocs/updateExampleDocFtIndex");
			Response response = target.request().post(Entity.text(""));
			checkResponse(204, response);
		}
		
		// Fetch the first two values, which should include both
		{
			WebTarget target = client.target(getRestUrl(null, TestDatabase.MAIN) + "/exampleDocs/exampleDocsInNumberGuy");
			Response response = target.queryParam("numberGuy", guys[0], guys[1]).request().get();
			checkResponse(200, response);

			String json = response.readEntity(String.class);
			try {
				JsonArray entities = Json.createReader(new StringReader(json)).readArray();
				assertEquals(2, entities.size());
				assertTrue(entities.stream().map(JsonObject.class::cast).anyMatch(obj -> unids[0].equals(obj.getString("unid"))));
				assertTrue(entities.stream().map(JsonObject.class::cast).anyMatch(obj -> unids[1].equals(obj.getString("unid"))));
			} catch(Exception e) {
				fail("Encountered exception with JSON " + json, e);
			}
		}
		
		// Fetch the second two values
		{
			WebTarget target = client.target(getRestUrl(null, TestDatabase.MAIN) + "/exampleDocs/exampleDocsInNumberGuy");
			Response response = target.queryParam("numberGuy", guys[1], guys[2]).request().get();
			checkResponse(200, response);

			String json = response.readEntity(String.class);
			try {
				JsonArray entities = Json.createReader(new StringReader(json)).readArray();
				assertEquals(2, entities.size());
				assertTrue(entities.stream().map(JsonObject.class::cast).anyMatch(obj -> unids[1].equals(obj.getString("unid"))));
				assertTrue(entities.stream().map(JsonObject.class::cast).anyMatch(obj -> unids[2].equals(obj.getString("unid"))));
			} catch(Exception e) {
				fail("Encountered exception with JSON " + json, e);
			}
		}
	}
	
	@Test
	public void testExampleDocLikeTitle() {
		Client client = getAnonymousClient();
		
		final String prefix1 = "fooddddd";
		final String title1 = prefix1 + System.currentTimeMillis();
		final String prefix2 = "dsfsdf";
		final String title2 = prefix2 + System.currentTimeMillis();
		
		// Create two new docs
		final String unid1;
		{
			JsonObject payload = Json.createObjectBuilder()
				.add("title", title1)
				.build();
			
			WebTarget postTarget = client.target(getRestUrl(null, TestDatabase.MAIN) + "/exampleDocs");
			Response response = postTarget.request().post(Entity.json(payload));
			checkResponse(200, response);

			String json = response.readEntity(String.class);
			JsonObject jsonObject = Json.createReader(new StringReader(json)).readObject();
			unid1 = jsonObject.getString("unid");
			assertNotNull(unid1);
			assertFalse(unid1.isEmpty());
		}
		final String unid2;
		{
			JsonObject payload = Json.createObjectBuilder()
				.add("title", title2)
				.build();
			
			WebTarget postTarget = client.target(getRestUrl(null, TestDatabase.MAIN) + "/exampleDocs");
			Response response = postTarget.request().post(Entity.json(payload));
			checkResponse(200, response);

			String json = response.readEntity(String.class);
			JsonObject jsonObject = Json.createReader(new StringReader(json)).readObject();
			unid2 = jsonObject.getString("unid");
			assertNotNull(unid2);
			assertFalse(unid2.isEmpty());
		}
		
		// Update the FT index, to make sure
		{
			WebTarget target = client.target(getRestUrl(null, TestDatabase.MAIN) + "/exampleDocs/updateExampleDocFtIndex");
			Response response = target.request().post(Entity.text(""));
			checkResponse(204, response);
		}

		// Fetch the first prefix
		{
			WebTarget target = client.target(getRestUrl(null, TestDatabase.MAIN) + "/exampleDocs/exampleDocsLikeTitle");
			Response response = target.queryParam("title", prefix1 + "*").request().get();
			checkResponse(200, response);

			String json = response.readEntity(String.class);
			try {
				JsonArray entities = Json.createReader(new StringReader(json)).readArray();
				assertEquals(1, entities.size());
				assertTrue(entities.stream().map(JsonObject.class::cast).anyMatch(obj -> unid1.equals(obj.getString("unid"))));
			} catch(Exception e) {
				fail("Encountered exception with JSON " + json, e);
			}
		}

		// Fetch the second prefix
		{
			WebTarget target = client.target(getRestUrl(null, TestDatabase.MAIN) + "/exampleDocs/exampleDocsLikeTitle");
			Response response = target.queryParam("title", prefix2 + "*").request().get();
			checkResponse(200, response);

			String json = response.readEntity(String.class);
			try {
				JsonArray entities = Json.createReader(new StringReader(json)).readArray();
				assertEquals(1, entities.size());
				assertTrue(entities.stream().map(JsonObject.class::cast).anyMatch(obj -> unid2.equals(obj.getString("unid"))));
			} catch(Exception e) {
				fail("Encountered exception with JSON " + json, e);
			}
		}
	}
	
	@Test
	public void testExampleDocAllSorted() {
		Client client = getAnonymousClient();
		
		String prefix = "allsorted";
		String[] unids = new String[2];
		
		// Create two docs with the same title but different numberGuy to test sorting
		for(int i = 0; i < 2; i++) {
			JsonObject payload = Json.createObjectBuilder()
				.add("title", prefix)
				.add("numberGuy", 2-i)
				.build();
			
			WebTarget postTarget = client.target(getRestUrl(null, TestDatabase.MAIN) + "/exampleDocs");
			Response response = postTarget.request().post(Entity.json(payload));
			checkResponse(200, response);

			String json = response.readEntity(String.class);
			JsonObject jsonObject = Json.createReader(new StringReader(json)).readObject();
			unids[i] = jsonObject.getString("unid");
			assertNotNull(unids[i]);
			assertFalse(unids[i].isEmpty());
		}
		
		// Fetch the list and make sure that the second UNID is before the first
		
		// Fetch the first two values, which should include both
		{
			WebTarget target = client.target(getRestUrl(null, TestDatabase.MAIN) + "/exampleDocs/allSorted");
			Response response = target.request().get();
			checkResponse(200, response);

			String json = response.readEntity(String.class);
			try {
				JsonArray entities = Json.createReader(new StringReader(json)).readArray();
				assertTrue(entities.size() > 2, "Received unexpected count " + entities.size());
				
				int index1 = -1;
				int index2 = -1; 
				for(int i = 0; i < entities.size(); i++) {
					JsonObject entity = entities.getJsonObject(i);
					if(unids[0].equals(entity.getString("unid"))) {
						index1 = i;
					} else if(unids[1].equals(entity.getString("unid"))) {
						index2 = i;
					}
				}
				if(index1 == -1) {
					fail("Did not find first UNID");
				} else if(index2 == -1) {
					fail("Did not find second UNID");
				} else if(index1 <= index2) {
					fail("index1 should not be less than index2");
				}
				
			} catch(Exception e) {
				fail("Encountered exception with JSON " + json, e);
			}
		}
	}
	

	
	@Test
	public void testExampleDocAllSortedCustom() {
		Client client = getAnonymousClient();
		
		String prefix = "allsortedcustom";
		String[] unids = new String[2];
		
		// Create two docs with the same title but different numberGuy to test sorting
		for(int i = 0; i < 2; i++) {
			JsonObject payload = Json.createObjectBuilder()
				.add("title", prefix)
				.add("numberGuy", 2-i)
				.build();
			
			WebTarget postTarget = client.target(getRestUrl(null, TestDatabase.MAIN) + "/exampleDocs");
			Response response = postTarget.request().post(Entity.json(payload));
			checkResponse(200, response);

			String json = response.readEntity(String.class);
			JsonObject jsonObject = Json.createReader(new StringReader(json)).readObject();
			unids[i] = jsonObject.getString("unid");
			assertNotNull(unids[i]);
			assertFalse(unids[i].isEmpty());
		}
		
		// Fetch the list and make sure that the second UNID is before the first
		
		// Fetch the first two values, which should include both
		{
			WebTarget target = client.target(getRestUrl(null, TestDatabase.MAIN) + "/exampleDocs/allSortedCustom");
			Response response = target.request().get();
			checkResponse(200, response);

			String json = response.readEntity(String.class);
			try {
				JsonArray entities = Json.createReader(new StringReader(json)).readArray();
				assertTrue(entities.size() > 2, "Received unexpected count " + entities.size());
				
				int index1 = -1;
				int index2 = -1; 
				for(int i = 0; i < entities.size(); i++) {
					JsonObject entity = entities.getJsonObject(i);
					if(unids[0].equals(entity.getString("unid"))) {
						index1 = i;
					} else if(unids[1].equals(entity.getString("unid"))) {
						index2 = i;
					}
				}
				if(index1 == -1) {
					fail("Did not find first UNID");
				} else if(index2 == -1) {
					fail("Did not find second UNID");
				} else if(index1 <= index2) {
					fail("index1 should not be less than index2");
				}
				
			} catch(Exception e) {
				fail("Encountered exception with JSON " + json, e);
			}
		}
	}
}
