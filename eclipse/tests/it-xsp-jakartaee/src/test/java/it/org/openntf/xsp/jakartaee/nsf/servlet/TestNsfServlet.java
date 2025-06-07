/**
 * Copyright (c) 2018-2025 Contributors to the XPages Jakarta EE Support Project
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

import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

import it.org.openntf.xsp.jakartaee.AbstractWebClientTest;
import it.org.openntf.xsp.jakartaee.TestDatabase;
import it.org.openntf.xsp.jakartaee.providers.MainAndModuleProvider;
import jakarta.mail.internet.ContentType;
import jakarta.mail.internet.ParseException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@SuppressWarnings("nls")
public class TestNsfServlet extends AbstractWebClientTest {
	public static class EnumAndServletPathsProvider implements ArgumentsProvider {
		@Override
		public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
			return new MainAndModuleProvider.EnumOnly().provideArguments(context)
				.map(args -> args.get()[0])
				.flatMap(e -> {
					String prefix = ((TestDatabase)e).getXspPrefix();
					String contextPath = ((TestDatabase)e).getContextPath();
					return Stream.of(
						Arguments.of(e, prefix+"/someservlet/dff?bar", String.format("Hello from ExampleServlet. context=%s, path=%s/someservlet, pathInfo=/dff\n", contextPath, prefix)),
						Arguments.of(e, prefix+"/testme.hello", String.format("Hello from ExampleServlet. context=%s, path=%s/testme.hello, pathInfo=null\n", contextPath, prefix)),
						Arguments.of(e, prefix+"/testme.hello/dsd", String.format("Hello from ExampleServlet. context=%s, path=%s/testme.hello, pathInfo=/dsd\n", contextPath, prefix)),
						Arguments.of(e, prefix+"/someservlet?bar", String.format("Hello from ExampleServlet. context=%s, path=%s/someservlet, pathInfo=null\n", contextPath, prefix))
					);
				});
		}
	}
	
	public static class EnumAndMediaTypeProvider implements ArgumentsProvider {
		@Override
		public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
			return new MainAndModuleProvider.EnumOnly().provideArguments(context)
				.map(args -> args.get()[0])
				.flatMap(e -> 
					Stream.of(
						MediaType.TEXT_PLAIN,
						MediaType.TEXT_HTML,
						MediaType.APPLICATION_FORM_URLENCODED,
						MediaType.MULTIPART_FORM_DATA)
					.map(t -> Arguments.of(e, t))
				);
		}
	}
	
	@ParameterizedTest
	@ArgumentsSource(EnumAndServletPathsProvider.class)
	public void testExampleServlet(TestDatabase db, String path, String expectedPrefix) {
		String expected = expectedPrefix + "ApplicationGuy: I'm application guy at ";
		
		Client client = getAnonymousClient();
		WebTarget target = client.target(getRootUrl(null, db) + path);
		Response response = target.request().get();
		
		String body = response.readEntity(String.class);
		assertTrue(body.startsWith(expected), () -> "Body should start with <" + expected + ">, got <" + body + ">");
	}

	@ParameterizedTest
	@ArgumentsSource(MainAndModuleProvider.EnumOnly.class)
	public void testExceptionServlet(TestDatabase db) {
		Client client = getAnonymousClient();
		WebTarget target = client.target(getRootUrl(null, db) + db.getXspPrefix()+"/exceptionservlet");
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
	@ArgumentsSource(EnumAndMediaTypeProvider.class)
	public void testEchoServlet(TestDatabase db, String mediaType) {
		Client client = getAnonymousClient();
		WebTarget target = client.target(getRootUrl(null, db) + db.getXspPrefix()+"/echoServlet");
		Response response = target.request().post(Entity.entity("foo=bar", mediaType));
		
		String body = response.readEntity(String.class);
		assertEquals("foo=bar", body);
	}

	@ParameterizedTest
	@ArgumentsSource(MainAndModuleProvider.EnumOnly.class)
	public void testWebXmlServlet(TestDatabase db) {
		Client client = getAnonymousClient();
		WebTarget target = client.target(getRootUrl(null, db) + db.getXspPrefix()+"/webXmlServlet");
		Response response = target.request().get();
		checkResponse(200, response);
		
		String body = response.readEntity(String.class);
		assertEquals("I am the web.xml Servlet, and I was initialized with I was set by web.xml", body);
	}

	@ParameterizedTest
	@ArgumentsSource(MainAndModuleProvider.EnumOnly.class)
	public void testByteBufferServlet(TestDatabase db) throws ParseException {
		Client client = getAnonymousClient();
		WebTarget target = client.target(getRootUrl(null, db) + db.getXspPrefix()+"/byteBufferServlet");
		
		String payload = "I am the text " + System.currentTimeMillis();
		Response response = target.request().post(Entity.text(payload));
		checkResponse(200, response);
		
		String contentTypeVal = response.getHeaderString(HttpHeaders.CONTENT_TYPE);
		ContentType type = new ContentType(contentTypeVal);
		String encoding = type.getParameter("charset");
		assertEquals(StandardCharsets.US_ASCII.name(), encoding);
		
		String body = response.readEntity(String.class);
		String expected = "Read " + payload.getBytes().length + " bytes of data: " + payload;
		assertEquals(expected, body);
	}
}
