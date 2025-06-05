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
package it.org.openntf.xsp.jakartaee.nsf.el;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import it.org.openntf.xsp.jakartaee.AbstractWebClientTest;
import it.org.openntf.xsp.jakartaee.TestDatabase;
import it.org.openntf.xsp.jakartaee.providers.BrowserArgumentsProvider;

@SuppressWarnings("nls")
public class TestElBasics extends AbstractWebClientTest {
	/**
	 * Tests basic EL bean property resolution.
	 */
	@ParameterizedTest
	@ArgumentsSource(BrowserArgumentsProvider.class)
	public void testBasicFunctionClass(WebDriver driver) {
		driver.get(getRootUrl(driver, TestDatabase.MAIN) + "/el.xsp");
		
		try {
			WebElement dd = driver.findElement(By.xpath("//dt[text()=\"#{functionClass.foo}\"]/following-sibling::dd[1]"));
			assertEquals("I am returned from getFoo()", dd.getText());
			
			dd = driver.findElement(By.xpath("//dt[text()=\"#{managedBeanGuy}\"]/following-sibling::dd[1]"));
			assertEquals("I am ManagedBeanGuy#toString", dd.getText());
		} catch(NoSuchElementException e) {
			fail("Encountered exception with HTML: " + driver.getPageSource(), e);
		}
	}
	
	@ParameterizedTest
	@ArgumentsSource(BrowserArgumentsProvider.class)
	public void testLegacyBeanResolution(WebDriver driver) {
		driver.get(getRootUrl(driver, TestDatabase.MAIN) + "/el.xsp");
		
		try {
			WebElement dd = driver.findElement(By.xpath("//dt[text()=\"#{managedBeanGuy}\"]/following-sibling::dd[1]"));
			assertEquals("I am ManagedBeanGuy#toString", dd.getText());
		} catch(NoSuchElementException e) {
			fail("Encountered exception with HTML: " + driver.getPageSource(), e);
		}
	}
}
