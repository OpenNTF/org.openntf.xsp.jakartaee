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
package it.org.openntf.xsp.jakartaee.nsf.mvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import it.org.openntf.xsp.jakartaee.AbstractWebClientTest;
import it.org.openntf.xsp.jakartaee.BrowserArgumentsProvider;
import it.org.openntf.xsp.jakartaee.TestDatabase;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;

@SuppressWarnings("nls")
public class TestMvcJsp extends AbstractWebClientTest {
	
	@ParameterizedTest
	@ArgumentsSource(BrowserArgumentsProvider.class)
	public void testHelloPage(WebDriver driver) {
		driver.get(getRestUrl(driver, TestDatabase.MAIN) + "/mvc?foo=bar");
		
		{
			WebElement p = driver.findElement(By.xpath("//p[1]"));
			assertEquals("From the URL, I got: bar", p.getText());
		}
		{
			WebElement p = driver.findElement(By.xpath("//p[2]"));
			assertTrue(p.getText().startsWith("Application guy is I'm application guy"), () -> p.getText());
		}
		{
			WebElement p = driver.findElement(By.xpath("//p[6]"));
			assertTrue(p.getText().startsWith("Context from controller is s: CN="), () -> p.getText());
		}
		
		WebElement dd = driver.findElement(By.xpath("//fieldset/p"));
		assertEquals("I was sent: Value sent into the tag", dd.getText());
	}
	
	// Account for cases where only the first MVC call works and then "poisons" future ones - ensure that
	//   a single test can run twice to show it specifically
	@RepeatedTest(2)
	public void testBeanParam() {
		Client client = getAnonymousClient();
		WebTarget target = client.target(getRestUrl(null, TestDatabase.MAIN) + "/mvc/beanParam"); //$NON-NLS-1$
		
		String firstName = "Foo" + System.currentTimeMillis();
		String lastName = "CreatedUnitTest" + System.currentTimeMillis();
		MultivaluedMap<String, String> payload = new MultivaluedHashMap<>();
		payload.putSingle("firstName", firstName);
		payload.putSingle("lastName", lastName);
		Response response = target.request()
			.accept(MediaType.TEXT_HTML_TYPE) // Ensure that it routes to MVC
			.post(Entity.form(payload));
		String html = response.readEntity(String.class);
		assertEquals(200, response.getStatus(), () -> "Invalid response code with HTML: " + html);
		assertTrue(html.contains("Last name: " + lastName), () -> "Unexpected HTML: " + html);
		
	}
}
