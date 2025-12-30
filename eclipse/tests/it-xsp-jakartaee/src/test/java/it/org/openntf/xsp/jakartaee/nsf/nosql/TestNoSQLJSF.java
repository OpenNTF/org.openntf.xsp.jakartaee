/**
 * Copyright (c) 2018-2025 Contributors to the XPages Jakarta EE Support Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package it.org.openntf.xsp.jakartaee.nsf.nosql;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import it.org.openntf.xsp.jakartaee.AbstractWebClientTest;
import it.org.openntf.xsp.jakartaee.AdminUserAuthenticator;
import it.org.openntf.xsp.jakartaee.TestDatabase;
import it.org.openntf.xsp.jakartaee.providers.MainAndModuleProvider;


@SuppressWarnings("nls")
public class TestNoSQLJSF extends AbstractWebClientTest {
	@ParameterizedTest
	@ArgumentsSource(MainAndModuleProvider.EnumAndBrowser.class)
	public void testCrudPage(TestDatabase db, WebDriver driver) {
		// First, log in to allow for CRUD
		// Intentionally always MAIN for the login URL
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
		
		driver.get(getRootUrl(driver, db) + "/person-list.xhtml");

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
				} catch(NoSuchElementException | StaleElementReferenceException e) {
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
					} catch(NoSuchElementException | StaleElementReferenceException e) {
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
				} catch(NoSuchElementException | StaleElementReferenceException e) {
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
				} catch(NoSuchElementException | StaleElementReferenceException e) {
					return null;
				}
			}, t -> t == null);
			assertNull(tr, "Should have removed row for created document");
		} catch(Exception e) {
			fail("Encountered exception with page source:\n" + driver.getPageSource(), e);
		}
	}
}
