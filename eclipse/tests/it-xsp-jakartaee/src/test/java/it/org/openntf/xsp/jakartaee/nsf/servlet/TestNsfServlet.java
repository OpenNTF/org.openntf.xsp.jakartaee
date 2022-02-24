package it.org.openntf.xsp.jakartaee.nsf.servlet;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

import it.org.openntf.xsp.jakartaee.AbstractWebClientTest;

@SuppressWarnings("nls")
public class TestNsfServlet extends AbstractWebClientTest {
	public static class ServletPathsProvider implements ArgumentsProvider {
		@Override
		public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
			return Stream.of(
				Arguments.of("/xsp/someservlet/dff?bar", "Hello from ExampleServlet. context=/dev/jakartaee.nsf, path=/xsp/someservlet, pathInfo=/dff\n"),
				Arguments.of("/xsp/testme.hello", "Hello from ExampleServlet. context=/dev/jakartaee.nsf, path=/xsp/testme.hello, pathInfo=null\n"),
				Arguments.of("/xsp/testme.hello/dsd", "Hello from ExampleServlet. context=/dev/jakartaee.nsf, path=/xsp/testme.hello, pathInfo=/dsd\n"),
				Arguments.of("/xsp/someservlet?bar", "Hello from ExampleServlet. context=/dev/jakartaee.nsf, path=/xsp/someservlet, pathInfo=null\n")
			);
		}
	}
	
	@ParameterizedTest
	@ArgumentsSource(ServletPathsProvider.class)
	public void testExampleServlet(String path, String expected) {
		Client client = getAnonymousClient();
		WebTarget target = client.target(getRootUrl(null) + path);
		Response response = target.request().get();
		
		assertEquals(expected, response.readEntity(String.class));
	}
}
