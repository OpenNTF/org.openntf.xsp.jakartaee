package it.org.openntf.xsp.jakartaee.nsfmodule;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import it.org.openntf.xsp.jakartaee.AbstractWebClientTest;
import it.org.openntf.xsp.jakartaee.TestDatabase;

/**
 * Tests behavior of accessing file resources in Jakarta NSF module apps
 */
@SuppressWarnings("nls")
public class TestNSFModuleResources extends AbstractWebClientTest {
	/**
	 * Tests that file resources are accessible by HTTP
	 */
	@Test
	public void testFileResource() {
		var client = getAnonymousClient();
		
		var target = client.target(getRootUrl(null, TestDatabase.MAIN_MODULE) + "/chat.html");
		var response = target.request().get();
		
		checkResponse(200, response);
		String contentType = response.getHeaderString("Content-Type");
		assertTrue(contentType.startsWith("text/html"), () -> "Unexpected content type: " + contentType);
		String content = response.readEntity(String.class);
		assertTrue(content.contains("<!DOCTYPE html>"), () -> "Unexpected content: " + content);
	}
	
	/**
	 * Tests that stylesheets are accessible by HTTP
	 */
	@Test
	public void testStyleSheet() {
		var client = getAnonymousClient();
		
		var target = client.target(getRootUrl(null, TestDatabase.MAIN_MODULE) + "/classic.css");
		var response = target.request().get();
		
		checkResponse(200, response);
		String contentType = response.getHeaderString("Content-Type");
		assertTrue(contentType.startsWith("text/css"), () -> "Unexpected content type: " + contentType);
		String content = response.readEntity(String.class);
		assertTrue(content.contains("font-family: sans-serif"), () -> "Unexpected content: " + content);
	}

	/**
	 * Tests that image resources are accessible by URL
	 */
	@Test
	public void testImageResources() throws IOException {
		var client = getAnonymousClient();
		
		var target = client.target(getRootUrl(null, TestDatabase.MAIN_MODULE) + "/tango/document-save.png");
		var response = target.request().get();

		checkResponse(200, response);
		String contentType = response.getHeaderString("Content-Type");
		assertTrue(contentType.startsWith("image/png"), () -> "Unexpected content type: " + contentType);
		var content = response.readEntity(byte[].class);
		assertTrue(content.length > 8, "Content should have non-zero length");
		
		byte[] expected;
		try(var is = getClass().getResourceAsStream("/tango_document-save.png")) {
			expected = is.readAllBytes();
		}
		
		assertArrayEquals(expected, content);
	}
	
	/**
	 * Tests that files in WebContent are accessible by URL
	 */
	@Test
	public void testWebContentResource() {
		var client = getAnonymousClient();
		
		var target = client.target(getRootUrl(null, TestDatabase.MAIN_MODULE) + "/lipsum.txt");
		var response = target.request().get();
		
		checkResponse(200, response);
		String contentType = response.getHeaderString("Content-Type");
		assertTrue(contentType.startsWith("text/plain"), () -> "Unexpected content type: " + contentType);
		String content = response.readEntity(String.class);
		assertTrue(content.contains("Maecenas nunc metus"), () -> "Unexpected content: " + content);
	}

	/**
	 * Tests that Java files in Code/Java are not accessible by URL
	 */
	@Test
	public void testCodeJava() {
		var client = getAnonymousClient();
		
		var target = client.target(getRootUrl(null, TestDatabase.MAIN_MODULE) + "/bean/ApplicationGuy.java");
		var response = target.request().get();
		
		checkResponse(404, response);
		String content = response.readEntity(String.class);
		assertFalse(content.contains("public class ApplicationGuy "), () -> "Unexpected content: " + content);
	}

	/**
	 * Tests that Themes are not accessible by URL
	 */
	@Test
	public void testTheme() {
		var client = getAnonymousClient();
		
		var target = client.target(getRootUrl(null, TestDatabase.MAIN_MODULE) + "/App.theme");
		var response = target.request().get();
		
		checkResponse(404, response);
		String content = response.readEntity(String.class);
		assertFalse(content.contains("xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""), () -> "Unexpected content: " + content);
	}
}
