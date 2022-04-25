/**
 * Copyright © 2018-2022 Contributors to the XPages Jakarta EE Support Project
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
package it.org.openntf.xsp.jakartaee.nsf.jsf;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.ibm.commons.util.StringUtil;

import it.org.openntf.xsp.jakartaee.AbstractWebClientTest;
import it.org.openntf.xsp.jakartaee.BrowserArgumentsProvider;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;

@SuppressWarnings("nls")
public class TestJsf extends AbstractWebClientTest {
	
	@ParameterizedTest
	@ArgumentsSource(BrowserArgumentsProvider.class)
	public void testHelloPage(WebDriver driver) {
		driver.get(getRootUrl(driver) + "/hello.xhtml");

		String expected = "inputValue" + System.currentTimeMillis();
		{
			WebElement form = driver.findElement(By.xpath("//form[1]"));

			WebElement dd = driver.findElement(By.xpath("//dt[text()=\"Request Method\"]/following-sibling::dd[1]"));
			assertEquals("GET", dd.getText());
			
			WebElement input = form.findElement(By.xpath("input[1]"));
			assertTrue(input.getAttribute("id").endsWith(":appGuyProperty"), () -> input.getAttribute("id"));
			input.click();
			input.sendKeys(expected);
			assertEquals(expected, input.getAttribute("value"));
			
			WebElement submit = form.findElement(By.xpath("input[@type='submit']"));
			assertEquals("Refresh", submit.getAttribute("value"));
			submit.click();
		}
		{
			
			WebElement form = driver.findElement(By.xpath("//form[1]"));
			
			WebElement span = form.findElement(By.xpath("p/span[1]"));
			assertEquals(expected, span.getText());
		}
	}
	
	/**
	 * Tests to ensure that a JSF file that doesn't exist leads to a
	 * non-empty 404 page.
	 */
	@Test
	public void testNotFound() {
		Client client = getAnonymousClient();
		WebTarget target = client.target(getRootUrl(null) + "/somefakepage.xhtml");
		Response response = target.request().get();
		
		assertEquals(404, response.getStatus());
		
		String content = response.readEntity(String.class);
		assertFalse(StringUtil.isEmpty(content));
	}
}
