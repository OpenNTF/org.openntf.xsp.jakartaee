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
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Map;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.ibm.commons.util.io.json.JsonException;
import com.ibm.commons.util.io.json.JsonJavaFactory;
import com.ibm.commons.util.io.json.JsonParser;

import it.org.openntf.xsp.jakartaee.AbstractWebClientTest;

@SuppressWarnings("nls")
public class TestRestClient extends AbstractWebClientTest {
	@SuppressWarnings("unchecked")
	@Test
	@Disabled("Disabled pending figuring out local URLs in a container")
	public void testRestClient() throws JsonException {
		Client client = getAnonymousClient();
		WebTarget target = client.target(getRestUrl(null) + "/restClient");
		Response response = target.request().get();
		
		String json = response.readEntity(String.class);
		Map<String, Object> jsonObject = (Map<String, Object>)JsonParser.fromJson(JsonJavaFactory.instance, json);
		Map<String, Object> responseObj = (Map<String, Object>)jsonObject.get("response");
		assertNotNull(responseObj, () -> json);
		assertEquals("bar", responseObj.get("foo"), () -> json);
	}
}
