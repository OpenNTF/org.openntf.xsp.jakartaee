package it.org.openntf.xsp.jakartaee.mvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import it.org.openntf.xsp.jakartaee.AbstractWebClientTest;
import it.org.openntf.xsp.jakartaee.BrowserArgumentsProvider;

@SuppressWarnings("nls")
public class TestMvcExceptions extends AbstractWebClientTest {
	@ParameterizedTest
	@ArgumentsSource(BrowserArgumentsProvider.class)
	public void testHtml(WebDriver driver) {
		driver.get(getRestUrl(driver) + "/mvc/exception");
		
		WebElement span = driver.findElement(By.xpath("//h2[text()=\"Exception\"]/following-sibling::span[1]"));
		assertEquals("I am an exception from an MVC resource", span.getText());
	}
	
	/**
	 * Tests that a fake endpoint ends up with a 404 and standard error page
	 */
	@ParameterizedTest
	@ArgumentsSource(BrowserArgumentsProvider.class)
	public void testNotFound(WebDriver driver) {
		{
			driver.get(getRestUrl(driver) + "/mvc/notFound");
			
			WebElement span = driver.findElement(By.xpath("//h2[text()=\"Exception\"]/following-sibling::span[1]"));
			assertTrue(span.getText().startsWith("I am a programmatic not-found exception from MVC"));
		}
		{
			Client client = getAnonymousClient();
			WebTarget target = client.target(getRestUrl(null) + "/mvc/notFound");
			Response response = target.request().get();
			assertEquals(404, response.getStatus());
		}
	}
}
