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
package it.org.openntf.xsp.jakartaee.servlet.cdi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.Map;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.junit.jupiter.api.Test;

import com.ibm.commons.util.io.json.JsonException;
import com.ibm.commons.util.io.json.JsonJavaFactory;
import com.ibm.commons.util.io.json.JsonParser;

import it.org.openntf.xsp.jakartaee.AbstractWebClientTest;

@SuppressWarnings({ "unchecked", "nls" })
public class TestBundleBeanResource extends AbstractWebClientTest {
	
	@Test
	public void testBundleBean() throws JsonException {
		int expectedIdentity = 0;
		// First NSF - uses .cdibundle
		{
			Map<String, Object> obj = getBean(getBudleNsfRootUrl(null) + "/exampleservlet");
			assertEquals("Hello from bundleBean", obj.get("hello"));
			expectedIdentity = ((Number)obj.get("identity")).intValue();
			assertNotEquals(0, expectedIdentity);
		}
		// Call this again to ensure that it uses the same bean
		{
			Map<String, Object> obj = getBean(getBudleNsfRootUrl(null) + "/exampleservlet");
			assertEquals("Hello from bundleBean", obj.get("hello"));
			int identity = ((Number)obj.get("identity")).intValue();
			assertEquals(expectedIdentity, identity);
		}
		// Second NSF - uses .cdibundlebase, so should be separate bean
		{
			Map<String, Object> obj = getBean(getBaseBudleNsfRootUrl(null) + "/exampleservlet");
			assertEquals("Hello from bundleBean", obj.get("hello"));
			int identity = ((Number)obj.get("identity")).intValue();
			assertNotEquals(expectedIdentity, identity);
		}
	}
	
	private Map<String, Object> getBean(String base) throws JsonException {
		Client client = getAnonymousClient();
		WebTarget target = client.target(base + "/bean");
		Response response = target.request().get();
		
		String output = response.readEntity(String.class);
		Map<String, Object> obj = (Map<String, Object>)JsonParser.fromJson(JsonJavaFactory.instance, output);
		return obj;
	}
}
