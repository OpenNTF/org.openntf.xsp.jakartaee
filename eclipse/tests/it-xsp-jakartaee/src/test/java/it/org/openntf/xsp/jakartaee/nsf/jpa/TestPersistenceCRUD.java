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
package it.org.openntf.xsp.jakartaee.nsf.jpa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.StringReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.testcontainers.postgresql.PostgreSQLContainer;

import it.org.openntf.xsp.jakartaee.AbstractWebClientTest;
import it.org.openntf.xsp.jakartaee.JakartaTestContainers;
import it.org.openntf.xsp.jakartaee.TestDatabase;
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
public class TestPersistenceCRUD extends AbstractWebClientTest {
	
	public static class JpaAndClientProvider implements ArgumentsProvider {
		@Override
		public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
			var client = new AbstractWebClientTest.AnonymousClientProvider();
			return Stream.of(TestDatabase.JPA, TestDatabase.JPA_MODULE)
				.flatMap(e ->
					client.provideArguments(context)
						.map(arg -> arg.get()[0])
						.map(browser -> Arguments.of(e, browser))
				);
		}
	}
	
	@BeforeAll
	public static void createTable() throws SQLException {
		PostgreSQLContainer container = JakartaTestContainers.instance.postgres;
		
		try(Connection conn = container.createConnection(""); Statement stmt = conn.createStatement()) {
			stmt.executeUpdate("CREATE TABLE IF NOT EXISTS public.companies (\n"
					+ "	id BIGSERIAL PRIMARY KEY,\n"
					+ "	name character varying(255) NOT NULL\n"
					+ ");");
		}
	}

	@ParameterizedTest
	@ArgumentsSource(JpaAndClientProvider.class)
	public void testPersistenceCrud(TestDatabase db, Client client) {
		WebTarget target = client.target(getRestUrl(null, db) + "/companies");
		
		String expected = "Test Company" + System.currentTimeMillis();
		// Create a new record
		{
			MultivaluedMap<String, String> payload = new MultivaluedHashMap<>();
			payload.add("name", expected);
			Response response = target.request().post(Entity.form(payload));
			String json = response.readEntity(String.class);
			try {
				JsonObject jsonObject = Json.createReader(new StringReader(json)).readObject();
				assertEquals(expected, jsonObject.getString("name"));
			} catch(Exception e) {
				fail("Encountered exception parsing " + json, e);
			}
		}
		
		{
			Response response = target.request().get();
			
			String json = response.readEntity(String.class);
			try {
				JsonArray companies = Json.createReader(new StringReader(json)).readArray();
				assertTrue(companies.stream()
					.map(JsonValue::asJsonObject)
					.anyMatch(o -> expected.equals(o.getString("name"))), () -> json);
			} catch(Exception e) {
				fail("Encountered exception parsing " + json, e);
			}
		}
	}
}
