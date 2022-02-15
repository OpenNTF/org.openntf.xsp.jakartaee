package it.org.openntf.xsp.jakartaee.nsf.cdi;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import it.org.openntf.xsp.jakartaee.AbstractWebClientTest;
import it.org.openntf.xsp.jakartaee.BrowserArgumentsProvider;

@SuppressWarnings("nls")
public class TestXPagesResolverBundle extends AbstractWebClientTest {
	@ParameterizedTest
	@ArgumentsSource(BrowserArgumentsProvider.class)
	public void testBundleBeanResolution(WebDriver driver) {
		driver.get(getBudleNsfRootUrl(driver) + "/home.xsp");
		
		WebElement dd = driver.findElement(By.xpath("//div[@id='container']/span"));
		assertEquals("Bundle bean says: Hello from bundleBean", dd.getText());
	}
}
