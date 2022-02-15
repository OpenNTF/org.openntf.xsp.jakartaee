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
package it.org.openntf.xsp.jakartaee.nsf.xml;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import it.org.openntf.xsp.jakartaee.AbstractWebClientTest;
import it.org.openntf.xsp.jakartaee.BrowserArgumentsProvider;

@SuppressWarnings("nls")
public class TestXmlXPages extends AbstractWebClientTest {
	/**
	 * Tests basic EL bean property resolution.
	 */
	@ParameterizedTest
	@ArgumentsSource(BrowserArgumentsProvider.class)
	public void testXmlXPage(WebDriver driver) {
		driver.get(getRootUrl(driver) + "/jaxb.xsp");
		
		WebElement span = driver.findElement(By.xpath("//span[@style=\"font-family: monospace; whitespace: pre-wrap\"]"));
		assertTrue(
			span.getText().startsWith("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?> <application-guy>"),
			() -> "Got unexpected content: " + span.getText()
		);
	}
}
