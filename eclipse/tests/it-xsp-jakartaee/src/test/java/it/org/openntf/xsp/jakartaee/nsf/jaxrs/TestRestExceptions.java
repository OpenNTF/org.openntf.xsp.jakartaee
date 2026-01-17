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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.StringReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.stream.Stream;

import com.ibm.commons.util.StringUtil;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import it.org.openntf.xsp.jakartaee.AbstractWebClientTest;
import it.org.openntf.xsp.jakartaee.TestDatabase;
import it.org.openntf.xsp.jakartaee.providers.MainAndModuleProvider;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.NotAcceptableException;
import jakarta.ws.rs.NotAllowedException;
import jakarta.ws.rs.NotSupportedException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@SuppressWarnings("nls")
public class TestRestExceptions extends AbstractWebClientTest {
	public static class EnumAndForbiddenPathsProvider implements ArgumentsProvider {
		@Override
		public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
			return new MainAndModuleProvider.EnumOnly().provideArguments(context)
				.map(args -> args.get()[0])
				.flatMap(e -> {
					return Stream.of(
						Arguments.of(e, "forbidden"),
						Arguments.of(e, "exception/NotAuthorizedException")
					);
				});
		}
	}
	
	public static class EnumAndExceptionsProvider implements ArgumentsProvider {
		@Override
		public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
			return new MainAndModuleProvider.EnumOnly().provideArguments(context)
				.map(args -> args.get()[0])
				.flatMap(e -> {
					return Stream.of(
						Arguments.of(e, BadRequestException.class),
						Arguments.of(e, InternalServerErrorException.class),
						Arguments.of(e, NotAcceptableException.class),
						Arguments.of(e, NotAllowedException.class),
						Arguments.of(e, NotSupportedException.class)
					);
				});
		}
	}
	
	/**
	 * Tests rest.ExceptionExample, which renders an exception as JSON
	 * @throws JsonException 
	 */
	@ParameterizedTest
	@ArgumentsSource(MainAndModuleProvider.EnumOnly.class)
	public void testJson(TestDatabase db) {
		Client client = getAnonymousClient();
		WebTarget target = client.target(getRestUrl(null, db) + "/exceptionExample");
		Response response = target.request().get();
		
		String json = response.readEntity(String.class);
		JsonObject jsonObject = Json.createReader(new StringReader(json)).readObject();
		
		String message = jsonObject.getString("message");
		assertEquals("java.lang.RuntimeException: this is an example exception", message);
		
		JsonArray stackTrace = jsonObject.getJsonArray("stackTrace");
		assertNotNull(stackTrace);
		assertEquals(2, stackTrace.size());
		assertEquals("java.lang.RuntimeException: this is an example exception", stackTrace.getJsonArray(0).getString(0));
	}
	
	/**
	 * Tests rest.ExceptionExample#html, which renders an exception as HTML using the stock
	 * XPages error page
	 */
	@ParameterizedTest
	@ArgumentsSource(MainAndModuleProvider.EnumAndBrowser.class)
	public void testHtml(TestDatabase db, WebDriver driver) {
		driver.get(getRestUrl(driver, db) + "/exceptionExample/html");
		
		WebElement span = driver.findElement(By.xpath("//h2[text()=\"Exception\"]/following-sibling::span[1]"));
		assertEquals("this is expected to be rendered as HTML", span.getText());
	}
	
	/**
	 * Tests rest.ExceptionExample#html, which renders an exception as HTML using the stock
	 * XPages error page
	 */
	@ParameterizedTest
	@ArgumentsSource(MainAndModuleProvider.EnumOnly.class)
	public void testText(TestDatabase db) {
		Client client = getAnonymousClient();
		WebTarget target = client.target(getRestUrl(null, db) + "/exceptionExample/text");
		Response response = target.request().get();
		
		assertTrue(MediaType.TEXT_PLAIN_TYPE.isCompatible(response.getMediaType()));
		String text = response.readEntity(String.class);
		assertTrue(text.startsWith("java.lang.RuntimeException: this is expected to be rendered as text"), "Response should start with the expected stack trace. Received: " + text);
	}
	
	/**
	 * Tests that a fake endpoint ends up with a 404 and standard error page
	 */
	@ParameterizedTest
	@ArgumentsSource(MainAndModuleProvider.EnumAndBrowser.class)
	public void testNotFound(TestDatabase db, WebDriver driver) {
		{
			driver.get(getRestUrl(driver, db) + "/fakeendpoint");
			
			WebElement span = driver.findElement(By.xpath("//h2[text()=\"Exception\"]/following-sibling::span[1]"));
			assertTrue(span.getText().startsWith("RESTEASY003210: Could not find resource for full path"), () -> "Unexpected content: " + span.getText());
		}
		{
			Client client = getAnonymousClient();
			WebTarget target = client.target(getRestUrl(null, db) + "/fakeendpoint");
			Response response = target.request().get();
			assertEquals(404, response.getStatus());
		}
	}
	
	/**
	 * Tests that a an endpoint throwing ForbiddenException gets an appropriate error
	 */
	@ParameterizedTest
	@ArgumentsSource(EnumAndForbiddenPathsProvider.class)
	public void testForbidden(TestDatabase db, String endpoint) {
		{
			Client client = getAnonymousClient();
			WebTarget target = client.target(getRestUrl(null, db) + "/sample/" + endpoint);
			Response response = target.request().get();
			assertEquals(401, response.getStatus());
			
			String content = response.readEntity(String.class);
			assertTrue(content.contains("<input name=\"Password\""), () -> "Unexpected content: " + content);
		}
		{
			Client client = getAdminClient();
			WebTarget target = client.target(getRestUrl(null, db) + "/sample/" + endpoint);
			Response response = target.request().get();
			assertEquals(401, response.getStatus());
			
			String content = response.readEntity(String.class);
			assertTrue(content.contains("do not have access to this resource"), () -> "Unexpected content: " + content);
		}
	}
	
	@ParameterizedTest
	@ArgumentsSource(EnumAndExceptionsProvider.class)
	public <T extends WebApplicationException> void testWebExceptions(TestDatabase db, Class<T> exceptionClass) {
		T e = null;
		try {
			Constructor<T> ctor = exceptionClass.getConstructor();
			e = ctor.newInstance();
		} catch(InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e2) {
			// Ignore
		}
		if(e == null) {
			try {
				Constructor<T> ctor = exceptionClass.getConstructor(String.class);
				e = ctor.newInstance("");
			} catch(InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e2) {
				// Ignore
			}
		}

		if(e == null) {
			try {
				Constructor<T> ctor = exceptionClass.getConstructor(String.class, String[].class);
				e = ctor.newInstance("", new String[0]);
			} catch(InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e2) {
				// Ignore
			}
		}
		
		Client client = getAnonymousClient();
		WebTarget target = client.target(getRestUrl(null, db) + "/sample/exception/" + exceptionClass.getSimpleName());
		Response response = target.request().get();
		assertEquals(e.getResponse().getStatus(), response.getStatus());
		
		String content = response.readEntity(String.class);
		assertFalse(StringUtil.isEmpty(content));
	}
}
