package it.org.openntf.xsp.jakartaee.nsf.nosql;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;

import it.org.openntf.xsp.jakartaee.AbstractWebClientTest;
import it.org.openntf.xsp.jakartaee.TestDatabase;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;

@SuppressWarnings("nls")
public class TestCustomPropertyTypeList extends AbstractWebClientTest {
	@Test
	public void testCustomPropertyList() {
		Client client = getAdminClient();
		WebTarget target = client.target(getRestUrl(null, TestDatabase.MAIN) + "/nosql/customPropertyList"); //$NON-NLS-1$
		
		JsonArray expected = Json.createArrayBuilder()
			.add(Json.createObjectBuilder().add("value", "foo"))
			.add(Json.createObjectBuilder().add("value", "bar"))
			.build();
		
		String id;
		{
			JsonObject payload = Json.createObjectBuilder()
				.add("customPropertyList", expected)
				.build();
			
			Response response = target.request().post(Entity.json(payload));
			JsonObject responseJson = response.readEntity(JsonObject.class);
			try {
				id = responseJson.getString("id");
				JsonArray actual = responseJson.getJsonArray("customPropertyList");
				assertEquals(expected, actual);
			} catch(Exception e) {
				fail("Encountered exception with JSON " + responseJson, e);
				throw e;
			}
		}
		
		{
			WebTarget getTarget = client.target(getRestUrl(null, TestDatabase.MAIN) + "/nosql/customPropertyList/" + id);
			Response response = getTarget.request().get();
			
			JsonObject responseJson = response.readEntity(JsonObject.class);
			try {
				JsonArray actual = responseJson.getJsonArray("customPropertyList");
				assertEquals(expected, actual, () -> "Failed with JSON " + responseJson);
			} catch(Exception e) {
				fail("Encountered exception with JSON " + responseJson, e);
			}
		}
	}
	
	@Test
	public void testJsonArrayStorage() {
		Client client = getAdminClient();
		WebTarget target = client.target(getRestUrl(null, TestDatabase.MAIN) + "/nosql/customPropertyList"); //$NON-NLS-1$
		
		JsonArray expected = Json.createArrayBuilder()
			.add(Json.createObjectBuilder().add("value", "foo"))
			.add(Json.createObjectBuilder().add("value", "bar"))
			.add("baz")
			.build();
		
		String id;
		{
			JsonObject payload = Json.createObjectBuilder()
				.add("jsonArrayStorage", expected)
				.build();
			
			Response response = target.request().post(Entity.json(payload));
			JsonObject responseJson = response.readEntity(JsonObject.class);
			try {
				id = responseJson.getString("id");
				JsonArray actual = responseJson.getJsonArray("jsonArrayStorage");
				assertEquals(expected, actual);
			} catch(Exception e) {
				fail("Encountered exception with JSON " + responseJson, e);
				throw e;
			}
		}
		
		{
			WebTarget getTarget = client.target(getRestUrl(null, TestDatabase.MAIN) + "/nosql/customPropertyList/" + id);
			Response response = getTarget.request().get();
			
			JsonObject responseJson = response.readEntity(JsonObject.class);
			try {
				JsonArray actual = responseJson.getJsonArray("jsonArrayStorage");
				assertEquals(expected, actual, () -> "Failed with JSON " + responseJson);
			} catch(Exception e) {
				fail("Encountered exception with JSON " + responseJson, e);
			}
		}
	}
}
