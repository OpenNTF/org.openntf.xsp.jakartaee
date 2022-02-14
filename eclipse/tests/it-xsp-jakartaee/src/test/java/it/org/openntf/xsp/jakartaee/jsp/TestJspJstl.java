package it.org.openntf.xsp.jakartaee.jsp;

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
public class TestJspJstl extends AbstractWebClientTest {
	
	@ParameterizedTest
	@ArgumentsSource(BrowserArgumentsProvider.class)
	public void testHelloPage(WebDriver driver) {
		driver.get(getRootUrl(driver) + "/hello.jsp");
		
		WebElement p = driver.findElement(By.xpath("//p[1]"));
		assertTrue(p.getText().startsWith("My CDI Bean is: I'm request guy"), () -> p.getText());
		
		WebElement dd = driver.findElement(By.xpath("//fieldset/p"));
		assertEquals("I was sent: Value sent into the tag", dd.getText());
	}
}
