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

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import it.org.openntf.xsp.jakartaee.AbstractWebClientTest;
import it.org.openntf.xsp.jakartaee.BrowserArgumentsProvider;

@SuppressWarnings("nls")
public class TestMvcJsp extends AbstractWebClientTest {
	
	@ParameterizedTest
	@ArgumentsSource(BrowserArgumentsProvider.class)
	public void testHelloPage(WebDriver driver) {
		driver.get(getRestUrl(driver) + "/mvc?foo=bar");
		
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
}
