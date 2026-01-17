/**
 * Copyright (c) 2018-2026 Contributors to the XPages Jakarta EE Support Project
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
package it.org.openntf.xsp.jakartaee.nsf.jaxrs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.StringReader;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import it.org.openntf.xsp.jakartaee.AbstractWebClientTest;
import it.org.openntf.xsp.jakartaee.TestDatabase;
import it.org.openntf.xsp.jakartaee.providers.MainAndModuleProvider;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;

@SuppressWarnings("nls")
public class TestJaxRs extends AbstractWebClientTest {
	public static class EnumAndBasePathsProvider implements ArgumentsProvider {
		@Override
		public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
			return new MainAndModuleProvider.EnumOnly().provideArguments(context)
				.map(args -> args.get()[0])
				.flatMap(e -> {
					return Stream.of(
						Arguments.of(e, "/"),
						Arguments.of(e, "")
					);
				});
		}
	}
	
	@ParameterizedTest
	@ArgumentsSource(MainAndModuleProvider.EnumOnly.class)
	public void testSample(TestDatabase db) {
		Client client = getAnonymousClient();
		WebTarget target = client.target(getRestUrl(null, db) + "/sample");
		Response response = target.request().get();
		
		String output = response.readEntity(String.class);
		
		assertTrue(output.startsWith("I'm application guy at"), () -> "Received unexpected output: " + output);
	}
	
	/**
	 * Tests test.Sample#xml, which uses JAX-RS, CDI, and JAX-B.
	 */
	@ParameterizedTest
	@ArgumentsSource(MainAndModuleProvider.EnumOnly.class)
	public void testSampleXml(TestDatabase db) {
		Client client = getAnonymousClient();
		WebTarget target = client.target(getRestUrl(null, db) + "/sample/xml");
		Response response = target.request().get();
		
		Document xmlDoc = response.readEntity(Document.class);
		
		Element applicationGuy = xmlDoc.getDocumentElement();
		assertEquals("application-guy", applicationGuy.getTagName());
		Element time = (Element) applicationGuy.getElementsByTagName("time").item(0);
		assertFalse(time.getTextContent().isEmpty());
		Long.parseLong(time.getTextContent());
	}
	
	@ParameterizedTest
	@ArgumentsSource(EnumAndBasePathsProvider.class)
	public void testBaseResource(TestDatabase db, String path) {
		Client client = getAnonymousClient();
		WebTarget target = client.target(getRestUrl(null, db) + path);
		Response response = target.request().get();
		
		String output = response.readEntity(String.class);
		assertEquals("I am the base resource.", output);
	}
	
	/**
	 * Tests to ensure that a post-matching ContainerResponseFilter adds its custom
	 * header.
	 * 
	 * @see <a href="https://github.com/OpenNTF/org.openntf.xsp.jakartaee/issues/382">Issue #382</a>
	 */
	@ParameterizedTest
	@ArgumentsSource(MainAndModuleProvider.EnumOnly.class)
	public void testRequestFilter(TestDatabase db) {
		Client client = getAnonymousClient();
		WebTarget target = client.target(getRestUrl(null, db));
		Response response = target.request().get();
		
		assertEquals("hello", response.getHeaderString("X-ExampleHeaderFilter"));
	}

	/**
	 * Tests that an in-NSF service class can contribute a configuration property
	 * programmatically.
	 * 
	 * @see <a href="https://github.com/OpenNTF/org.openntf.xsp.jakartaee/issues/168">Issue #168</a>
	 */
	@ParameterizedTest
	@ArgumentsSource(MainAndModuleProvider.EnumOnly.class)
	public void testContributedProperty(TestDatabase db) {
		Client client = getAnonymousClient();
		WebTarget target = client.target(getRestUrl(null, db) + "/jaxrsConfig");
		Response response = target.request().get();
		
		String json = response.readEntity(String.class);
		try {
			JsonObject config = Json.createReader(new StringReader(json)).readObject();
			assertNotNull(config);
			assertEquals("EXPLICIT", config.getString("jakarta.mvc.security.CsrfProtection", null));
		} catch(Exception e) {
			fail("Received unexpected JSON: " + json, e);
		}
	}
	
	/**
	 * Tests that an in-NSF web.xml file can contribute context parameters that will be picked up
	 * by JAX-RS
	 * 
	 * @see <a href="https://github.com/OpenNTF/org.openntf.xsp.jakartaee/issues/469">Issue #469</a>
	 */
	@ParameterizedTest
	@ArgumentsSource(MainAndModuleProvider.EnumOnly.class)
	public void testWebXmlProperty(TestDatabase db) {
		Client client = getAnonymousClient();
		WebTarget target = client.target(getRestUrl(null, db) + "/jaxrsConfig/servlet");
		Response response = target.request().get();
		
		String json = response.readEntity(String.class);
		try {
			JsonObject config = Json.createReader(new StringReader(json)).readObject();
			assertNotNull(config);
			
			assertEquals("I am the param value", config.getString("org.openntf.example.param", null), () ->  "Received unexpected JSON: " + json);
			assertEquals("I am the param value from a fragment in a JAR", config.getString("org.openntf.example.fragment.param", null), () ->  "Received unexpected JSON: " + json);
		} catch(Exception e) {
			fail("Received unexpected JSON: " + json, e);
		}
	}
}
