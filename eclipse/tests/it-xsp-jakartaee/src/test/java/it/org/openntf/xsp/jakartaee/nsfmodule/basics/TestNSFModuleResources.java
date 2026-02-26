package it.org.openntf.xsp.jakartaee.nsfmodule.basics;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

import org.junit.jupiter.api.Test;

import it.org.openntf.xsp.jakartaee.AbstractWebClientTest;
import it.org.openntf.xsp.jakartaee.TestDatabase;
import jakarta.ws.rs.core.HttpHeaders;

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
		String contentType = response.getHeaderString(HttpHeaders.CONTENT_TYPE);
		assertTrue(contentType.startsWith("text/html"), () -> "Unexpected content type: " + contentType);
		String content = response.readEntity(String.class);
		assertTrue(content.contains("<!DOCTYPE html>"), () -> "Unexpected content: " + content);
	}
	
	/**
	 * Tests that file resources are accessible by HTTP and are GZIP'd when asked
	 */
	@Test
	public void testFileResourceGzip() throws IOException {
		var client = getAnonymousClient();
		
		var target = client.target(getRootUrl(null, TestDatabase.MAIN_MODULE) + "/chat.html");
		var response = target.request()
			.header(HttpHeaders.ACCEPT_ENCODING, "gzip")
			.get();
		
		checkResponse(200, response);
		String contentType = response.getHeaderString(HttpHeaders.CONTENT_TYPE);
		assertTrue(contentType.startsWith("text/html"), () -> "Unexpected content type: " + contentType);
		String contentEncoding = response.getHeaderString(HttpHeaders.CONTENT_ENCODING);
		assertEquals("gzip", contentEncoding);
		var is = response.readEntity(InputStream.class);
		String content;
		try(var zip = new GZIPInputStream(is)) {
			content = new String(zip.readAllBytes());
		}
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
		String contentType = response.getHeaderString(HttpHeaders.CONTENT_TYPE);
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
		String contentType = response.getHeaderString(HttpHeaders.CONTENT_TYPE);
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
		String contentType = response.getHeaderString(HttpHeaders.CONTENT_TYPE);
		assertTrue(contentType.startsWith("text/plain"), () -> "Unexpected content type: " + contentType);
		String content = response.readEntity(String.class);
		assertTrue(content.contains("Maecenas nunc metus"), () -> "Unexpected content: " + content);
	}
	
	/**
	 * Tests that files in WebContent are accessible by URL and are GZIP'd when asked
	 */
	@Test
	public void testWebContentResourceGzip() throws IOException {
		var client = getAnonymousClient();
		
		var target = client.target(getRootUrl(null, TestDatabase.MAIN_MODULE) + "/lipsum.txt");
		var response = target.request()
			.header(HttpHeaders.ACCEPT_ENCODING, "gzip")
			.get();
		
		checkResponse(200, response);
		String contentType = response.getHeaderString(HttpHeaders.CONTENT_TYPE);
		assertTrue(contentType.startsWith("text/plain"), () -> "Unexpected content type: " + contentType);
		String contentEncoding = response.getHeaderString(HttpHeaders.CONTENT_ENCODING);
		assertEquals("gzip", contentEncoding);
		var is = response.readEntity(InputStream.class);
		String content;
		try(var zip = new GZIPInputStream(is)) {
			content = new String(zip.readAllBytes());
		}
		assertTrue(content.contains("Maecenas nunc metus"), () -> "Unexpected content: " + content);
	}
	
	/**
	 * Tests that files in WebContent are accessible by URL and that
	 * tiny ones are not gzip'd
	 */
	@Test
	public void testTinyWebContentResource() {
		var client = getAnonymousClient();
		
		var target = client.target(getRootUrl(null, TestDatabase.MAIN_MODULE) + "/tinyfile.txt");
		var response = target.request()
			.header(HttpHeaders.ACCEPT_ENCODING, "gzip")
			.get();
		
		checkResponse(200, response);
		String contentType = response.getHeaderString(HttpHeaders.CONTENT_TYPE);
		assertTrue(contentType.startsWith("text/plain"), () -> "Unexpected content type: " + contentType);
		String contentEncoding = response.getHeaderString(HttpHeaders.CONTENT_ENCODING);
		assertNull(contentEncoding);
		String content = response.readEntity(String.class);
		assertEquals("I am a non-gzip file", content);
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
	
	/**
	 * Tests that resources in META-INF/resources in JARs are loaded
	 */
	@Test
	public void testJarMetaResource() {
		var client = getAnonymousClient();
		
		var target = client.target(getRootUrl(null, TestDatabase.MAIN_MODULE) + "/jarlipsum.txt");
		var response = target.request().get();
		
		checkResponse(200, response);
		String content = response.readEntity(String.class);
		
		assertTrue(content.contains("Maecenas nunc metus"), () -> "Unexpected content: " + content);
	}
	
	/**
	 * Tests that resources in META-INF/resources in JARs are loaded and gzip'd when requested
	 */
	@Test
	public void testJarMetaResourceGzip() throws IOException {
		var client = getAnonymousClient();
		
		var target = client.target(getRootUrl(null, TestDatabase.MAIN_MODULE) + "/jarlipsum.txt");
		var response = target.request()
			.acceptEncoding("gzip")
			.get();
		
		checkResponse(200, response);
		String contentEncoding = response.getHeaderString(HttpHeaders.CONTENT_ENCODING);
		assertEquals("gzip", contentEncoding);
		var is = response.readEntity(InputStream.class);
		String content;
		try(var zip = new GZIPInputStream(is)) {
			content = new String(zip.readAllBytes());
		}
		assertTrue(content.contains("Maecenas nunc metus"), () -> "Unexpected content: " + content);
	}
}
