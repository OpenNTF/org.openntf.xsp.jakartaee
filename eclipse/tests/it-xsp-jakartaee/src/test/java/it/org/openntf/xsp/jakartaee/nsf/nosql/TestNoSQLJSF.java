package it.org.openntf.xsp.jakartaee.nsf.nosql;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import it.org.openntf.xsp.jakartaee.AbstractWebClientTest;
import it.org.openntf.xsp.jakartaee.AdminUserAuthenticator;
import it.org.openntf.xsp.jakartaee.BrowserArgumentsProvider;
import it.org.openntf.xsp.jakartaee.TestDatabase;

@SuppressWarnings("nls")
public class TestNoSQLJSF extends AbstractWebClientTest {
	@ParameterizedTest
	@ArgumentsSource(BrowserArgumentsProvider.class)
	@Order(1)
	public void testCrudPage(WebDriver driver) {
		// First, log in to allow for CRUD
		driver.get(getRootUrl(driver, TestDatabase.MAIN) + "?Open&Login");
		{
			WebElement form = driver.findElement(By.tagName("form"));
			WebElement username = form.findElement(By.cssSelector("input[name='Username']"));
			username.click();
			username.sendKeys(AdminUserAuthenticator.USER);
			WebElement password = form.findElement(By.cssSelector("input[name='Password']"));
			password.click();
			password.sendKeys(AdminUserAuthenticator.PASSWORD);
			
			WebElement submit = form.findElement(By.cssSelector("input[type='submit']"));
			submit.click();
			
			assertFalse(driver.manage().getCookies().isEmpty(), () -> driver.getPageSource());
		}
		
		driver.get(getRootUrl(driver, TestDatabase.MAIN) + "/person-list.xhtml");

		String firstName = "Foo";
		String lastName = "Created By JSF " + System.currentTimeMillis();
		try {
			// Create a new document
			WebElement form = driver.findElement(By.tagName("form"));
			{
				WebElement fieldset = form.findElement(By.cssSelector("table + hr + fieldset"));
				
				{
					WebElement dd = fieldset.findElement(By.xpath("//dt[text()=\"First Name\"]/following-sibling::dd[1]"));
					WebElement field = dd.findElement(By.tagName("input"));
					field.click();
					field.sendKeys(firstName);
				}
				{
					WebElement dd = fieldset.findElement(By.xpath("//dt[text()=\"Last Name\"]/following-sibling::dd[1]"));
					WebElement field = dd.findElement(By.tagName("input"));
					field.click();
					field.sendKeys(lastName);
				}
				
				WebElement input = fieldset.findElement(By.cssSelector("input[type=submit]"));
				input.click();
			}
			
			WebElement tr = waitFor(() -> {
				try {
					WebElement f = driver.findElement(By.tagName("form"));
					return f.findElement(By.xpath("//td[text()=\"" + lastName + "\"]/parent::tr"));
				} catch(NoSuchElementException e) {
					return null;
				}
			}, t -> t != null);
			assertNotNull(tr, "Could not find row for created document");
			
			// Now edit that entry
			firstName = "Foo" + System.currentTimeMillis();
			{
				WebElement edit = tr.findElement(By.cssSelector("input[type=submit][value=Edit]"));
				edit.click();
				WebElement fieldset = waitFor(() -> {
					try {
						WebElement f = driver.findElement(By.tagName("form"));
						return f.findElement(By.xpath("//td[text()=\"" + lastName + "\"]/parent::tr/td/fieldset"));
					} catch(NoSuchElementException e) {
						return null;
					}
				}, t -> t != null);
				{
					WebElement dd = fieldset.findElement(By.xpath("//dt[text()=\"First Name\"]/following-sibling::dd[1]"));
					WebElement field = dd.findElement(By.tagName("input"));
					field.click();
					field.clear();
					field.sendKeys(firstName);
				}
				
				WebElement input = fieldset.findElement(By.cssSelector("input[type=submit]"));
				input.click();
			}
			
			// Now find it and make sure it was edited
			String fFirstName = firstName;
			tr = waitFor(() -> {
				try {
					WebElement f = driver.findElement(By.tagName("form"));
					return f.findElement(By.xpath("//td[text()=\"" + fFirstName + "\"]/parent::tr"));
				} catch(NoSuchElementException e) {
					return null;
				}
			}, t -> t != null);
			assertNotNull(tr, "Could not find row for created document");
			
			// Now delete it
			WebElement delete = tr.findElement(By.cssSelector("input[value=Delete]"));
			delete.click();
			
			// Now make sure it's gone
			tr = waitFor(() -> {
				try {
					WebElement f = driver.findElement(By.tagName("form"));
					return f.findElement(By.xpath("//td[text()=\"" + lastName + "\"]/parent::tr"));
				} catch(NoSuchElementException e) {
					return null;
				}
			}, t -> t == null);
			assertNull(tr, "Should have removed row for created document");
		} catch(Exception e) {
			throw new RuntimeException("Encountered exception with page source:\n" + driver.getPageSource(), e);
		}
	}
}
