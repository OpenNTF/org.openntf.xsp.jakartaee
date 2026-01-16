/**
 * Copyright (c) 2018-2026 Contributors to the XPages Jakarta EE Support Project
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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebDriver;

import com.ibm.commons.util.PathUtil;

@SuppressWarnings("nls")
public abstract class AbstractWebClientTest {
	
	public static class AnonymousClientProvider implements ArgumentsProvider {
		@Override
		public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
			Client client = ClientBuilder.newClient();
			anonymousClients.add(client);
			return Stream.of(Arguments.of(client));
		}
	}
	
	private static List<Client> anonymousClients = new ArrayList<>();
	private static List<Client> adminClients = new ArrayList<>();
	
	@AfterAll
	public static void tearDownClients() {
		anonymousClients.forEach(Client::close);
		adminClients.forEach(Client::close);
	}

	public Client getAnonymousClient() {
		Client client = ClientBuilder.newClient();
		anonymousClients.add(client);
		return client;
	}
	public Client getAdminClient() {
		Client client = ClientBuilder.newBuilder().register(AdminUserAuthenticator.class).build();
		adminClients.add(client);
		return client;
	}
	
	public String getRootUrl(WebDriver driver, TestDatabase db) {
		String host;
		int port;
		if(driver instanceof RemoteWebDriver) {
			host = JakartaTestContainers.CONTAINER_NETWORK_NAME;
			port = 80;
		} else {
			host = JakartaTestContainers.instance.domino.getHost();
			port = JakartaTestContainers.instance.domino.getMappedPort(80);
		}
		
		String context = db.getContextPath();
		return PathUtil.concat("http://" + host + ":" + port, context, '/');
	}
	
	public String getRestUrl(WebDriver driver, TestDatabase db) {
		String root = getRootUrl(driver, db);
		String path = db.isNsf() ? "xsp/app" : "app";
		return PathUtil.concat(root, path, '/');
	}

	public String getServletRestUrl(WebDriver driver, String context) {
		String host;
		int port;
		if(driver instanceof RemoteWebDriver) {
			host = JakartaTestContainers.CONTAINER_NETWORK_NAME;
			port = 80;
		} else {
			host = JakartaTestContainers.instance.domino.getHost();
			port = JakartaTestContainers.instance.domino.getMappedPort(80);
		}
		
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
		
		String context = PathUtil.concat(TestDatabase.MAIN.getContextPath(), TestDatabase.OSGI_WEBAPP.getContextPath(), '/');
		return PathUtil.concat("http://" + host + ":" + port, context, '/');
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
		
		String context = TestDatabase.BUNDLEBASE.getContextPath();
		return PathUtil.concat("http://" + host + ":" + port, context, '/');
	}

	protected void checkResponse(int expectedCode, Response response) {
		assertEquals(expectedCode, response.getStatus(), () -> "Received unexpected code " + response.getStatus() + ": " + response.readEntity(String.class));
	}
	
	protected <T> T waitFor(Supplier<T> supplier, Predicate<T> condition) throws InterruptedException {
		T result = null;
		for(int i = 0; i < 1000; i++) {
			result = supplier.get();
			if(condition.test(result)) {
				return result;
			}
			TimeUnit.MILLISECONDS.sleep(10);
		}
		fail("Timed out waiting on condition");
		return null;
	}
	
	protected static void assertInstantsCloseEnough(String expected, String actual) {
		Instant expectedInst = Instant.parse(expected);
		Instant actualInst = Instant.parse(actual);
		assertTrue(Math.abs(expectedInst.toEpochMilli() - actualInst.toEpochMilli()) <= 20, () -> "Expected " + actual + " to be within 20 ms of " + expected); 
	}
}
