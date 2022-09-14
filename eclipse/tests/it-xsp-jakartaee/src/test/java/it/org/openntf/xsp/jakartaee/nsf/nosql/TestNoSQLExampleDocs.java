/**
 * Copyright © 2018-2022 Contributors to the XPages Jakarta EE Support Project
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
import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.w3c.dom.Element;

import com.ibm.commons.xml.DOMUtil;
import com.ibm.commons.xml.XMLException;

import it.org.openntf.xsp.jakartaee.AbstractWebClientTest;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;

@SuppressWarnings("nls")
public class TestNoSQLExampleDocs extends AbstractWebClientTest {
	@Test
	public void testExampleDoc() throws XMLException {
		Client client = getAnonymousClient();
		
		// Create a new doc
		String unid;
		{
			MultivaluedMap<String, String> payload = new MultivaluedHashMap<>();
			payload.putSingle("title", "foo");
			payload.put("categories", Arrays.asList("foo", "bar"));
			
			WebTarget postTarget = client.target(getRestUrl(null) + "/exampleDocs");
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
			WebTarget target = client.target(getRestUrl(null) + "/exampleDocs/" + unid);
			Response response = target.request().get();
			checkResponse(200, response);
			String json = response.readEntity(String.class);

			JsonObject jsonObject = Json.createReader(new StringReader(json)).readObject();
			
			assertEquals(unid, jsonObject.getString("unid"));
			
			String dxl = jsonObject.getString("dxl");
			assertNotNull(dxl);
			assertFalse(dxl.isEmpty());
			
			org.w3c.dom.Document xmlDoc = DOMUtil.createDocument(dxl);
			assertNotNull(xmlDoc);
			String title = DOMUtil.evaluateXPath(xmlDoc, "//*[name()='item'][@name='$$Title']/*[name()='text']/text()").getStringValue();
			assertEquals("foo", title);
		}
	}
	
	@Test
	public void testExampleDocAuthors() throws XMLException {
		Client client = getAnonymousClient();
		
		// Create a new doc
		String unid;
		{
			MultivaluedMap<String, String> payload = new MultivaluedHashMap<>();
			payload.putSingle("title", "foo");
			payload.put("categories", Arrays.asList("foo", "bar"));
			payload.put("authors", Arrays.asList("CN=foo fooson/O=Bar"));
			
			WebTarget postTarget = client.target(getRestUrl(null) + "/exampleDocs");
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
			WebTarget target = client.target(getRestUrl(null) + "/exampleDocs/" + unid);
			Response response = target.request().get();
			checkResponse(200, response);
			String json = response.readEntity(String.class);

			JsonObject jsonObject = Json.createReader(new StringReader(json)).readObject();
			
			assertEquals(unid, jsonObject.getString("unid"));
			
			String dxl = jsonObject.getString("dxl");
			assertNotNull(dxl);
			assertFalse(dxl.isEmpty());
			
			org.w3c.dom.Document xmlDoc = DOMUtil.createDocument(dxl);
			assertNotNull(xmlDoc);
			Element authors = (Element)DOMUtil.evaluateXPath(xmlDoc, "//*[name()='item'][@name='Authors']").getSingleNode();
			assertNotNull(authors);
			assertEquals("true", authors.getAttribute("authors"));
		}
	}

	@Test
	public void testComputeWithForm() throws XMLException {
		Client client = getAnonymousClient();
		
		// Create a new doc
		String unid;
		{
			MultivaluedMap<String, String> payload = new MultivaluedHashMap<>();
			payload.putSingle("title", "foo");
			payload.put("categories", Arrays.asList("foo", "bar"));
			
			WebTarget postTarget = client.target(getRestUrl(null) + "/exampleDocs");
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
			WebTarget target = client.target(getRestUrl(null) + "/exampleDocs/" + unid);
			Response response = target.request().get();
			checkResponse(200, response);
			String json = response.readEntity(String.class);

			JsonObject jsonObject = Json.createReader(new StringReader(json)).readObject();
			
			assertEquals(unid, jsonObject.getString("unid"));
			
			String dxl = jsonObject.getString("dxl");
			assertNotNull(dxl);
			assertFalse(dxl.isEmpty());
			
			org.w3c.dom.Document xmlDoc = DOMUtil.createDocument(dxl);
			assertNotNull(xmlDoc);
			String val = DOMUtil.evaluateXPath(xmlDoc, "//*[name()='item'][@name='DefaultValue']/*[name()='text']/text()").getStringValue();
			assertNotNull(val);
			assertEquals("I am the default value", val);
		}
	}
	
	@Test
	public void testItemStorage() throws XMLException {
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
			
			WebTarget postTarget = client.target(getRestUrl(null) + "/exampleDocs");
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
			WebTarget target = client.target(getRestUrl(null) + "/exampleDocs/" + unid);
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
	public void testSaveToDisk() throws XMLException {
		Client client = getAdminClient();
		// Create a new doc
		String unid;
		{
			JsonObject payloadJson = Json.createObjectBuilder()
				.add("title", "I am saveToDisk guy")
				.add("computedValue", "I am written by the test")
				.build();
			
			WebTarget postTarget = client.target(getRestUrl(null) + "/exampleDocs");
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
			WebTarget target = client.target(getRestUrl(null) + "/exampleDocs/" + unid);
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
			
			WebTarget target = client.target(getRestUrl(null) + "/exampleDocs/" + unid);
			Response response = target.request().put(Entity.json(payloadJson.toString()));
			checkResponse(200, response);
		}
		
		// Fetch it again
		{
			WebTarget target = client.target(getRestUrl(null) + "/exampleDocs/" + unid);
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
	public void testInsertableUpdatable() throws XMLException {
		Client client = getAdminClient();
		// Create a new doc
		String unid;
		{
			JsonObject payloadJson = Json.createObjectBuilder()
				.add("title", "I am insertUpdate guy")
				.add("nonInsertable", "I should not be written during insert")
				.add("nonUpdatable", "I should be written during insert")
				.build();
			
			WebTarget postTarget = client.target(getRestUrl(null) + "/exampleDocs");
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
			WebTarget target = client.target(getRestUrl(null) + "/exampleDocs/" + unid);
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
			
			WebTarget target = client.target(getRestUrl(null) + "/exampleDocs/" + unid);
			Response response = target.request().put(Entity.json(payloadJson.toString()));
			checkResponse(200, response);
		}
		
		// Fetch it again
		{
			WebTarget target = client.target(getRestUrl(null) + "/exampleDocs/" + unid);
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
			
			WebTarget postTarget = client.target(getRestUrl(null) + "/exampleDocs");
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
			WebTarget target = client.target(getRestUrl(null) + "/exampleDocs/inView");
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
			
			WebTarget postTarget = client.target(getRestUrl(null) + "/exampleDocs");
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
			WebTarget target = client.target(getRestUrl(null) + "/exampleDocs/inView?docsOnly=true");
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
			
			WebTarget postTarget = client.target(getRestUrl(null) + "/exampleDocs");
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
			WebTarget target = client.target(getRestUrl(null) + "/exampleDocs/viewCategories");
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
			
			WebTarget postTarget = client.target(getRestUrl(null) + "/exampleDocs");
			Response response = postTarget.request().post(Entity.form(payload));
			checkResponse(500, response);
			
			String content = response.readEntity(String.class);
			assertTrue(content.contains("I was asked to intentionally roll back"), () -> "Received unexpected content " + content);
		}
		
		// Make sure it doesn't show up in the view entries
		{
			WebTarget target = client.target(getRestUrl(null) + "/exampleDocs/inView");
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
			WebTarget target = client.target(getRestUrl(null) + "/exampleDocs/exampleDocAndPersonTransaction");
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
			WebTarget target = client.target(getRestUrl(null) + "/exampleDocs/inView?docsOnly=true");
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
	public void testNumberPrecision() throws XMLException {
		Client client = getAdminClient();
		
		// Create a new doc
		String unid;
		{
			JsonObject payloadJson = Json.createObjectBuilder()
				.add("title", "I am testNumberPrecision guy")
				.add("numberGuy", 3.111)
				.add("numbersGuy", Json.createArrayBuilder(Arrays.asList(4.111, 5.111)))
				.build();
			
			WebTarget postTarget = client.target(getRestUrl(null) + "/exampleDocs");
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
			WebTarget target = client.target(getRestUrl(null) + "/exampleDocs/" + unid);
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
}
