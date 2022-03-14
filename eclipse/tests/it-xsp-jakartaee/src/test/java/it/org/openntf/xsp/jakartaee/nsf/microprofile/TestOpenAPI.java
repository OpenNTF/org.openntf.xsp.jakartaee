/**
 * Copyright © 2018-2022 Jesse Gallagher
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

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

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
		
		// Check for the presence of a version from $TemplateBuild
		Map<String, Object> info = (Map<String, Object>)obj.get("info");
		String mavenVersion = DominoContainer.getMavenVersion();
		if(mavenVersion.endsWith("-SNAPSHOT")) {
			mavenVersion = mavenVersion.substring(0, mavenVersion.length()-"-SNAPSHOT".length());
		}
		String version = (String)info.get("version");
		assertTrue(version.startsWith(mavenVersion), "Expected version '" + version + "' to start with '" + mavenVersion + "'");
	}
}
