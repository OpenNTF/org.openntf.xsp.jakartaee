/**
 * Copyright © 2018-2022 Jesse Gallagher
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
package it.org.openntf.xsp.jakartaee.nsf.json;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.junit.jupiter.api.Test;

import com.ibm.commons.util.io.json.JsonException;
import com.ibm.commons.util.io.json.JsonJavaFactory;
import com.ibm.commons.util.io.json.JsonParser;

import it.org.openntf.xsp.jakartaee.AbstractWebClientTest;

@SuppressWarnings("nls")
public class TestRestJson extends AbstractWebClientTest {
	@SuppressWarnings("unchecked")
	@Test
	public void testJsonp() throws JsonException {
		Client client = getAnonymousClient();
		WebTarget target = client.target(getRestUrl(null) + "/jsonExample/jsonp");
		Response response = target.request().get();
		
		String json = response.readEntity(String.class);
		Map<String, Object> jsonObject = (Map<String, Object>)JsonParser.fromJson(JsonJavaFactory.instance, json);
		assertEquals("baz", jsonObject.get("bar"));
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testJsonb() throws JsonException {
		Client client = getAnonymousClient();
		WebTarget target = client.target(getRestUrl(null) + "/jsonExample");
		Response response = target.request().get();
		
		String json = response.readEntity(String.class);
		Map<String, Object> jsonObject = (Map<String, Object>)JsonParser.fromJson(JsonJavaFactory.instance, json);
		assertEquals("bar", jsonObject.get("foo"));
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testJsonbCdi() throws JsonException {
		Client client = getAnonymousClient();
		WebTarget target = client.target(getRestUrl(null) + "/jsonExample/jsonb");
		Response response = target.request().get();
		
		String json = response.readEntity(String.class);
		Map<String, Object> jsonObject = (Map<String, Object>)JsonParser.fromJson(JsonJavaFactory.instance, json);
		String jsonMessage = (String)jsonObject.get("jsonMessage");
		assertTrue(jsonMessage.startsWith("I'm application guy at "));
	}
}
