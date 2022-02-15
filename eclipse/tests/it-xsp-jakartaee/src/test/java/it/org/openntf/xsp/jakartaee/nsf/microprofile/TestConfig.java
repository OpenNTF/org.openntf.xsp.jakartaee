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
import static org.junit.jupiter.api.Assertions.assertFalse;
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
public class TestConfig extends AbstractWebClientTest {
	@SuppressWarnings("unchecked")
	@Test
	public void testConfig() throws JsonException {
		Client client = getAnonymousClient();
		WebTarget target = client.target(getRestUrl(null) + "/config");
		Response response = target.request().get();
		
		String json = response.readEntity(String.class);
		Map<String, Object> jsonObject = (Map<String, Object>)JsonParser.fromJson(JsonJavaFactory.instance, json);
		assertFalse(((String)jsonObject.get("java.version")).isEmpty(), () -> json);
		assertTrue(((String)jsonObject.get("xsp.library.depends")).startsWith("org.openntf.xsp.el"), () -> json);
		assertEquals("/local/notesdata", jsonObject.get("Directory"));
		assertEquals("foo", jsonObject.get("mpconfig.example.setting"));
	}
}
