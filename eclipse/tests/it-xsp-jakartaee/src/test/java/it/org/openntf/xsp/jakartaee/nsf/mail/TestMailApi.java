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
package it.org.openntf.xsp.jakartaee.nsf.mail;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
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
public class TestMailApi extends AbstractWebClientTest {
	public static class EnumAndIntlProvider implements ArgumentsProvider {
		@Override
		public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
			
			return new MainAndModuleProvider.EnumAndBrowser().provideArguments(context)
				.map(args -> args.get()[0])
				.flatMap(e ->
					Stream.of("foo.txt", "foó.txt")
						.map(page -> Arguments.of(e, page))
				);
		}
	}
	
	/**
	 * Tests test.MailExample, which uses requires admin login
	 */
	@ParameterizedTest
	@ArgumentsSource(MainAndModuleProvider.EnumOnly.class)
	public void testDirect(TestDatabase db) {
		Client client = getAnonymousClient();
		WebTarget target = client.target(getRestUrl(null, db) + "/mail/multipart");
		Response response = target.request().get();
		
		String output = response.readEntity(String.class);
		
		assertTrue(output.startsWith("I am preamble"), () -> "Received unexpected output: " + output);
	}
	
	/**
	 * Tests that multipart/form-data payloads can use UTF-8 in file names
	 * 
	 * @see <a href="https://github.com/OpenNTF/org.openntf.xsp.jakartaee/issues/501">Issue #501</a>
	 */
	@ParameterizedTest
	@ArgumentsSource(EnumAndIntlProvider.class)
	public void testUtfFileName(TestDatabase db, String fileName) {
		Client client = getAnonymousClient();
		WebTarget target = client.target(getRestUrl(null, db) + "/mail/echoFileName");

		String body = "--__X_PAW_BOUNDARY__\n"
				+ "Content-Disposition: form-data; name=\"file\"; filename=\"" + fileName + "\"\n"
				+ "Content-Type: application/octet-stream\n"
				+ "\n"
				+ "2023/11/21 12:56:39.221 [main] ERROR n.s.pmd.eclipse.plugin.PMDPlugin - Marker id 480772 not found.\n"
				+ "org.eclipse.core.internal.resources.ResourceException: Marker id 480772 not found.\n"
				+ "--__X_PAW_BOUNDARY__--";
		
		Response response = target.request()
			.accept(MediaType.TEXT_PLAIN)
			.post(Entity.entity(body, MediaType.MULTIPART_FORM_DATA_TYPE));
		checkResponse(200, response);
		
		assertEquals(fileName, new String(response.readEntity(byte[].class), StandardCharsets.UTF_8));
	}
}
