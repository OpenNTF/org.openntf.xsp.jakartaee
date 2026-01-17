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
package it.org.openntf.xsp.jakartaee.nsf.mvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import it.org.openntf.xsp.jakartaee.AbstractWebClientTest;
import it.org.openntf.xsp.jakartaee.TestDatabase;
import it.org.openntf.xsp.jakartaee.providers.MainAndModuleProvider;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;

@SuppressWarnings("nls")
public class TestMvcExceptions extends AbstractWebClientTest {
	@ParameterizedTest
	@ArgumentsSource(MainAndModuleProvider.EnumAndBrowser.class)
	public void testHtml(TestDatabase db, WebDriver driver) {
		try {
			driver.get(getRestUrl(driver, db) + "/mvc/exception");
			
			WebElement span = driver.findElement(By.xpath("//h2[text()=\"Exception\"]/following-sibling::span[1]"));
			assertEquals("I am an exception from an MVC resource", span.getText());
		} catch(Exception e) {
			fail("Encountered exception with page source:\n" + driver.getPageSource(), e);
		}
	}
	
	/**
	 * Tests that a fake endpoint ends up with a 404 and standard error page
	 */
	@ParameterizedTest
	@ArgumentsSource(MainAndModuleProvider.EnumAndBrowser.class)
	public void testNotFound(TestDatabase db, WebDriver driver) {
		try {
			{
				driver.get(getRestUrl(driver, db) + "/mvc/notFound");
				
				WebElement span = driver.findElement(By.xpath("//h2[text()=\"Exception\"]/following-sibling::span[1]"));
				assertTrue(span.getText().startsWith("I am a programmatic not-found exception from MVC"), () -> "Received unexpected page content: " + driver.getPageSource());
			}
			{
				Client client = getAnonymousClient();
				WebTarget target = client.target(getRestUrl(null, db) + "/mvc/notFound");
				Response response = target.request().get();
				assertEquals(404, response.getStatus());
			}
		} catch(Exception e) {
			fail("Encountered exception with page source:\n" + driver.getPageSource(), e);
		}
	}
	
	/**
	 * Tests that a an endpoint throwing ForbiddenException gets an appropriate error
	 */
	@ParameterizedTest
	@ArgumentsSource(MainAndModuleProvider.EnumAndBrowser.class)
	public void testForbidden(TestDatabase db) {
		{
			Client client = getAnonymousClient();
			WebTarget target = client.target(getRestUrl(null, db) + "/mvc/forbidden");
			Response response = target.request().get();
			assertEquals(401, response.getStatus());
			
			String content = response.readEntity(String.class);
			assertTrue(content.contains("<input name=\"Password\""), () -> "Unexpected content: " + content);
		}
		{
			Client client = getAdminClient();
			WebTarget target = client.target(getRestUrl(null, db) + "/mvc/forbidden");
			Response response = target.request().get();
			assertEquals(401, response.getStatus());
			
			String content = response.readEntity(String.class);
			assertTrue(content.contains("do not have access to this resource"), () -> "Unexpected content: " + content);
		}
	}
}
