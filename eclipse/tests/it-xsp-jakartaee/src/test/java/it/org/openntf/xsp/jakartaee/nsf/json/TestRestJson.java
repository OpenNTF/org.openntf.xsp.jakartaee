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
package it.org.openntf.xsp.jakartaee.nsf.json;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import it.org.openntf.xsp.jakartaee.AbstractWebClientTest;
import it.org.openntf.xsp.jakartaee.AdminUserAuthenticator;
import it.org.openntf.xsp.jakartaee.TestDatabase;
import it.org.openntf.xsp.jakartaee.providers.MainAndModuleProvider;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;

@SuppressWarnings("nls")
public class TestRestJson extends AbstractWebClientTest {
	
	@ParameterizedTest
	@ArgumentsSource(MainAndModuleProvider.EnumAndAnonymousClient.class)
	public void testJsonp(TestDatabase db, Client client) {
		WebTarget target = client.target(getRestUrl(null, db) + "/jsonExample/jsonp");
		Response response = target.request().get();
		
		String json = response.readEntity(String.class);
		JsonObject jsonObject = Json.createReader(new StringReader(json)).readObject();
		assertEquals("baz", jsonObject.getString("bar"));
	}
	
	@ParameterizedTest
	@ArgumentsSource(MainAndModuleProvider.EnumAndAnonymousClient.class)
	public void testJsonb(TestDatabase db, Client client) {
		WebTarget target = client.target(getRestUrl(null, db) + "/jsonExample");
		Response response = target.request().get();
		
		String json = response.readEntity(String.class);
		JsonObject jsonObject = Json.createReader(new StringReader(json)).readObject();
		assertEquals("bar", jsonObject.getString("foo"));
	}
	
	@ParameterizedTest
	@ArgumentsSource(MainAndModuleProvider.EnumAndAnonymousClient.class)
	public void testJsonbCdi(TestDatabase db, Client client) {
		WebTarget target = client.target(getRestUrl(null, db) + "/jsonExample/jsonb");
		Response response = target.request().get();
		
		String json = response.readEntity(String.class);
		try {
			JsonObject jsonObject = Json.createReader(new StringReader(json)).readObject();
			String jsonMessage = jsonObject.getString("jsonMessage");
			assertTrue(jsonMessage.startsWith("I'm application guy at "));
		} catch(Exception e) {
			fail("Encountered exception parsing " + json, e);
		}
	}
	
	@Test
	public void testJsonMultithread() throws InterruptedException {
		int runCount = 150;
		ExecutorService exec = Executors.newFixedThreadPool(runCount);
		try {
			CountDownLatch latch = new CountDownLatch(runCount);
			List<Throwable> failures = Collections.synchronizedList(new ArrayList<>());
			for(int i = 0; i < runCount; i++) {
				exec.submit(() -> {
					try {
						for(TestDatabase db : MainAndModuleProvider.VALS) {
							ThreadLocalRandom rand = ThreadLocalRandom.current();
							ClientBuilder builder = ClientBuilder.newBuilder();
							if(rand.nextBoolean()) {
								builder.register(AdminUserAuthenticator.class);
							}
							
							Client client = builder.build();
							try {
								testJsonp(db, client);
								TimeUnit.MILLISECONDS.sleep(rand.nextInt(500));
								testJsonb(db, client);
								TimeUnit.MILLISECONDS.sleep(rand.nextInt(500));
								testJsonbCdi(db, client);
							} catch (InterruptedException e) {
								e.printStackTrace();
							} catch(Throwable t) {
								failures.add(t);
							} finally {
								client.close();
							}
						}
					} finally {
						latch.countDown();
					}
				});
			}
			latch.await();
			
			assertIterableEquals(Collections.emptyList(), failures);
		} finally {
			exec.shutdownNow();
			exec.awaitTermination(1, TimeUnit.MINUTES);
		}
	}
}
