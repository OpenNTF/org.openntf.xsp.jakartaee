/**
 * Copyright (c) 2018-2023 Contributors to the XPages Jakarta EE Support Project
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
package it.org.openntf.xsp.jakartaee;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.Response;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.ibm.commons.util.PathUtil;

@SuppressWarnings("nls")
@Testcontainers
public abstract class AbstractWebClientTest {
	
	public static class AnonymousClientProvider implements ArgumentsProvider {
		@Override
		public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
			return Stream.of(Arguments.of(anonymousClient));
		}
	}
	
	private static Client anonymousClient;
	private static Client adminClient;
	
	@BeforeAll
	public static void buildClients() {
		anonymousClient = ClientBuilder.newBuilder().build();
		adminClient = ClientBuilder.newBuilder().register(AdminUserAuthenticator.class).build();
	}
	
	@AfterAll
	public static void tearDownClients() {
		anonymousClient.close();
		adminClient.close();
	}

	public Client getAnonymousClient() {
		return anonymousClient;
	}
	public Client getAdminClient() {
		return adminClient;
	}
	
	public String getExampleContextPath() {
		return "/dev/jakartaee.nsf";
	}
	
	public String getBundleExampleContextPath() {
		return "/dev/jeebundle.nsf";
	}
	
	public String getBaseBundleExampleContextPath() {
		return "/dev/jeebasebundle.nsf";
	}
	
	public String getJsonbExampleContextPath() {
		return "/dev/jsonbconfig.nsf";
	}
	
	public String getWebappContextPath() {
		return "/jeeExample";
	}
	
	public String getRootUrl(WebDriver driver) {
		String host;
		int port;
		if(driver instanceof RemoteWebDriver) {
			host = JakartaTestContainers.CONTAINER_NETWORK_NAME;
			port = 80;
		} else {
			host = JakartaTestContainers.instance.domino.getHost();
			port = JakartaTestContainers.instance.domino.getMappedPort(80);
		}
		
		String context = getExampleContextPath();
		return PathUtil.concat("http://" + host + ":" + port, context, '/');
	}
	
	public String getRestUrl(WebDriver driver) {
		String root = getRootUrl(driver);
		return PathUtil.concat(root, "xsp/app", '/');
	}

	public String getServletRestUrl(WebDriver driver) {
		String host;
		int port;
		if(driver instanceof RemoteWebDriver) {
			host = JakartaTestContainers.CONTAINER_NETWORK_NAME;
			port = 80;
		} else {
			host = JakartaTestContainers.instance.domino.getHost();
			port = JakartaTestContainers.instance.domino.getMappedPort(80);
		}
		
		String context = "/exampleservlet";
		return PathUtil.concat("http://" + host + ":" + port, context, '/');
	}
	
	public String getBundleNsfRootUrl(WebDriver driver) {
		String host;
		int port;
		if(driver instanceof RemoteWebDriver) {
			host = JakartaTestContainers.CONTAINER_NETWORK_NAME;
			port = 80;
		} else {
			host = JakartaTestContainers.instance.domino.getHost();
			port = JakartaTestContainers.instance.domino.getFirstMappedPort();
		}
		
		String context = getBundleExampleContextPath();
		return PathUtil.concat("http://" + host + ":" + port, context, '/');
	}
	
	public String getJsonbConfigRootUrl(WebDriver driver) {
		String host;
		int port;
		if(driver instanceof RemoteWebDriver) {
			host = JakartaTestContainers.CONTAINER_NETWORK_NAME;
			port = 80;
		} else {
			host = JakartaTestContainers.instance.domino.getHost();
			port = JakartaTestContainers.instance.domino.getFirstMappedPort();
		}
		
		String context = getJsonbExampleContextPath();
		return PathUtil.concat("http://" + host + ":" + port, context, '/');
	}
	
	public String getWebappRootUrl(WebDriver driver) {
		String host;
		int port;
		if(driver instanceof RemoteWebDriver) {
			host = JakartaTestContainers.CONTAINER_NETWORK_NAME;
			port = 80;
		} else {
			host = JakartaTestContainers.instance.domino.getHost();
			port = JakartaTestContainers.instance.domino.getMappedPort(80);
		}
		
		String context = getWebappContextPath();
		return PathUtil.concat("http://" + host + ":" + port, context, '/');
	}
	
	public String getWebappContextualRootUrl(WebDriver driver) {
		String host;
		int port;
		if(driver instanceof RemoteWebDriver) {
			host = JakartaTestContainers.CONTAINER_NETWORK_NAME;
			port = 80;
		} else {
			host = JakartaTestContainers.instance.domino.getHost();
			port = JakartaTestContainers.instance.domino.getMappedPort(80);
		}
		
		String context = PathUtil.concat(getExampleContextPath(), getWebappContextPath(), '/');
		return PathUtil.concat("http://" + host + ":" + port, context, '/');
	}
	
	public String getBundleNsfRestUrl(WebDriver driver) {
		String root = getBundleNsfRootUrl(driver);
		return PathUtil.concat(root, "xsp/app", '/');
	}
	
	public String getJsonbConfigRestUrl(WebDriver driver) {
		String root = getJsonbConfigRootUrl(null);
		return PathUtil.concat(root, "xsp/app", '/');
	}
	
	public String getBaseBudleNsfRootUrl(WebDriver driver) {
		String host;
		int port;
		if(driver instanceof RemoteWebDriver) {
			host = JakartaTestContainers.CONTAINER_NETWORK_NAME;
			port = 80;
		} else {
			host = JakartaTestContainers.instance.domino.getHost();
			port = JakartaTestContainers.instance.domino.getMappedPort(80);
		}
		
		String context = getBaseBundleExampleContextPath();
		return PathUtil.concat("http://" + host + ":" + port, context, '/');
	}

	protected void checkResponse(int expectedCode, Response response) {
		assertEquals(expectedCode, response.getStatus(), () -> "Received unexpected code " + response.getStatus() + ": " + response.readEntity(String.class));
	}
}
