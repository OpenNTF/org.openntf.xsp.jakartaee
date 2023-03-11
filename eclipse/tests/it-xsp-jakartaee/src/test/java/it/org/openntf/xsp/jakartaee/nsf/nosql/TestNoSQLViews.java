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
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataOutput;
import org.junit.jupiter.api.Test;

import it.org.openntf.xsp.jakartaee.AbstractWebClientTest;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
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
	 * Creates two person documents, optionally using the same auto-generated last name
	 * for both documents.
	 * 
	 * @param retainLastName whether both docs should be created with the same last name
	 * @return the second document created
	 * @throws JsonException if there is a problem parsing the result
	 */
	private JsonObject createTwoPersonDocuments(boolean retainLastName){
		Client client = getAdminClient();
		
		// Create two documents to ensure that we can query by the second
		String lastName = null;
		JsonObject person = null;
		for(int i = 0; i < 2; i++) {
			WebTarget postTarget = client.target(getRestUrl(null) + "/nosql/create"); //$NON-NLS-1$

			if(lastName == null || !retainLastName) {
				lastName = "Fooson" + System.nanoTime();
			}
			MultipartFormDataOutput payload = new MultipartFormDataOutput();
			payload.addFormData("firstName", "Foo" + System.nanoTime(), MediaType.TEXT_PLAIN_TYPE);
			payload.addFormData("lastName", lastName, MediaType.TEXT_PLAIN_TYPE);
			
			Response response = postTarget.request()
				.accept(MediaType.APPLICATION_JSON_TYPE)
				.post(Entity.entity(payload, MediaType.MULTIPART_FORM_DATA_TYPE));
			String json = response.readEntity(String.class);
			assertEquals(200, response.getStatus(), () -> "Received unexpected result: " + json);

			person = Json.createReader(new StringReader(json)).readObject();
			String unid = person.getString("unid");
			assertNotNull(unid);
			assertFalse(unid.isEmpty());
		}
		
		return person;
	}
}
