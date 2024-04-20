/**
 * Copyright (c) 2018-2024 Contributors to the XPages Jakarta EE Support Project
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
package it.org.openntf.xsp.jakartaee.nsf.jsp;

import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;

import com.ibm.commons.util.StringUtil;

import it.org.openntf.xsp.jakartaee.AbstractWebClientTest;
import it.org.openntf.xsp.jakartaee.TestDatabase;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;

/**
 * @author Jesse Gallagher
 * @since 2.5.0
 */
@SuppressWarnings("nls")
public class TestJsp extends AbstractWebClientTest {
	/**
	 * Tests to ensure that a JSP file that doesn't exist leads to a
	 * non-empty 404 page.
	 */
	@Test
	public void testNotFound() {
		Client client = getAnonymousClient();
		WebTarget target = client.target(getRootUrl(null, TestDatabase.MAIN) + "/somefakepage.jsp");
		Response response = target.request().get();
		
		checkResponse(404, response);
		
		String content = response.readEntity(String.class);
		assertFalse(StringUtil.isEmpty(content));
	}
}
