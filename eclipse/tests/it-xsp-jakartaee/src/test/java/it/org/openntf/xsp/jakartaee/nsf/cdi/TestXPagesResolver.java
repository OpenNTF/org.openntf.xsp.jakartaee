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
package it.org.openntf.xsp.jakartaee.nsf.cdi;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.ibm.commons.util.StringUtil;

import it.org.openntf.xsp.jakartaee.AbstractWebClientTest;
import it.org.openntf.xsp.jakartaee.TestDatabase;
import it.org.openntf.xsp.jakartaee.providers.BrowserArgumentsProvider;

@SuppressWarnings("nls")
public class TestXPagesResolver extends AbstractWebClientTest {
	@ParameterizedTest
	@ArgumentsSource(BrowserArgumentsProvider.class)
	public void testApplicationScopeResolution(WebDriver driver) {
		driver.get(getRootUrl(driver, TestDatabase.MAIN) + "/beans.xsp");
		
		{
			WebElement dd = driver.findElement(By.xpath("//dt[text()=\"Application Guy\"]/following-sibling::dd[1]"));
			assertTrue(dd.getText().startsWith("I'm application guy at "));
		}
		
		// While here, test the phase listeners
		{
			WebElement dd = driver.findElement(By.xpath("//dt[text()=\"Faces Phase Listener Output\"]/following-sibling::dd[1]"));
			assertTrue(dd.getText().isEmpty());
		}
		{
			WebElement dd = driver.findElement(By.xpath("//dt[text()=\"XPages Phase Listener Output\"]/following-sibling::dd[1]"));
			assertTrue(dd.getText().equals("I was set by the XPages listener"));
		}
	}
	
	@ParameterizedTest
	@ArgumentsSource(BrowserArgumentsProvider.class)
	public void testSessionAsSigner(WebDriver driver) {
		driver.get(getRootUrl(driver, TestDatabase.MAIN) + "/beans.xsp");
		
		WebElement dd = driver.findElement(By.xpath("//dt[text()=\"#{sessionAsSigner}\"]/following-sibling::dd[1]"));
		assertTrue(dd.getText().startsWith("CN="));
	}

	
	@ParameterizedTest
	@ArgumentsSource(BrowserArgumentsProvider.class)
	public void testXspContextResolution(WebDriver driver) {
		driver.get(getRootUrl(driver, TestDatabase.MAIN) + "/beans.xsp");
		
		try {
			WebElement dd = driver.findElement(By.xpath("//dt[text()=\"XSP URL\"]/following-sibling::dd[1]"));
			assertTrue(StringUtil.toString(dd.getText()).contains("beans.xsp"), () -> "XSP URL should contain el.xsp; got: " + dd.getText());
		} catch(NoSuchElementException e) {
			fail("Encountered exception with HTML: " + driver.getPageSource(), e);
		}
	}
}
