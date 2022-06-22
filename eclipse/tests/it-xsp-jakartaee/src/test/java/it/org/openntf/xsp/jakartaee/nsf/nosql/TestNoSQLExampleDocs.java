package it.org.openntf.xsp.jakartaee.nsf.nosql;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Arrays;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.w3c.dom.Element;

import com.ibm.commons.util.io.json.JsonException;
import com.ibm.commons.util.io.json.JsonJavaFactory;
import com.ibm.commons.util.io.json.JsonParser;
import com.ibm.commons.xml.DOMUtil;
import com.ibm.commons.xml.XMLException;

import it.org.openntf.xsp.jakartaee.AbstractWebClientTest;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;

@SuppressWarnings("nls")
public class TestNoSQLExampleDocs extends AbstractWebClientTest {
	@SuppressWarnings({ "unchecked" })
	@Test
	public void testExampleDoc() throws JsonException, XMLException {
		Client client = getAnonymousClient();
		
		// Create a new doc
		String unid;
		{
			MultivaluedMap<String, String> payload = new MultivaluedHashMap<>();
			payload.putSingle("title", "foo");
			payload.put("categories", Arrays.asList("foo", "bar"));
			
			WebTarget postTarget = client.target(getRestUrl(null) + "/exampleDocs");
			Response response = postTarget.request().post(Entity.form(payload));
			assertEquals(200, response.getStatus());

			String json = response.readEntity(String.class);
			Map<String, Object> jsonObject = (Map<String, Object>)JsonParser.fromJson(JsonJavaFactory.instance, json);
			unid = (String)jsonObject.get("unid");
			assertNotNull(unid);
			assertFalse(unid.isEmpty());
		}
		
		// Fetch the doc
		{
			WebTarget target = client.target(getRestUrl(null) + "/exampleDocs/" + unid);
			Response response = target.request().get();
			String json = response.readEntity(String.class);
			assertEquals(200, response.getStatus(), () -> "Received unexpected code " + response.getStatus() + ": " + json);

			Map<String, Object> jsonObject = (Map<String, Object>)JsonParser.fromJson(JsonJavaFactory.instance, json);
			
			assertEquals(unid, jsonObject.get("unid"));
			
			String dxl = (String)jsonObject.get("dxl");
			assertNotNull(dxl);
			assertFalse(dxl.isEmpty());
			
			org.w3c.dom.Document xmlDoc = DOMUtil.createDocument(dxl);
			assertNotNull(xmlDoc);
			String title = DOMUtil.evaluateXPath(xmlDoc, "//*[name()='item'][@name='$$Title']/*[name()='text']/text()").getStringValue();
			assertEquals("foo", title);
		}
	}
	@SuppressWarnings({ "unchecked" })
	@Test
	public void testExampleDocAuthors() throws JsonException, XMLException {
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
			assertEquals(200, response.getStatus());

			String json = response.readEntity(String.class);
			Map<String, Object> jsonObject = (Map<String, Object>)JsonParser.fromJson(JsonJavaFactory.instance, json);
			unid = (String)jsonObject.get("unid");
			assertNotNull(unid);
			assertFalse(unid.isEmpty());
		}
		
		// Fetch the doc
		{
			WebTarget target = client.target(getRestUrl(null) + "/exampleDocs/" + unid);
			Response response = target.request().get();
			String json = response.readEntity(String.class);
			assertEquals(200, response.getStatus(), () -> "Received unexpected code " + response.getStatus() + ": " + json);

			Map<String, Object> jsonObject = (Map<String, Object>)JsonParser.fromJson(JsonJavaFactory.instance, json);
			
			assertEquals(unid, jsonObject.get("unid"));
			
			String dxl = (String)jsonObject.get("dxl");
			assertNotNull(dxl);
			assertFalse(dxl.isEmpty());
			
			org.w3c.dom.Document xmlDoc = DOMUtil.createDocument(dxl);
			assertNotNull(xmlDoc);
			Element authors = (Element)DOMUtil.evaluateXPath(xmlDoc, "//*[name()='item'][@name='Authors']").getSingleNode();
			assertNotNull(authors);
			assertEquals("true", authors.getAttribute("authors"));
		}
	}
}
