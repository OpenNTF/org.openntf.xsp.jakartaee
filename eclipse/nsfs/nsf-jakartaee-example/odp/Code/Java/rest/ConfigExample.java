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
package rest;

import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.servlet.ServletContext;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("config")
@ApplicationScoped
public class ConfigExample {
	@Inject
	@ConfigProperty(name="java.version", defaultValue="nope")
	private String javaVersion;
	
	@Inject
	@ConfigProperty(name="xsp.library.depends")
	private String xspDepends;
	
	@Inject
	@ConfigProperty(name="Directory")
	private String directory;
	
	@Inject
	@ConfigProperty(name="mpconfig.example.setting")
	private String exampleSetting;
	
	@Inject
	private ServletContext context;
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String, Object> get() throws ClassNotFoundException {
		Map<String, Object> result = new LinkedHashMap<>();
		result.put("java.version", javaVersion);
		result.put("xsp.library.depends", xspDepends);
		result.put("Directory", directory);
		result.put("mpconfig.example.setting", exampleSetting);
		return result;
	}
	
}