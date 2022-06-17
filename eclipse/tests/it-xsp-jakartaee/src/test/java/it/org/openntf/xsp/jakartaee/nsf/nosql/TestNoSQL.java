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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;

import org.checkerframework.checker.nullness.qual.AssertNonNullIfNonNull;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.ibm.commons.util.io.json.JsonException;
import com.ibm.commons.util.io.json.JsonJavaFactory;
import com.ibm.commons.util.io.json.JsonParser;

import it.org.openntf.xsp.jakartaee.AbstractWebClientTest;

public class TestNoSQL extends AbstractWebClientTest {
	@SuppressWarnings("unchecked")
	@Test
	public void testNoSql() throws JsonException {
		Client client = getAnonymousClient();
		WebTarget target = client.target(getRestUrl(null) + "/nosql?lastName=CreatedUnitTest"); //$NON-NLS-1$
		
		{
			Response response = target.request().get();
			
			String json = response.readEntity(String.class);
			Map<String, Object> jsonObject = (Map<String, Object>)JsonParser.fromJson(JsonJavaFactory.instance, json);
			
			List<Object> byQueryLastName = (List<Object>)jsonObject.get("byQueryLastName"); //$NON-NLS-1$
			assertNotNull(byQueryLastName, () -> String.valueOf(jsonObject));
			assertTrue(byQueryLastName.isEmpty(), () -> String.valueOf(jsonObject));
		}
		
		// Now use the MVC endpoint to create one, which admittedly is outside this test
		{
			MultivaluedMap<String, String> payload = new MultivaluedHashMap<>();
			payload.putSingle("firstName", "foo"); //$NON-NLS-1$ //$NON-NLS-2$
			payload.putSingle("lastName", "CreatedUnitTest"); //$NON-NLS-1$ //$NON-NLS-2$
			payload.putSingle("customProperty", "i am custom property"); //$NON-NLS-1$ //$NON-NLS-2$
			WebTarget postTarget = client.target(getRestUrl(null) + "/nosql/create"); //$NON-NLS-1$
			Response response = postTarget.request().post(Entity.form(payload));
			assertEquals(303, response.getStatus());
		}
		
		// There should be at least one now
		{
			Response response = target.request().get();
			
			String json = response.readEntity(String.class);
			Map<String, Object> jsonObject = (Map<String, Object>)JsonParser.fromJson(JsonJavaFactory.instance, json);
			
			List<Map<String, Object>> byQueryLastName = (List<Map<String, Object>>)jsonObject.get("byQueryLastName"); //$NON-NLS-1$
			assertFalse(byQueryLastName.isEmpty());
			Map<String, Object> entry = byQueryLastName.get(0);
			assertEquals("CreatedUnitTest", entry.get("lastName")); //$NON-NLS-1$ //$NON-NLS-2$
			{
				Object customProp = entry.get("customProperty"); //$NON-NLS-1$
				assertTrue(customProp instanceof Map, "customProperty should be a Map"); //$NON-NLS-1$
				String val = (String)((Map<String, Object>)customProp).get("value"); //$NON-NLS-1$
				assertEquals("i am custom property", val); //$NON-NLS-1$
			}
			assertFalse(((String)entry.get("unid")).isEmpty()); //$NON-NLS-1$
		}
	}
	
	@SuppressWarnings("unchecked")
	@Test
	@Disabled("QRP#executeToView is currently broken on Linux (12.0.1IF2)")
	public void testNoSqlNames() throws JsonException {
		Client client = getAnonymousClient();
		WebTarget target = client.target(getRestUrl(null) + "/nosql/servers"); //$NON-NLS-1$
		
		Response response = target.request().get();
		
		String json = response.readEntity(String.class);
		Map<String, Object> jsonObject = (Map<String, Object>)JsonParser.fromJson(JsonJavaFactory.instance, json);
		
		List<Map<String, Object>> all = (List<Map<String, Object>>)jsonObject.get("all"); //$NON-NLS-1$
		assertNotNull(all, () -> json);
		assertFalse(all.isEmpty(), () -> json);
		Map<String, Object> entry = all.get(0);
		assertEquals("CN=JakartaEE/O=OpenNTFTest", entry.get("serverName"), () -> json); //$NON-NLS-1$ //$NON-NLS-2$
		assertFalse(((String)entry.get("unid")).isEmpty(), () -> json); //$NON-NLS-1$
		assertEquals(1d, ((Number)jsonObject.get("totalCount")).doubleValue(), () -> json); //$NON-NLS-1$
	}
}
