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
package it.org.openntf.xsp.jakartaee.nsf.concurrency;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import it.org.openntf.xsp.jakartaee.AbstractWebClientTest;
import it.org.openntf.xsp.jakartaee.TestDatabase;
import it.org.openntf.xsp.jakartaee.providers.MainAndModuleProvider;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;

@SuppressWarnings("nls")
public class TestConcurrency extends AbstractWebClientTest {
	@ParameterizedTest
	@ArgumentsSource(MainAndModuleProvider.EnumOnly.class)
	public void testBasics(TestDatabase db) {
		Client client = getAnonymousClient();
		WebTarget target = client.target(getRestUrl(null, db) + "/concurrency");
		Response response = target.request().get();
		
		String output = response.readEntity(String.class);
		
		assertTrue(output.startsWith("Hello from executor\n"), () -> "Received unexpected output: " + output);
		assertTrue(output.contains("CDI is org"), () -> "Received unexpected output: " + output);
		assertTrue(output.contains("Username is: Anonymous"), () -> "Received unexpected output: " + output);
		assertTrue(output.contains("Database is: dev"), () -> "Received unexpected output: " + output);
		assertTrue(output.contains("applicationGuy is: I'm application guy"), () -> "Received unexpected output: " + output);
	}

	@ParameterizedTest
	@ArgumentsSource(MainAndModuleProvider.EnumOnly.class)
	public void testBasicsAuthenticated(TestDatabase db) {
		Client client = getAdminClient();
		WebTarget target = client.target(getRestUrl(null, db) + "/concurrency");
		Response response = target.request().get();
		
		String output = response.readEntity(String.class);
		
		assertTrue(output.startsWith("Hello from executor\n"), () -> "Received unexpected output: " + output);
		assertTrue(output.contains("CDI is org"), () -> "Received unexpected output: " + output);
		assertTrue(output.contains("Username is: CN="), () -> "Received unexpected output: " + output);
		assertTrue(output.contains("Database is: dev"), () -> "Received unexpected output: " + output);
		assertTrue(output.contains("applicationGuy is: I'm application guy"), () -> "Received unexpected output: " + output);
	}

	@ParameterizedTest
	@ArgumentsSource(MainAndModuleProvider.EnumOnly.class)
	public void testScheduled(TestDatabase db) {
		Client client = getAnonymousClient();
		WebTarget target = client.target(getRestUrl(null, TestDatabase.MAIN) + "/concurrency/scheduled");
		Response response = target.request().get();
		
		String output = response.readEntity(String.class);
		
		assertTrue(output.contains("hello from scheduler"), () -> "Received unexpected output: " + output);
	}
	
	@Test
	public void testXPages() {
		Client client = getAnonymousClient();
		WebTarget target = client.target(getRootUrl(null, TestDatabase.MAIN) + "/concurrency.xsp");
		Response response = target.request().get();
		
		String output = response.readEntity(String.class);
		
		assertTrue(output.contains("bean says: Hello from executor"), () -> "Received unexpected output: " + output);
	}

	@ParameterizedTest
	@ArgumentsSource(MainAndModuleProvider.EnumOnly.class)
	public void testAsyncLookup(TestDatabase db) {
		Client client = getAnonymousClient();
		WebTarget target = client.target(getRestUrl(null, db) + "/concurrency/asyncLookup");
		Response response = target.request().get();
		
		String output = response.readEntity(String.class);
		
		assertTrue(output.startsWith("I looked up: "), () -> "Received unexpected output: " + output);
		assertTrue(output.contains("ManagedExecutorService"), () -> "Received unexpected output: " + output);
	}

	@ParameterizedTest
	@ArgumentsSource(MainAndModuleProvider.EnumOnly.class)
	public void testDoubleAsyncLookup(TestDatabase db) {
		Client client = getAnonymousClient();
		WebTarget target = client.target(getRestUrl(null, db) + "/concurrency/doubleAsyncLookup");
		Response response = target.request().get();
		
		String output = response.readEntity(String.class);
		
		assertTrue(output.startsWith("I looked up: "), () -> "Received unexpected output: " + output);
		assertTrue(output.contains("ManagedExecutorService"), () -> "Received unexpected output: " + output);
	}

	@ParameterizedTest
	@ArgumentsSource(MainAndModuleProvider.EnumOnly.class)
	public void testAsyncMethod(TestDatabase db) {
		Client client = getAnonymousClient();
		WebTarget target = client.target(getRestUrl(null, db) + "/concurrency/asyncMethod");
		Response response = target.request().get();
		
		String output = response.readEntity(String.class);
		
		Pattern pattern = Pattern.compile("^I was run on (\\d+)\\nI was run on thread (\\d+)$");
		
		Matcher matcher = pattern.matcher(output);
		
		assertTrue(matcher.matches(), () -> "Received unexpected output: " + output);
		
		String id1 = matcher.group(1);
		String id2 = matcher.group(2);
		
		assertNotEquals(id1, id2, () -> "IDs should not be the same: " + output);
	}

	/**
	 * Tests that a method annotated with {@code @Asynchronous(runAt)} will
	 * run once a second.
	 * 
	 * @param db the db endpoint
	 * @throws InterruptedException not likely
	 * @see <a href="https://github.com/OpenNTF/org.openntf.xsp.jakartaee/issues/704">Issue #704</a>
	 */
	@ParameterizedTest
	@ArgumentsSource(MainAndModuleProvider.EnumOnly.class)
	public void testScheduledMethod(TestDatabase db) throws InterruptedException {
		Client client = getAnonymousClient();
		{
			WebTarget target = client.target(getRestUrl(null, db) + "/concurrency/runScheduled");
			Response response = target.request().post(Entity.text(""));
			checkResponse(200, response);
			
			String output = response.readEntity(String.class);
			assertEquals("ok.", output);
		}
		
		// Wait a few seconds to make sure the counter is incremented
		TimeUnit.SECONDS.sleep(2);
		
		{
			WebTarget target = client.target(getRestUrl(null, db) + "/concurrency/getScheduleRan");
			Response response = target.request().get();
			checkResponse(200, response);
			
			String output = response.readEntity(String.class);
			assertTrue(output.startsWith("Count: "), () -> "Received unexpected output: " + output);
			assertNotEquals("Count: 0", output);
		}
	}
}
