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
package it.org.openntf.xsp.jakartaee.nsf.jaxrs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.openqa.selenium.WebDriver;

import it.org.openntf.xsp.jakartaee.AbstractWebClientTest;
import it.org.openntf.xsp.jakartaee.TestDatabase;
import it.org.openntf.xsp.jakartaee.providers.BrowserArgumentsProvider;
import it.org.openntf.xsp.jakartaee.providers.MainAndModuleProvider;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;

@SuppressWarnings("nls")
public class TestJaxRsClient extends AbstractWebClientTest {

	@ParameterizedTest
	@ArgumentsSource(MainAndModuleProvider.EnumOnly.class)
	public void testJaxRsClient(TestDatabase db) {
		Client client = getAnonymousClient();
		WebTarget target = client.target(getRestUrl(null, db) + "/jaxrsClient");
		Response response = target.request()
				.header("Host", "localhost:80")
				.get();
		
		JsonObject output = response.readEntity(JsonObject.class);
		
		assertEquals("bar", output.getString("foo", null), () -> "Received incorrect JSON: " + output);
	}

	@ParameterizedTest
	@ArgumentsSource(BrowserArgumentsProvider.class)
	public void testJaxRsClientXPages(WebDriver driver) {
		Client client = getAnonymousClient();
		WebTarget target = client.target(getRootUrl(null, TestDatabase.MAIN) + "/jaxrsClient.xsp");
		Response response = target.request()
				.header("Host", "localhost:80")
				.get();
		
		String html = response.readEntity(String.class);
		
		Document doc = Jsoup.parse(html);
		try {
			Element dd = doc.selectXpath("//div[@id='container']/span").get(0);
			assertEquals("{\"foo\":\"bar\"}", dd.text());
		} catch(Exception e) {
			fail("Failed with HTML " + html, e);
		}
	}

	@ParameterizedTest
	@ArgumentsSource(MainAndModuleProvider.EnumOnly.class)
	public void testJaxRsClientImplicitJson(TestDatabase db) {
		Client client = getAnonymousClient();
		WebTarget target = client.target(getRestUrl(null, db) + "/jaxrsClient/exampleObject");
		Response response = target.request()
				.header("Host", "localhost:80")
				.get();
		
		JsonObject output = response.readEntity(JsonObject.class);
		
		assertEquals("bar", output.getString("foo", null), () -> "Received incorrect JSON: " + output);
	}

	@ParameterizedTest
	@ArgumentsSource(MainAndModuleProvider.EnumOnly.class)
	public void testEchoObject(TestDatabase db) {
		Client client = getAnonymousClient();
		WebTarget target = client.target(getRestUrl(null, db) + "/jaxrsClient/echoExampleObject");
		JsonObject obj = Json.createObjectBuilder()
			.add("foo", "Echo me")
			.build();
		Response response = target.request()
				.header("Host", "localhost:80")
				.post(Entity.json(obj));
		
		JsonObject output = response.readEntity(JsonObject.class);
		
		assertEquals("Echo me - return value", output.getString("foo", null), () -> "Received incorrect JSON: " + output);
	}

	@ParameterizedTest
	@ArgumentsSource(MainAndModuleProvider.EnumOnly.class)
	public void testAsyncSelfEcho(TestDatabase db) {
		Client client = getAnonymousClient();
		WebTarget target = client.target(getRestUrl(null, db) + "/jaxrsClient/roundTripEcho");
		Response response = target.request()
				.header("Host", "localhost:80")
				.get();
		
		JsonObject output = response.readEntity(JsonObject.class);
		
		assertEquals("sending from async - return value", output.getString("foo", null), () -> "Received incorrect JSON: " + output);
	}

	@ParameterizedTest
	@ArgumentsSource(MainAndModuleProvider.EnumOnly.class)
	public void testAsyncSelfEchoAsync(TestDatabase db) {
		Client client = getAnonymousClient();
		WebTarget target = client.target(getRestUrl(null, db) + "/jaxrsClient/roundTripEchoAsync");
		Response response = target.request()
				.header("Host", "localhost:80")
				.get();
		
		JsonObject output = response.readEntity(JsonObject.class);
		
		assertEquals("sending from async - return value", output.getString("foo", null), () -> "Received incorrect JSON: " + output);
	}

	@ParameterizedTest
	@ArgumentsSource(MainAndModuleProvider.EnumOnly.class)
	public void testAsyncSelfEchoDoubleAsync(TestDatabase db) {
		Client client = getAnonymousClient();
		WebTarget target = client.target(getRestUrl(null, db) + "/jaxrsClient/roundTripEchoDoubleAsync");
		Response response = target.request()
				.header("Host", "localhost:80")
				.get();
		
		JsonObject output = response.readEntity(JsonObject.class);
		
		assertEquals("sending from async - return value", output.getString("foo", null), () -> "Received incorrect JSON: " + output);
	}
}
