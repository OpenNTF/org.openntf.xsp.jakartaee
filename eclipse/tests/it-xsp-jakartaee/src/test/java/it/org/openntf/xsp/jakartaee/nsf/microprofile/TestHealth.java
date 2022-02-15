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
package it.org.openntf.xsp.jakartaee.nsf.microprofile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
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
public class TestHealth extends AbstractWebClientTest {
	@SuppressWarnings("unchecked")
	@Test
	public void testAll() throws JsonException {
		Client client = getAnonymousClient();
		WebTarget target = client.target(getRestUrl(null) + "/health");
		Response response = target.request().get();
		
		String result = response.readEntity(String.class);
		Map<String, Object> jsonObject = (Map<String, Object>)JsonParser.fromJson(JsonJavaFactory.instance, result);
		assertEquals("DOWN", jsonObject.get("status"));
		List<Map<String, Object>> checks = (List<Map<String, Object>>)jsonObject.get("checks");
		assertEquals(3, checks.size());
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testReadiness() throws JsonException {
		Client client = getAnonymousClient();
		WebTarget target = client.target(getRestUrl(null) + "/health/ready");
		Response response = target.request().get();
		
		String result = response.readEntity(String.class);
		Map<String, Object> jsonObject = (Map<String, Object>)JsonParser.fromJson(JsonJavaFactory.instance, result);
		assertEquals("DOWN", jsonObject.get("status"));
		List<Map<String, Object>> checks = (List<Map<String, Object>>)jsonObject.get("checks");
		assertEquals(1, checks.size());
		
		assertEquals("I am a failing readiness check", checks.get(0).get("name"));
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testLiveness() throws JsonException {
		Client client = getAnonymousClient();
		WebTarget target = client.target(getRestUrl(null) + "/health/live");
		Response response = target.request().get();
		
		String result = response.readEntity(String.class);
		Map<String, Object> jsonObject = (Map<String, Object>)JsonParser.fromJson(JsonJavaFactory.instance, result);
		assertEquals("UP", jsonObject.get("status"));
		List<Map<String, Object>> checks = (List<Map<String, Object>>)jsonObject.get("checks");
		assertEquals(1, checks.size());
		
		assertEquals("I am the liveliness check", checks.get(0).get("name"));
		Map<String, Object> data = (Map<String, Object>)checks.get(0).get("data");
		assertTrue(((Number)data.get("noteCount")).intValue() > 0);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testStarted() throws JsonException {
		Client client = getAnonymousClient();
		WebTarget target = client.target(getRestUrl(null) + "/health/started");
		Response response = target.request().get();
		
		String result = response.readEntity(String.class);
		Map<String, Object> jsonObject = (Map<String, Object>)JsonParser.fromJson(JsonJavaFactory.instance, result);
		assertEquals("UP", jsonObject.get("status"));
		List<Map<String, Object>> checks = (List<Map<String, Object>>)jsonObject.get("checks");
		assertEquals(1, checks.size());
		
		assertEquals("started up fine", checks.get(0).get("name"));
	}
	
}
