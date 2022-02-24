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
package it.org.openntf.xsp.jakartaee.nsf.jaxrs;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Map;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.ibm.commons.util.io.json.JsonException;
import com.ibm.commons.util.io.json.JsonJavaFactory;
import com.ibm.commons.util.io.json.JsonParser;

import it.org.openntf.xsp.jakartaee.AbstractWebClientTest;
import it.org.openntf.xsp.jakartaee.BrowserArgumentsProvider;

@SuppressWarnings("nls")
public class TestRestExceptions extends AbstractWebClientTest {
	/**
	 * Tests rest.ExceptionExample, which renders an exception as JSON
	 * @throws JsonException 
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void testJson() throws JsonException {
		Client client = getAnonymousClient();
		WebTarget target = client.target(getRestUrl(null) + "/exceptionExample");
		Response response = target.request().get();
		
		String json = response.readEntity(String.class);
		Map<String, Object> jsonObject = (Map<String, Object>)JsonParser.fromJson(JsonJavaFactory.instance, json);
		
		String message = (String)jsonObject.get("message");
		assertNotNull(message);
		assertEquals("java.lang.RuntimeException: this is an example exception", message);
		
		List<List<String>> stackTrace = (List<List<String>>)jsonObject.get("stackTrace");
		assertNotNull(stackTrace);
		assertEquals(2, stackTrace.size());
		assertEquals("java.lang.RuntimeException: this is an example exception", stackTrace.get(0).get(0));
	}
	
	/**
	 * Tests rest.ExceptionExample#html, which renders an exception as HTML using the stock
	 * XPages error page
	 */
	@ParameterizedTest
	@ArgumentsSource(BrowserArgumentsProvider.class)
	public void testHtml(WebDriver driver) {
		driver.get(getRestUrl(driver) + "/exceptionExample/html");
		
		WebElement span = driver.findElement(By.xpath("//h2[text()=\"Exception\"]/following-sibling::span[1]"));
		assertEquals("this is expected to be rendered as HTML", span.getText());
	}
	
	/**
	 * Tests that a fake endpoint ends up with a 404 and standard error page
	 */
	@ParameterizedTest
	@ArgumentsSource(BrowserArgumentsProvider.class)
	public void testNotFound(WebDriver driver) {
		{
			driver.get(getRestUrl(driver) + "/fakeendpoint");
			
			WebElement span = driver.findElement(By.xpath("//h2[text()=\"Exception\"]/following-sibling::span[1]"));
			assertTrue(span.getText().startsWith("RESTEASY003210: Could not find resource for full path"));
		}
		{
			Client client = getAnonymousClient();
			WebTarget target = client.target(getRestUrl(null) + "/fakeendpoint");
			Response response = target.request().get();
			assertEquals(404, response.getStatus());
		}
	}
}
