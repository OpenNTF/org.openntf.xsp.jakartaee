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
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import it.org.openntf.xsp.jakartaee.AbstractWebClientTest;
import it.org.openntf.xsp.jakartaee.BrowserArgumentsProvider;
import it.org.openntf.xsp.jakartaee.TestDatabase;

/**
 * Test for contextual beans added for issue #455
 * 
 * @see <a href="https://github.com/OpenNTF/org.openntf.xsp.jakartaee/issues/455">Issue #455</a>
 */
@SuppressWarnings("nls")
public class TestJspContextBeans extends AbstractWebClientTest {
	@ParameterizedTest
	@ArgumentsSource(BrowserArgumentsProvider.class)
	public void testHelloPage(WebDriver driver) {
		driver.get(getRootUrl(driver, TestDatabase.MAIN) + "/httpContext.jsp");

		try {
			{
				WebElement dd = driver.findElement(By.xpath("//dt[text()=\"httpContextGuy.request.contextPath\"]/following-sibling::dd[1]"));
				assertEquals(TestDatabase.MAIN.getContextPath(), dd.getText());
			}
			{
				WebElement dd = driver.findElement(By.xpath("//dt[text()=\"httpContextGuy.response.status\"]/following-sibling::dd[1]"));
				assertEquals("200", dd.getText());
			}
			{
				WebElement dd = driver.findElement(By.xpath("//dt[text()=\"httpContextGuy.context.serverInfo\"]/following-sibling::dd[1]"));
				assertEquals("XPages-Domino Web Container", dd.getText());
			}
		} catch(Exception e) {
			fail("Encountered exception with page source:\n" + driver.getPageSource(), e);
		}
	}
}
