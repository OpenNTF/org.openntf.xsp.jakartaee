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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.ibm.commons.util.StringUtil;

import it.org.openntf.xsp.jakartaee.AbstractWebClientTest;
import it.org.openntf.xsp.jakartaee.TestDatabase;
import it.org.openntf.xsp.jakartaee.providers.BrowserArgumentsProvider;
import it.org.openntf.xsp.jakartaee.providers.MainAndModuleProvider;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;

@SuppressWarnings("nls")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TestJsf extends AbstractWebClientTest {
	
	public static class EnumBrowserAndHelloProvider implements ArgumentsProvider {
		@Override
		public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
			
			return new MainAndModuleProvider.EnumAndBrowser().provideArguments(context)
				.map(args -> args.get())
				.flatMap(enumAndBrowser ->
					Stream.of("/hello.xhtml", ((TestDatabase)enumAndBrowser[0]).getXspPrefix() + "/helloForExtensionless")
						.map(page -> Arguments.of(enumAndBrowser[0], enumAndBrowser[1], page))
				);
		}
	}
	
	@ParameterizedTest
	@ArgumentsSource(EnumBrowserAndHelloProvider.class)
	@Order(1)
	public void testHelloPage(TestDatabase db, WebDriver driver, String page) {
		driver.get(getRootUrl(driver, db) + page);
		
		try {
			String expected = "inputValue" + System.currentTimeMillis();
			{
				WebElement form = driver.findElement(By.xpath("//form[1]"));
				
				{
					WebElement dd = driver.findElement(By.xpath("//dt[text()=\"Request Method\"]/following-sibling::dd[1]"));
					assertEquals("GET", dd.getText());
				}
				
				// Look for scoped beans, as resolved via CDI
				{
					WebElement dd = driver.findElement(By.xpath("//dt[text()=\"requestGuy.message\"]/following-sibling::dd[1]"));
					assertTrue(dd.getText().startsWith("I'm request guy at "));
				}
				{
					WebElement dd = driver.findElement(By.xpath("//dt[text()=\"sessionGuy.message\"]/following-sibling::dd[1]"));
					assertTrue(dd.getText().startsWith("I'm session guy at "));
				}
				{
					WebElement dd = driver.findElement(By.xpath("//dt[text()=\"applicationGuy.message\"]/following-sibling::dd[1]"));
					assertTrue(dd.getText().startsWith("I'm application guy at "));
				}
				
				// Look for the composite component text
				{
					WebElement dd = driver.findElement(By.xpath("//dt[text()=\"Composite Component\"]/following-sibling::dd[1]"));
					assertEquals("I am text sent to a composite component", dd.getText());
				}
				
				// Make sure Domino objects are available
				{
					WebElement dd = driver.findElement(By.xpath("//dt[text()=\"dominoSession\"]/following-sibling::dd[1]"));
					assertTrue(dd.getText().startsWith("CN="));
				}
				{
					WebElement dd = driver.findElement(By.xpath("//dt[text()=\"database\"]/following-sibling::dd[1]"));
					assertEquals("dev/jakartaee.nsf", dd.getText());
				}
				
				// Make sure the init param from web.xml made it
				{
					WebElement dd = driver.findElement(By.xpath("//dt[text()=\"initParam\"]/following-sibling::dd[1]"));
					assertTrue(dd.getText().contains("org.openntf.example.param=I am the param value"), () -> "initParam value should have contained the example param: " + dd.getText());
					
					// And the one from the web-fragment.xml
					assertTrue(dd.getText().contains("org.openntf.example.fragment.param=I am the param value from a fragment in a JAR"), () -> "initParam value should have contained the example param: " + dd.getText());
				}
				
				WebElement input = form.findElement(By.xpath("input[1]"));
				assertTrue(input.getAttribute("id").endsWith(":appGuyProperty"), () -> input.getAttribute("id"));
				// May be set by previous test
				input.clear();
				input.click();
				input.sendKeys(expected);
				assertEquals(expected, input.getAttribute("value"));
				
				WebElement submit = form.findElement(By.xpath("input[@type='submit']"));
				assertEquals("Refresh", submit.getAttribute("value"));
				submit.click();
				// Give it a bit to do the partial refresh
				TimeUnit.MILLISECONDS.sleep(500);
			}
			{
				
				WebElement form = driver.findElement(By.xpath("//form[1]"));
				
				WebElement span = form.findElement(By.xpath("p/span[1]"));
				assertEquals(expected, span.getText());
			}
			
			// Check for the NSF-defined Facelet library
			{
				WebElement div = driver.findElement(By.cssSelector("div.example-facelet"));
				assertEquals("I am the example facelet", div.getText());
			}
			
			// While here, test the phase listeners
			{
				WebElement dd = driver.findElement(By.xpath("//dt[text()=\"Faces Phase Listener Output\"]/following-sibling::dd[1]"));
				assertTrue(dd.getText().equals("I was set by the Faces listener"));
			}
			{
				WebElement dd = driver.findElement(By.xpath("//dt[text()=\"XPages Phase Listener Output\"]/following-sibling::dd[1]"));
				assertTrue(dd.getText().isEmpty());
			}
		} catch(Exception e) {
			fail("Encountered exception with page source:\n" + driver.getPageSource(), e);
		}
	}
	
	/**
	 * Tests to ensure that a JSF file that doesn't exist leads to a
	 * non-empty 404 page.
	 */
	@ParameterizedTest
	@ArgumentsSource(MainAndModuleProvider.EnumOnly.class)
	@Order(2)
	public void testNotFound(TestDatabase db) {
		Client client = getAnonymousClient();
		WebTarget target = client.target(getRootUrl(null, db) + "/somefakepage.xhtml");
		Response response = target.request().get();
		
		checkResponse(404, response);
		
		String content = response.readEntity(String.class);
		assertFalse(StringUtil.isEmpty(content));
	}
	
	/**
	 * Tests to ensure that the jsf.js resource can be properly loaded.
	 */
	@ParameterizedTest
	@ArgumentsSource(MainAndModuleProvider.EnumOnly.class)
	@Order(3)
	public void testJsfJs(TestDatabase db) {
		Client client = getAnonymousClient();
		WebTarget target = client.target(getRootUrl(null, db) + "/jakarta.faces.resource/faces.js.xhtml?ln=jakarta.faces");
		Response response = target.request().get();
		assertEquals(200, response.getStatus());

		String content = response.readEntity(String.class);
		assertFalse(StringUtil.isEmpty(content));
		
	}
	
	@ParameterizedTest
	@ArgumentsSource(BrowserArgumentsProvider.class)
	@Order(4)
	public void testPrimeFaces(WebDriver driver) {
		driver.get(getRootUrl(driver, TestDatabase.PRIMEFACES) + "/pf.xhtml");

		try {
			WebElement spinner = driver.findElement(By.cssSelector("span.ui-spinner"));
			
			WebElement a = spinner.findElement(By.xpath("a[1]"));
			a.click();
			
			WebElement input = spinner.findElement(By.xpath("input[1]"));
			
			String value = waitFor(() -> input.getAttribute("value"), "1"::equals);
			assertEquals("1", value, () -> "Didn't received expected value with HTML: " + driver.getPageSource());
			
			a.click();
			
			value = waitFor(() -> input.getAttribute("value"), "2"::equals);
			assertEquals("2", value, () -> "Didn't received expected value with HTML: " + driver.getPageSource());
		} catch(Exception e) {
			fail("Encountered exception with page source:\n" + driver.getPageSource(), e);
		}
	}
	
	@ParameterizedTest
	@ArgumentsSource(MainAndModuleProvider.EnumAndBrowser.class)
	@Order(5)
	public void testProgrammaticFacelet(TestDatabase db, WebDriver driver) {
		String expected = "foo" + System.currentTimeMillis();
		driver.get(getRootUrl(driver, db) + "/programmaticFacelet.xhtml?foo=" + expected);

		try {
			WebElement output = driver.findElement(By.cssSelector(".param-output"));
			assertEquals(expected, output.getText());
		} catch(Exception e) {
			fail("Encountered exception with page source:\n" + driver.getPageSource(), e);
		}
	}
}
