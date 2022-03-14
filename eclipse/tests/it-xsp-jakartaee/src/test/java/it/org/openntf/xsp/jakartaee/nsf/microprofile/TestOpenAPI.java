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
package it.org.openntf.xsp.jakartaee.nsf.microprofile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.ibm.commons.util.io.json.JsonException;
import com.ibm.commons.util.io.json.JsonJavaFactory;
import com.ibm.commons.util.io.json.JsonParser;

import it.org.openntf.xsp.jakartaee.AbstractWebClientTest;
import it.org.openntf.xsp.jakartaee.nsf.docker.DominoContainer;

@SuppressWarnings("nls")
public class TestOpenAPI extends AbstractWebClientTest {
	@ParameterizedTest
	@ValueSource(strings = { "openapi", "openapi.yaml" })
	public void testOpenAPIYaml(String path) {
		Client client = getAnonymousClient();
		WebTarget target = client.target(getRestUrl(null) + "/" + path);
		Response response = target.request().get();
		
		String yaml = response.readEntity(String.class);
		assertTrue(yaml.startsWith("---\nopenapi: 3.0"), () -> yaml);
		assertTrue(yaml.contains("  /adminrole:"));
	}
	
	@ParameterizedTest
	@ValueSource(strings = { "openapi", "openapi.json" })
	@SuppressWarnings("unchecked")
	public void testOpenAPIJson(String path) throws JsonException {
		Client client = getAnonymousClient();
		WebTarget target = client.target(getRestUrl(null) + "/" + path);
		Response response = target.request()
			.accept(MediaType.APPLICATION_JSON_TYPE)
			.get();
		
		String json = response.readEntity(String.class);
		Map<String, Object> obj = (Map<String, Object>) JsonParser.fromJson(JsonJavaFactory.instance, json);
		
		// Check for a known resource
		Map<String, Object> paths = (Map<String, Object>)obj.get("paths");
		assertTrue(paths.containsKey("/adminrole"));

		Map<String, Object> info = (Map<String, Object>)obj.get("info");
		assertEquals("XPages JEE Example", info.get("title"));
		
		// Check for the presence of a version from $TemplateBuild
		String mavenVersion = DominoContainer.getMavenVersion();
		if(mavenVersion.endsWith("-SNAPSHOT")) {
			mavenVersion = mavenVersion.substring(0, mavenVersion.length()-"-SNAPSHOT".length());
		}
		String version = (String)info.get("version");
		assertTrue(version.startsWith(mavenVersion), "Expected version '" + version + "' to start with '" + mavenVersion + "'");
		
		List<Map<String, Object>> servers = (List<Map<String, Object>>)obj.get("servers");
		Map<String, Object> server0 = servers.get(0);
		assertEquals(getRestUrl(null), server0.get("url"));
	}
	
	@ParameterizedTest
	@ValueSource(strings = { "openapi", "openapi.json" })
	@SuppressWarnings("unchecked")
	public void testOpenAPIBundleDb(String path) throws JsonException {
		Client client = getAnonymousClient();
		WebTarget target = client.target(getBundleNsfRestUrl(null) + "/" + path);
		Response response = target.request()
			.accept(MediaType.APPLICATION_JSON_TYPE)
			.get();
		
		String json = response.readEntity(String.class);
		Map<String, Object> obj = (Map<String, Object>) JsonParser.fromJson(JsonJavaFactory.instance, json);
		
		// Check for the presence overridden versions
		Map<String, Object> info = (Map<String, Object>)obj.get("info");
		assertEquals("OpenAPI Overridden Title", info.get("title"));
		assertEquals("3.1.1.override", info.get("version"));
		Map<String, Object> license = (Map<String, Object>)info.get("license");
		assertEquals("http://some.license/url", license.get("url"));
		
		List<Map<String, Object>> servers = (List<Map<String, Object>>)obj.get("servers");
		Map<String, Object> server0 = servers.get(0);
		assertEquals("http://override.server/path", server0.get("url"));
	}
}
