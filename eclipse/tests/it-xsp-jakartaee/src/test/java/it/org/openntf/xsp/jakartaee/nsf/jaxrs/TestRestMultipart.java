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
package it.org.openntf.xsp.jakartaee.nsf.jaxrs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.text.MessageFormat;

import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataOutput;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import it.org.openntf.xsp.jakartaee.AbstractWebClientTest;
import it.org.openntf.xsp.jakartaee.TestDatabase;
import it.org.openntf.xsp.jakartaee.providers.MainAndModuleProvider;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@SuppressWarnings("nls")
public class TestRestMultipart extends AbstractWebClientTest {
	@ParameterizedTest
	@ArgumentsSource(MainAndModuleProvider.EnumOnly.class)
	public void testMultipart(TestDatabase db) {
		Client client = getAnonymousClient();
		WebTarget target = client.target(getRestUrl(null, db) + "/restMultipart");
		
		String name = "Foo" + System.nanoTime();
		byte[] data = "hey hey".getBytes();
		
		MultipartFormDataOutput payload = new MultipartFormDataOutput();
		payload.addFormData("name", name, MediaType.TEXT_PLAIN_TYPE);
		payload.addFormData("part", data, MediaType.APPLICATION_OCTET_STREAM_TYPE);
		
		Response response = target.request()
			.accept(MediaType.APPLICATION_JSON_TYPE)
			.post(Entity.entity(payload, MediaType.MULTIPART_FORM_DATA_TYPE));
		checkResponse(200, response);
		String result = response.readEntity(String.class);
		String expected = MessageFormat.format("You sent me name={0} and a part of {1} bytes", name, data.length);
		assertEquals(expected, result);
	}
}
