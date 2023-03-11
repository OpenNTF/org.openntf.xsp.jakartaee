/**
 * Copyright (c) 2018-2023 Contributors to the XPages Jakarta EE Support Project
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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.stream.Stream;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.ValueSource;

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
	public void testExampleServlet(String path, String expectedPrefix) {
		String expected = expectedPrefix + "ApplicationGuy: I'm application guy at ";
		
		Client client = getAnonymousClient();
		WebTarget target = client.target(getRootUrl(null) + path);
		Response response = target.request().get();
		
		String body = response.readEntity(String.class);
		assertTrue(body.startsWith(expected), () -> "Body should start with <" + expected + ">, got <" + body + ">");
	}
	
	@Test
	public void testExceptionServlet() {
		Client client = getAnonymousClient();
		WebTarget target = client.target(getRootUrl(null) + "/xsp/exceptionservlet");
		Response response = target.request().get();
		
		String body = response.readEntity(String.class);
		assertTrue(body.contains("java.lang.RuntimeException: I am the expected exception"), () -> "Body should contain stack trace, got: " + body);
	}
	
	/**
	 * Tests to ensure that a Servlet that uses the Reader from the request is able to
	 * echo back the content of the incoming payload.
	 * 
	 * <p>The underlying trouble appears to be that the Faces context initialization sees
	 * incoming form-type POST requests and reads the parameters - then, XspCmdHttpServletRequest
	 * internally opens the InputStream to do so.</p>
	 * 
	 * @see <a href="https://github.com/OpenNTF/org.openntf.xsp.jakartaee/issues/364">Issue #364</a>
	 */
	@ParameterizedTest
	@ValueSource(strings = {
		MediaType.TEXT_PLAIN,
		MediaType.TEXT_HTML,
		MediaType.APPLICATION_FORM_URLENCODED,
		MediaType.MULTIPART_FORM_DATA
	})
	public void testEchoServlet(String mediaType) {
		Client client = getAnonymousClient();
		WebTarget target = client.target(getRootUrl(null) + "/xsp/echoServlet");
		Response response = target.request().post(Entity.entity("foo=bar", mediaType));
		
		String body = response.readEntity(String.class);
		assertEquals("foo=bar", body);
	}
}
