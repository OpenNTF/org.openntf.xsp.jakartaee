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
package it.org.openntf.xsp.jakartaee.nsf.mvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import it.org.openntf.xsp.jakartaee.AbstractWebClientTest;
import it.org.openntf.xsp.jakartaee.BrowserArgumentsProvider;

@SuppressWarnings("nls")
public class TestMvcExceptions extends AbstractWebClientTest {
	@ParameterizedTest
	@ArgumentsSource(BrowserArgumentsProvider.class)
	public void testHtml(WebDriver driver) {
		driver.get(getRestUrl(driver) + "/mvc/exception");
		
		WebElement span = driver.findElement(By.xpath("//h2[text()=\"Exception\"]/following-sibling::span[1]"));
		assertEquals("I am an exception from an MVC resource", span.getText());
	}
	
	/**
	 * Tests that a fake endpoint ends up with a 404 and standard error page
	 */
	@ParameterizedTest
	@ArgumentsSource(BrowserArgumentsProvider.class)
	public void testNotFound(WebDriver driver) {
		{
			driver.get(getRestUrl(driver) + "/mvc/notFound");
			
			WebElement span = driver.findElement(By.xpath("//h2[text()=\"Exception\"]/following-sibling::span[1]"));
			assertTrue(span.getText().startsWith("I am a programmatic not-found exception from MVC"));
		}
		{
			Client client = getAnonymousClient();
			WebTarget target = client.target(getRestUrl(null) + "/mvc/notFound");
			Response response = target.request().get();
			assertEquals(404, response.getStatus());
		}
	}
}
