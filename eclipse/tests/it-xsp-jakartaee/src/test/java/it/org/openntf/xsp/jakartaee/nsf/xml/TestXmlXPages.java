package it.org.openntf.xsp.jakartaee.nsf.xml;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import it.org.openntf.xsp.jakartaee.AbstractWebClientTest;
import it.org.openntf.xsp.jakartaee.BrowserArgumentsProvider;

@SuppressWarnings("nls")
public class TestXmlXPages extends AbstractWebClientTest {
	/**
	 * Tests basic EL bean property resolution.
	 */
	@ParameterizedTest
	@ArgumentsSource(BrowserArgumentsProvider.class)
	public void testXmlXPage(WebDriver driver) {
		driver.get(getRootUrl(driver) + "/jaxb.xsp");
		
		WebElement span = driver.findElement(By.xpath("//span[@style=\"font-family: monospace; whitespace: pre-wrap\"]"));
		assertTrue(
			span.getText().startsWith("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?> <application-guy>"),
			() -> "Got unexpected content: " + span.getText()
		);
	}
}
