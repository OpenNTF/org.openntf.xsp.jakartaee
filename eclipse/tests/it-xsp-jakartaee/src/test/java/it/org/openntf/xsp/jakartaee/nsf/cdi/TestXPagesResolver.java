package it.org.openntf.xsp.jakartaee.nsf.cdi;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import it.org.openntf.xsp.jakartaee.AbstractWebClientTest;
import it.org.openntf.xsp.jakartaee.BrowserArgumentsProvider;

@SuppressWarnings("nls")
public class TestXPagesResolver extends AbstractWebClientTest {
	@ParameterizedTest
	@ArgumentsSource(BrowserArgumentsProvider.class)
	public void testApplicationScopeResolution(WebDriver driver) {
		driver.get(getRootUrl(driver) + "/beans.xsp");
		
		WebElement dd = driver.findElement(By.xpath("//dt[text()=\"Application Guy\"]/following-sibling::dd[1]"));
		assertTrue(dd.getText().startsWith("I'm application guy at "));
	}
	
	@ParameterizedTest
	@ArgumentsSource(BrowserArgumentsProvider.class)
	public void testSessionAsSigner(WebDriver driver) {
		driver.get(getRootUrl(driver) + "/beans.xsp");
		
		WebElement dd = driver.findElement(By.xpath("//dt[text()=\"#{sessionAsSigner}\"]/following-sibling::dd[1]"));
		assertTrue(dd.getText().startsWith("CN="));
	}
}
