package it.org.openntf.xsp.jakartaee.mvc;

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
