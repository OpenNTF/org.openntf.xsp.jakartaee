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
package it.org.openntf.xsp.jakartaee.nsf.jsp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import it.org.openntf.xsp.jakartaee.AbstractWebClientTest;
import it.org.openntf.xsp.jakartaee.TestDatabase;
import it.org.openntf.xsp.jakartaee.providers.MainAndModuleProvider;

@SuppressWarnings("nls")
public class TestJspJstl extends AbstractWebClientTest {
	
	@ParameterizedTest
	@ArgumentsSource(MainAndModuleProvider.EnumAndBrowser.class)
	public void testHelloPage(TestDatabase db, WebDriver driver) {
		driver.get(getRootUrl(driver, db) + "/hello.jsp");
		
		WebElement p = driver.findElement(By.xpath("//p[1]"));
		assertTrue(p.getText().startsWith("My CDI Bean is: I'm request guy"), () -> "Unexpected HTML: " + driver.getPageSource());
		
		WebElement dd = driver.findElement(By.xpath("//fieldset/p"));
		assertEquals("I was sent: Value sent into the tag", dd.getText(), () -> "Unexpected HTML: " + driver.getPageSource());
	}
}
