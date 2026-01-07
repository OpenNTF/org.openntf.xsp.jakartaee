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
package it.org.openntf.xsp.jakartaee.nsf.jsp;

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
import it.org.openntf.xsp.jakartaee.providers.MainAndModuleProvider;

@SuppressWarnings("nls")
public class TestJspRecords extends AbstractWebClientTest {
	@ParameterizedTest
	@ArgumentsSource(MainAndModuleProvider.EnumAndBrowser.class)
	public void testResolution(TestDatabase db, WebDriver driver) {
		driver.get(getRootUrl(driver, db) + "/recordExample.jsp");
		
		try {
			{
				WebElement dd = driver.findElement(By.cssSelector(".text-output"));
				assertEquals("I am the example", dd.getText());
			}
			
			// Chained empty Optional in EL
			{
				WebElement dd = driver.findElement(By.cssSelector(".text-output2"));
				assertEquals("", dd.getText());
			}
			// Chained full Optional in EL
			{
				WebElement dd = driver.findElement(By.cssSelector(".text-output3"));
				assertEquals("I am the optional example", dd.getText());
			}
			// Traditional bean-style getter method
			{
				WebElement dd = driver.findElement(By.cssSelector(".text-output4"));
				assertEquals("I am the message for I am the example", dd.getText());
			}
			
		} catch(NoSuchElementException e) {
			fail("Encountered exception with HTML: " + driver.getPageSource(), e);
		}
	}
}
