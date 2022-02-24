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
package it.org.openntf.xsp.jakartaee.nsf.servlet;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

import it.org.openntf.xsp.jakartaee.AbstractWebClientTest;

@SuppressWarnings("nls")
public class TestNsfServlet extends AbstractWebClientTest {
	public static class ServletPathsProvider implements ArgumentsProvider {
		@Override
		public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
			return Stream.of(
				Arguments.of("/xsp/someservlet/dff?bar", "Hello from ExampleServlet. context=/dev/jakartaee.nsf, path=/xsp/someservlet, pathInfo=/dff\n"),
				Arguments.of("/xsp/testme.hello", "Hello from ExampleServlet. context=/dev/jakartaee.nsf, path=/xsp/testme.hello, pathInfo=null\n"),
				Arguments.of("/xsp/testme.hello/dsd", "Hello from ExampleServlet. context=/dev/jakartaee.nsf, path=/xsp/testme.hello, pathInfo=/dsd\n"),
				Arguments.of("/xsp/someservlet?bar", "Hello from ExampleServlet. context=/dev/jakartaee.nsf, path=/xsp/someservlet, pathInfo=null\n")
			);
		}
	}
	
	@ParameterizedTest
	@ArgumentsSource(ServletPathsProvider.class)
	public void testExampleServlet(String path, String expected) {
		Client client = getAnonymousClient();
		WebTarget target = client.target(getRootUrl(null) + path);
		Response response = target.request().get();
		
		assertEquals(expected, response.readEntity(String.class));
	}
}
