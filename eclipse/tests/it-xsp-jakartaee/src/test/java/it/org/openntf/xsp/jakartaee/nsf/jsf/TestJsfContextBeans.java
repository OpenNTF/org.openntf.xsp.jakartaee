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
package it.org.openntf.xsp.jakartaee.nsf.jsf;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import it.org.openntf.xsp.jakartaee.AbstractWebClientTest;
import it.org.openntf.xsp.jakartaee.TestDatabase;
import it.org.openntf.xsp.jakartaee.providers.MainAndModuleProvider;

/**
 * Test for contextual beans added for issue #455
 * 
 * @see <a href="https://github.com/OpenNTF/org.openntf.xsp.jakartaee/issues/455">Issue #455</a>
 */
@SuppressWarnings("nls")
public class TestJsfContextBeans extends AbstractWebClientTest {
	@ParameterizedTest
	@ArgumentsSource(MainAndModuleProvider.EnumAndBrowser.class)
	public void testHelloPage(TestDatabase db, WebDriver driver) {
		driver.get(getRootUrl(driver, db) + "/httpContext.xhtml");

		try {
			{
				WebElement dd = driver.findElement(By.xpath("//dt[text()=\"httpContextGuy.request.contextPath\"]/following-sibling::dd[1]"));
				assertEquals(db.getContextPath(), dd.getText(), () -> "Unexpected source: " + driver.getPageSource());
			}
			{
				WebElement dd = driver.findElement(By.xpath("//dt[text()=\"httpContextGuy.response.status\"]/following-sibling::dd[1]"));
				assertEquals("200", dd.getText(), () -> "Unexpected source: " + driver.getPageSource());
			}
			{
				WebElement dd = driver.findElement(By.xpath("//dt[text()=\"httpContextGuy.context.serverInfo\"]/following-sibling::dd[1]"));
				assertEquals("XPages-Domino Web Container", dd.getText(), () -> "Unexpected source: " + driver.getPageSource());
			}
		} catch(Exception e) {
			fail("Encountered exception with page source:\n" + driver.getPageSource(), e);
		}
	}
}
