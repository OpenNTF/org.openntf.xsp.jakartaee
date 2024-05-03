package it.org.openntf.xsp.jakartaee.nsf.jsf;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import it.org.openntf.xsp.jakartaee.AbstractWebClientTest;
import it.org.openntf.xsp.jakartaee.BrowserArgumentsProvider;
import it.org.openntf.xsp.jakartaee.TestDatabase;

/**
 * Test for contextual beans added for issue #455
 * 
 * @see <a href="https://github.com/OpenNTF/org.openntf.xsp.jakartaee/issues/455">Issue #455</a>
 */
@SuppressWarnings("nls")
public class TestJsfContextBeans extends AbstractWebClientTest {
	@ParameterizedTest
	@ArgumentsSource(BrowserArgumentsProvider.class)
	public void testHelloPage(WebDriver driver) {
		driver.get(getRootUrl(driver, TestDatabase.MAIN) + "/httpContext.xhtml");

		try {
			{
				WebElement dd = driver.findElement(By.xpath("//dt[text()=\"httpContextGuy.request.contextPath\"]/following-sibling::dd[1]"));
				assertEquals(TestDatabase.MAIN.getContextPath(), dd.getText(), () -> "Unexpected source: " + driver.getPageSource());
			}
			{
				WebElement dd = driver.findElement(By.xpath("//dt[text()=\"httpContextGuy.response.status\"]/following-sibling::dd[1]"));
				assertEquals("200", dd.getText(), () -> "Unexpected source: " + driver.getPageSource());
			}
			{
				WebElement dd = driver.findElement(By.xpath("//dt[text()=\"httpContextGuy.context.serverInfo\"]/following-sibling::dd[1]"));
				assertEquals("XPages-Domino Web Container", dd.getText(), () -> "Unexpected source: " + driver.getPageSource());
			}
		} catch(Exception e) {
			fail("Encountered exception with page source:\n" + driver.getPageSource(), e);
		}
	}
}