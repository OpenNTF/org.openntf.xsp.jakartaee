package it.org.openntf.xsp.jakartaee.nsf.jsf;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.openqa.selenium.WebDriver;

import it.org.openntf.xsp.jakartaee.AbstractWebClientTest;
import it.org.openntf.xsp.jakartaee.BrowserArgumentsProvider;
import it.org.openntf.xsp.jakartaee.TestDatabase;

@SuppressWarnings("nls")
public class TestJsfShowcase extends AbstractWebClientTest {
	@ParameterizedTest
	@ArgumentsSource(BrowserArgumentsProvider.class)
	public void testTheming(WebDriver driver) {
		driver.get(getRootUrl(driver, TestDatabase.PRIMEFACES_SHOWCASE) + "/theming.xhtml");
		
		String html = driver.getPageSource();
		assertTrue(html.contains("Give a name to your theme"), () -> "Unexpected HTML: " + html);
	}
}
