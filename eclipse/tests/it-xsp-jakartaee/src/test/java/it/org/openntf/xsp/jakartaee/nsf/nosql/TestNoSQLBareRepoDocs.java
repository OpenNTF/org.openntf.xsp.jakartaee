/*
 * Copyright (c) 2018-2026 Contributors to the XPages Jakarta EE Support Project
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
import java.util.regex.Pattern;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import it.org.openntf.xsp.jakartaee.AbstractWebClientTest;
import it.org.openntf.xsp.jakartaee.TestDatabase;
import it.org.openntf.xsp.jakartaee.providers.MainAndModuleProvider;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;

@SuppressWarnings("nls")
public class TestNoSQLBareRepoDocs extends AbstractWebClientTest {
	public static final Pattern ETAG_PATTERN = Pattern.compile("^[\\d\\w]{32}$");
	
	@ParameterizedTest
	@ArgumentsSource(MainAndModuleProvider.EnumOnly.class)
	public void testExampleDoc(TestDatabase db) {
		Client client = getAdminClient();
		
		// Create a new doc
		String unid;
		String title = "foo" + System.currentTimeMillis();
		{
			var payload = Json.createObjectBuilder().add("title", title).build();
			WebTarget postTarget = client.target(getRestUrl(null, db) + "/bareRepoDocs");
			Response response = postTarget.request().post(Entity.json(payload));
			checkResponse(200, response);

			String json = response.readEntity(String.class);
			JsonObject jsonObject = Json.createReader(new StringReader(json)).readObject();
			unid = jsonObject.getString("unid");
			assertNotNull(unid);
			assertFalse(unid.isEmpty());
		}
		
		// Fetch the doc
		{
			WebTarget target = client.target(getRestUrl(null, db) + "/bareRepoDocs/" + unid);
			Response response = target.request().get();
			checkResponse(200, response);
			String json = response.readEntity(String.class);

			JsonObject jsonObject = Json.createReader(new StringReader(json)).readObject();
			
			assertEquals(unid, jsonObject.getString("unid"));
			assertEquals(title, jsonObject.getString("title"));
		}
		
		// Update it
		title = "Foo2" + System.currentTimeMillis();
		{
			var payload = Json.createObjectBuilder().add("title", title).build();
			WebTarget postTarget = client.target(getRestUrl(null, db) + "/bareRepoDocs/" + unid);
			Response response = postTarget.request().put(Entity.json(payload));
			checkResponse(200, response);

			String json = response.readEntity(String.class);
			JsonObject jsonObject = Json.createReader(new StringReader(json)).readObject();
			unid = jsonObject.getString("unid");
			assertNotNull(unid);
			assertFalse(unid.isEmpty());
		}
		
		// Fetch the changed doc
		{
			WebTarget target = client.target(getRestUrl(null, db) + "/bareRepoDocs/" + unid);
			Response response = target.request().get();
			checkResponse(200, response);
			String json = response.readEntity(String.class);

			JsonObject jsonObject = Json.createReader(new StringReader(json)).readObject();
			
			assertEquals(unid, jsonObject.getString("unid"));
			assertEquals(title, jsonObject.getString("title"));
		}
	}
}
