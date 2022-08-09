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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataOutput;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.ibm.commons.util.io.json.JsonException;
import com.ibm.commons.util.io.json.JsonJavaFactory;
import com.ibm.commons.util.io.json.JsonParser;

import it.org.openntf.xsp.jakartaee.AbstractWebClientTest;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@SuppressWarnings("nls")
public class TestNoSQLViews extends AbstractWebClientTest {
	@SuppressWarnings({ "unchecked" })
	@Test
	public void testQueryByKey() throws JsonException, UnsupportedEncodingException {
		Client client = getAdminClient();
		
		Map<String, Object> person = createTwoPersonDocuments(false);
		
		// Find by the last name of the second person
		String lastName = (String)person.get("lastName");
		assertNotNull(lastName);
		WebTarget queryTarget = client.target(getRestUrl(null) + "/nosql/byViewKey/" + URLEncoder.encode(lastName, "UTF-8"));
		
		Response response = queryTarget.request()
			.accept(MediaType.APPLICATION_JSON_TYPE)
			.get();
		String json = response.readEntity(String.class);
		assertEquals(200, response.getStatus(), () -> "Received unexpected result: " + json);

		Map<String, Object> result = (Map<String, Object>)JsonParser.fromJson(JsonJavaFactory.instance, json);
		assertEquals(person.get("unid"), result.get("unid"));
		assertEquals(person.get("lastName"), result.get("lastName"));
	}
	
	@SuppressWarnings({ "unchecked" })
	@ParameterizedTest
	@ValueSource(strings = { "byViewTwoKeys", "byViewCollectionKey" })
	public void testQueryByTwoKeys(String pathPart) throws JsonException, UnsupportedEncodingException {
		Client client = getAdminClient();
		
		Map<String, Object> person = createTwoPersonDocuments(true);
		
		// Find by the last name of the second person
		String lastName = (String)person.get("lastName");
		assertNotNull(lastName);
		String firstName = (String)person.get("firstName");
		assertNotNull(firstName);
		WebTarget queryTarget = client.target(getRestUrl(null) + "/nosql/" + pathPart
			+ "/" + URLEncoder.encode(lastName, "UTF-8")
			+ "/"
			+ URLEncoder.encode(firstName, "UTF-8")
		);
		
		Response response = queryTarget.request()
			.accept(MediaType.APPLICATION_JSON_TYPE)
			.get();
		String json = response.readEntity(String.class);
		assertEquals(200, response.getStatus(), () -> "Received unexpected result: " + json);

		Map<String, Object> result = (Map<String, Object>)JsonParser.fromJson(JsonJavaFactory.instance, json);
		assertEquals(person.get("unid"), result.get("unid"));
		assertEquals(person.get("lastName"), result.get("lastName"));
	}
	
	@SuppressWarnings({ "unchecked" })
	@Test
	public void testQueryByKeyMulti() throws JsonException, UnsupportedEncodingException {
		Client client = getAdminClient();
		
		// Create four documents with two distinct last names
		createTwoPersonDocuments(true);
		Map<String, Object> person = createTwoPersonDocuments(true);
		
		// Find by the last name of the second person
		String lastName = (String)person.get("lastName");
		assertNotNull(lastName);
		WebTarget queryTarget = client.target(getRestUrl(null) + "/nosql/byViewKeyMulti/" + URLEncoder.encode(lastName, "UTF-8"));
		
		Response response = queryTarget.request()
			.accept(MediaType.APPLICATION_JSON_TYPE)
			.get();
		String json = response.readEntity(String.class);
		assertEquals(200, response.getStatus(), () -> "Received unexpected result: " + json);

		List<Map<String, Object>> result = (List<Map<String, Object>>)JsonParser.fromJson(JsonJavaFactory.instance, json);
		assertEquals(2, result.size());
		Map<String, Object> resultPerson = result.get(1);
		assertEquals(person.get("unid"), resultPerson.get("unid"));
		assertEquals(person.get("lastName"), resultPerson.get("lastName"));
	}
	
	/**
	 * Creates two person documents, optionally using the same auto-generated last name
	 * for both documents.
	 * 
	 * @param retainLastName whether both docs should be created with the same last name
	 * @return the second document created
	 * @throws JsonException if there is a problem parsing the result
	 */
	@SuppressWarnings("unchecked")
	private Map<String, Object> createTwoPersonDocuments(boolean retainLastName) throws JsonException {
		Client client = getAdminClient();
		
		// Create two documents to ensure that we can query by the second
		String lastName = null;
		Map<String, Object> person = null;
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

			person = (Map<String, Object>)JsonParser.fromJson(JsonJavaFactory.instance, json);
			String unid = (String)person.get("unid");
			assertNotNull(unid);
			assertFalse(unid.isEmpty());
		}
		
		return person;
	}
}
