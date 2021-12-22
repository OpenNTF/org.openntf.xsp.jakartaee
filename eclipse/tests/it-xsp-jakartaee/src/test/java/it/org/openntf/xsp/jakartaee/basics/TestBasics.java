package it.org.openntf.xsp.jakartaee.basics;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import it.org.openntf.xsp.jakartaee.AbstractWebClientTest;
import it.org.openntf.xsp.jakartaee.BrowserArgumentsProvider;

@SuppressWarnings("nls")
public class TestBasics extends AbstractWebClientTest {
	/**
	 * Tests that the main frameset page, before any Java code, is loaded.
	 */
	@ParameterizedTest
	@ArgumentsSource(BrowserArgumentsProvider.class)
	public void testFrameset(WebDriver driver) {
		driver.get(getRootUrl(driver));
		
		assertDoesNotThrow(() -> By.xpath("//frameset"));
	}
}
