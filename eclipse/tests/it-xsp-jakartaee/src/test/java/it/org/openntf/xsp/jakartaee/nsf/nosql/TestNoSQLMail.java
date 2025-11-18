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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

import it.org.openntf.xsp.jakartaee.AbstractWebClientTest;
import it.org.openntf.xsp.jakartaee.TestDatabase;
import it.org.openntf.xsp.jakartaee.providers.MainAndModuleProvider;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;

@SuppressWarnings("nls")
public class TestNoSQLMail extends AbstractWebClientTest {
	public static class EnumAndBooleanProvider implements ArgumentsProvider {
		@Override
		public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
			
			return new MainAndModuleProvider.EnumAndBrowser().provideArguments(context)
				.map(args -> args.get()[0])
				.flatMap(e ->
					Stream.of(true, false)
						.map(page -> Arguments.of(e, page))
				);
		}
	}
	
	@ParameterizedTest
	@ArgumentsSource(EnumAndBooleanProvider.class)
	public void testSendMailNoSave(TestDatabase db, boolean save) throws IOException, InterruptedException {
		Client client = getAdminClient();
		
		String subject = "I am the subject without saving " + System.currentTimeMillis();
		{
			{
				WebTarget target = client.target(getRestUrl(null, db) + "/memo?save=" + save); //$NON-NLS-1$
				
				JsonObject payload = Json.createObjectBuilder()
						.add("subject", subject)
						.add("body", "I am the body")
						.add("sendTo", Json.createArrayBuilder().add("Foo Fooson"))
						.build();
				
				Response response = target.request().post(Entity.json(payload));
				checkResponse(200, response);
				JsonObject json = response.readEntity(JsonObject.class);
				assertEquals(subject, json.getString("subject", null));
			}
			// Check the main DB to make sure it was or wasn't saved
			{
				WebTarget target = client.target(getRestUrl(null, db) + "/memo?subject=" + URLEncoder.encode(subject, StandardCharsets.UTF_8));
				Response response = target.request().get();
				checkResponse(200, response);
				JsonArray results = response.readEntity(JsonArray.class);
				if(save) {
					assertFalse(results.isEmpty());
					JsonObject memo = results.getJsonObject(0);
					assertEquals(subject, memo.getString("subject", null));
				} else {
					assertTrue(results.isEmpty());
				}
			}
			TimeUnit.SECONDS.sleep(1);
			// Check to make sure it's stored in the target
			{
				WebTarget target = client.target(getRestUrl(null, db) + "/memo/mailfile?subject=" + URLEncoder.encode(subject, StandardCharsets.UTF_8));
				Response response = target.request().get();
				checkResponse(200, response);
				JsonArray results = response.readEntity(JsonArray.class);
				assertFalse(results.isEmpty());
				JsonObject memo = results.getJsonObject(0);
				assertEquals(subject, memo.getString("subject", null));
			}
		}
		
	}
}