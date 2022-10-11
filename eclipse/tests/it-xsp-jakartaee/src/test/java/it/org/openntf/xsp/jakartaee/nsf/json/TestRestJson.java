/**
 * Copyright © 2018-2022 Contributors to the XPages Jakarta EE Support Project
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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.StringReader;
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
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;

@SuppressWarnings("nls")
public class TestRestJson extends AbstractWebClientTest {
	
	@ParameterizedTest
	@ArgumentsSource(AnonymousClientProvider.class)
	public void testJsonp(Client client) {
		WebTarget target = client.target(getRestUrl(null) + "/jsonExample/jsonp");
		Response response = target.request().get();
		
		String json = response.readEntity(String.class);
		JsonObject jsonObject = Json.createReader(new StringReader(json)).readObject();
		assertEquals("baz", jsonObject.getString("bar"));
	}
	
	@ParameterizedTest
	@ArgumentsSource(AnonymousClientProvider.class)
	public void testJsonb(Client client) {
		WebTarget target = client.target(getRestUrl(null) + "/jsonExample");
		Response response = target.request().get();
		
		String json = response.readEntity(String.class);
		JsonObject jsonObject = Json.createReader(new StringReader(json)).readObject();
		assertEquals("bar", jsonObject.getString("foo"));
	}
	
	@ParameterizedTest
	@ArgumentsSource(AnonymousClientProvider.class)
	public void testJsonbCdi(Client client) {
		WebTarget target = client.target(getRestUrl(null) + "/jsonExample/jsonb");
		Response response = target.request().get();
		
		String json = response.readEntity(String.class);
		JsonObject jsonObject = Json.createReader(new StringReader(json)).readObject();
		String jsonMessage = jsonObject.getString("jsonMessage");
		assertTrue(jsonMessage.startsWith("I'm application guy at "));
	}
	
	@Test
	public void testJsonMultithread() throws InterruptedException {
		int runCount = 150;
		ExecutorService exec = Executors.newFixedThreadPool(runCount);
		try {
			CountDownLatch latch = new CountDownLatch(runCount);
			for(int i = 0; i < runCount; i++) {
				exec.submit(() -> {
					ThreadLocalRandom rand = ThreadLocalRandom.current();
					ClientBuilder builder = ClientBuilder.newBuilder();
					if(rand.nextBoolean()) {
						builder.register(AdminUserAuthenticator.class);
					}
					Client client = builder.build();
					try {
						testJsonp(client);
						TimeUnit.MILLISECONDS.sleep(rand.nextInt(500));
						testJsonb(client);
						TimeUnit.MILLISECONDS.sleep(rand.nextInt(500));
						testJsonbCdi(client);
					} catch (InterruptedException e) {
						e.printStackTrace();
					} finally {
						latch.countDown();
						client.close();
					}
				});
			}
			latch.await();
		} finally {
			exec.shutdownNow();
			exec.awaitTermination(1, TimeUnit.MINUTES);
		}
	}
}
