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
package it.org.openntf.xsp.jakartaee.nsf.mvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import it.org.openntf.xsp.jakartaee.AbstractWebClientTest;
import it.org.openntf.xsp.jakartaee.TestDatabase;
import it.org.openntf.xsp.jakartaee.providers.MainAndModuleProvider;

@SuppressWarnings("nls")
public class TestMvcJsf extends AbstractWebClientTest {

	@ParameterizedTest
	@ArgumentsSource(MainAndModuleProvider.EnumAndBrowser.class)
	@Order(1)
	public void testHelloPage(TestDatabase db, WebDriver driver) {
		driver.get(getRestUrl(driver, db) + "/mvcjsf");

		try {
			String expected = "inputValue" + System.currentTimeMillis();
			{
				WebElement form = driver.findElement(By.xpath("//form[1]"));
	
				WebElement dd = driver.findElement(By.xpath("//dt[text()=\"Request Method\"]/following-sibling::dd[1]"));
				assertEquals("GET", dd.getText());
				
				WebElement input = form.findElement(By.xpath("input[1]"));
				assertTrue(input.getAttribute("id").endsWith(":appGuyProperty"), () -> input.getAttribute("id"));
				input.click();
				// May be set by previous test
				input.clear();
				input.sendKeys(expected);
				assertEquals(expected, input.getAttribute("value"));
				
				WebElement submit = form.findElement(By.xpath("input[@type='submit']"));
				assertEquals("Refresh", submit.getAttribute("value"));
				submit.click();
				// Give it a bit to do the partial refresh
				TimeUnit.MILLISECONDS.sleep(250);
			}
			{
				
				WebElement form = driver.findElement(By.xpath("//form[1]"));
				
				WebElement span = form.findElement(By.xpath("p/span[1]"));
				assertEquals(expected, span.getText());
			}
		} catch(Exception e) {
			fail("Encountered exception with page source:\n" + driver.getPageSource(), e);
		}
	}
}
