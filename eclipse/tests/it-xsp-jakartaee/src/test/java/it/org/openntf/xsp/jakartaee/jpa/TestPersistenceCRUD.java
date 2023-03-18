package it.org.openntf.xsp.jakartaee.jpa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.StringReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.testcontainers.containers.PostgreSQLContainer;

import it.org.openntf.xsp.jakartaee.AbstractWebClientTest;
import it.org.openntf.xsp.jakartaee.JakartaTestContainers;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;

@SuppressWarnings("nls")
public class TestPersistenceCRUD extends AbstractWebClientTest {
	
	@BeforeAll
	public static void createTable() throws SQLException {
		PostgreSQLContainer<?> container = JakartaTestContainers.instance.postgres;
		
		try(Connection conn = container.createConnection(""); Statement stmt = conn.createStatement()) {
			stmt.executeUpdate("CREATE TABLE IF NOT EXISTS public.companies (\n"
					+ "	id BIGSERIAL PRIMARY KEY,\n"
					+ "	name character varying(255) NOT NULL\n"
					+ ");");
		}
	}

	@ParameterizedTest
	@ArgumentsSource(AnonymousClientProvider.class)
	public void testPersistenceCrud(Client client) {
		WebTarget target = client.target(getJpaExampleRestUrl(null) + "/companies");
		
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
				JsonObject jsonObject = companies.getJsonObject(0);
				assertEquals(expected, jsonObject.getString("name"));
			} catch(Exception e) {
				fail("Encountered exception parsing " + json, e);
			}
		}
	}
}
