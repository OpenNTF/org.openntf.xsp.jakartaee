/**
 * Copyright (c) 2018-2024 Contributors to the XPages Jakarta EE Support Project
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

import org.junit.jupiter.api.Test;

import it.org.openntf.xsp.jakartaee.AbstractWebClientTest;
import it.org.openntf.xsp.jakartaee.TestDatabase;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;

@SuppressWarnings("nls")
public class TestNoSQLRecords extends AbstractWebClientTest {
	@Test
	public void testRecordDoc() throws UnsupportedEncodingException {
		Client client = getAnonymousClient();
		
		// Create a new doc
		String unid;
		String name;
		{
			name = "Some Key " + System.currentTimeMillis();
			JsonObject payload = Json.createObjectBuilder()
				.add("name", name)
				.add("index", 4)
				.build();
			
			WebTarget postTarget = client.target(getRestUrl(null, TestDatabase.MAIN) + "/nosqlRecordDocs");
			Response response = postTarget.request().post(Entity.json(payload));
			checkResponse(200, response);

			String json = response.readEntity(String.class);
			JsonObject jsonObject = Json.createReader(new StringReader(json)).readObject();
			unid = jsonObject.getString("unid", null);
			assertNotNull(unid);
			assertFalse(unid.isEmpty());
			assertEquals(name, jsonObject.getString("name", null));
			assertEquals(4, jsonObject.getInt("index", 0));
		}
		
		// Fetch the doc
		{
			WebTarget target = client.target(getRestUrl(null, TestDatabase.MAIN) + "/nosqlRecordDocs/byName/" + URLEncoder.encode(name, "UTF-8"));
			Response response = target.request().get();
			checkResponse(200, response);
			String json = response.readEntity(String.class);

			JsonObject jsonObject = Json.createReader(new StringReader(json)).readObject();
			
			assertEquals(unid, jsonObject.getString("unid"));
			assertEquals(name, jsonObject.getString("name", null));
			assertEquals(4, jsonObject.getInt("index", 0));
		}
	}
	
	// Tests to make sure that default values from docs with primitives work
	@Test
	public void testPartialRecordDoc() throws UnsupportedEncodingException {
		Client client = getAnonymousClient();
		
		// Create a new doc
		String name;
		{
			JsonObject payload = Json.createObjectBuilder().build();
			
			WebTarget postTarget = client.target(getRestUrl(null, TestDatabase.MAIN) + "/nosqlRecordDocs/createPartial");
			Response response = postTarget.request().post(Entity.json(payload));
			checkResponse(200, response);

			String json = response.readEntity(String.class);
			JsonObject jsonObject = Json.createReader(new StringReader(json)).readObject();
			name = jsonObject.getString("name", null);
			assertNotNull(name);
			assertFalse(name.isEmpty());
		}
		
		// Fetch the doc
		{
			WebTarget target = client.target(getRestUrl(null, TestDatabase.MAIN) + "/nosqlRecordDocs/byName/" + URLEncoder.encode(name, "UTF-8"));
			Response response = target.request().get();
			checkResponse(200, response);
			String json = response.readEntity(String.class);

			JsonObject jsonObject = Json.createReader(new StringReader(json)).readObject();
			
			assertEquals(name, jsonObject.getString("name", null));
			assertEquals(0, jsonObject.getInt("index", -1));
		}
	}
}
