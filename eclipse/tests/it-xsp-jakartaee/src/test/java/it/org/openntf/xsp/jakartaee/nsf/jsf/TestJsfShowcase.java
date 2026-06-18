/*
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
package it.org.openntf.xsp.jakartaee.nsf.jsf;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.openqa.selenium.WebDriver;

import com.ibm.commons.util.StringUtil;

import it.org.openntf.xsp.jakartaee.AbstractWebClientTest;
import it.org.openntf.xsp.jakartaee.TestDatabase;
import it.org.openntf.xsp.jakartaee.providers.BrowserArgumentsProvider;

@SuppressWarnings("nls")
public class TestJsfShowcase extends AbstractWebClientTest {
	// TODO figure out why the Module version hits an infinite error.xhtml redirect
	@ParameterizedTest
//	@ArgumentsSource(ShowcaseAndModuleProvider.EnumAndBrowser.class)
	@ArgumentsSource(BrowserArgumentsProvider.class)
	public void testTheming(WebDriver driver) {
		driver.get(getRootUrl(driver, TestDatabase.PRIMEFACES_SHOWCASE) + "/theming.xhtml");
		
		String html = driver.getPageSource();
		assertTrue(html.contains("Give a name to your theme"), () -> "Unexpected HTML: " + html);
	}
	
	/**
	 * Tests that the PF Showcase module processes the welcome-page properly,
	 * which in this case should result in a 302 redirection to include
	 * the jfwid parameter
	 */
	@Test
	public void testShowcaseWelcome() {
		var client = getAnonymousClient();
		
		{
			var target = client.target(getRootUrl(null, TestDatabase.PRIMEFACES_SHOWCASE_MODULE));
			var response = target.request().get();
			
			checkResponse(302, response);
			var loc = response.getHeaderString("Location");
			assertFalse(StringUtil.isEmpty(loc));
			assertTrue(loc.contains("jfwid"), () -> "Unexpected redirect location: " + loc);
		}
		{
			var target = client.target(getRootUrl(null, TestDatabase.PRIMEFACES_SHOWCASE_MODULE) + "/");
			var response = target.request().get();
			
			checkResponse(302, response);
			var loc = response.getHeaderString("Location");
			assertFalse(StringUtil.isEmpty(loc));
			assertTrue(loc.contains("jfwid"), () -> "Unexpected redirect location: " + loc);
		}
	}
}
