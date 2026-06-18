/*
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
package it.org.openntf.xsp.jakartaee.nsfmodule.basics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import it.org.openntf.xsp.jakartaee.AbstractWebClientTest;
import it.org.openntf.xsp.jakartaee.TestDatabase;
import it.org.openntf.xsp.jakartaee.providers.BrowserArgumentsProvider;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;

@SuppressWarnings("nls")
public class TestNSFJakartaModuleBasics extends AbstractWebClientTest {
	/**
	 * Tests that a module registered in the config but marked as disabled is
	 * not loaded
	 */
	@Test
	public void testDisabledModuleApp() {
		Client client = getAdminClient();
		WebTarget target = client.target(getRootUrl(null, TestDatabase.DISABLED_MODULE));
		Response response = target.request().get();
		checkResponse(404, response);
		String content = response.readEntity(String.class);
		assertTrue(content.contains("File not found or unable to read file"), () -> "Did not receive expected 404 page; got content: " + content);
	}
	
	/**
	 * Tests that welcome-file-list will work for static resources in
	 * a subdirectory
	 */
	@ParameterizedTest
	@ArgumentsSource(BrowserArgumentsProvider.class)
	public void testSubdirectoryWelcomeStatic(WebDriver driver) {
		driver.get(getRootUrl(driver, TestDatabase.MAIN_MODULE) + "/somedir2");
		
		try {
			// Test a basic HTML element to make sure it was served
			WebElement h1 = driver.findElement(By.tagName("h1"));
			assertNotNull(h1);
			assertEquals("I am a static HTML file", h1.getText());
		} catch(Exception e) {
			fail("Encountered exception with page source:\n" + driver.getPageSource(), e);
		}
	}
}
