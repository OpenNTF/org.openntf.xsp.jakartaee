package it.org.openntf.xsp.jakartaee.el;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import it.org.openntf.xsp.jakartaee.AbstractWebClientTest;
import it.org.openntf.xsp.jakartaee.BrowserArgumentsProvider;

@SuppressWarnings("nls")
public class TestElBasics extends AbstractWebClientTest {
	/**
	 * Tests basic EL bean property resolution.
	 */
	@ParameterizedTest
	@ArgumentsSource(BrowserArgumentsProvider.class)
	public void testBasicFunctionClass(WebDriver driver) {
		driver.get(getRootUrl(driver) + "/el.xsp");
		
		WebElement dd = driver.findElement(By.xpath("//dt[text()=\"#{functionClass.foo}\"]/following-sibling::dd[1]"));
		assertEquals("I am returned from getFoo()", dd.getText());
	}
}
