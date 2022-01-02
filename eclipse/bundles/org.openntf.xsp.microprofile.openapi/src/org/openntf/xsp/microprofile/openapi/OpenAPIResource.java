/**
 * Copyright © 2018-2021 Martin Pradny and Jesse Gallagher
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
package org.openntf.xsp.microprofile.openapi;

import java.io.IOException;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.eclipse.microprofile.openapi.models.servers.Server;
import org.jboss.jandex.Index;
import org.openntf.xsp.jakartaee.DelegatingClassLoader;
import org.openntf.xsp.jaxrs.JAXRSServletFactory;
import org.openntf.xsp.microprofile.openapi.config.NOPConfig;

import com.ibm.commons.util.PathUtil;
import com.ibm.domino.xsp.module.nsf.NotesContext;

import io.smallrye.openapi.api.OpenApiConfig;
import io.smallrye.openapi.api.OpenApiConfigImpl;
import io.smallrye.openapi.api.models.servers.ServerImpl;
import io.smallrye.openapi.runtime.OpenApiProcessor;
import io.smallrye.openapi.runtime.io.Format;
import io.smallrye.openapi.runtime.io.OpenApiSerializer;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Configuration;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lotus.domino.Database;
import lotus.domino.NotesException;

@Path("openapi")
public class OpenAPIResource {
	
	@Context
	Configuration jaxrsConfig;
	
	@Context
	Application application;
	
	@Context
	HttpServletRequest req;
	
	@GET
	public Response get(@Context HttpHeaders headers) throws IOException, NotesException {
		Set<Class<?>> classes = new HashSet<>();
		classes.addAll(application.getClasses());
		classes.add(application.getClass());
		Index index = Index.of(classes);
		
		Config mpConfig = new NOPConfig();
		OpenApiConfig config = OpenApiConfigImpl.fromConfig(mpConfig);
		ClassLoader cl = new DelegatingClassLoader(OpenApiProcessor.class.getClassLoader(), Thread.currentThread().getContextClassLoader());
		OpenAPI openapi = OpenApiProcessor.bootstrap(config, index, cl);
		
		NotesContext notesContext = NotesContext.getCurrent();
		Database database = notesContext.getCurrentDatabase();
		// TODO look up version from $TemplateBuild
		openapi.getInfo().setTitle(database.getTitle());
		
		// Build a URI to the base of JAX-RS
		Server server = new ServerImpl();
		URI uri = URI.create(req.getRequestURL().toString());
		String jaxrsRoot = JAXRSServletFactory.getServletPath(notesContext.getModule());
		uri = uri.resolve(PathUtil.concat(req.getContextPath(), jaxrsRoot, '/'));
		String uriString = uri.toString();
		if(uriString.endsWith("/")) { //$NON-NLS-1$
			uriString = uriString.substring(0, uriString.length()-1);
		}
		server.setUrl(uriString);
		openapi.addServer(server);

		// JSON wins if it's explicitly mentioned; otherwise it's YAML as text/plain
		boolean hasJson = headers.getAcceptableMediaTypes()
			.stream()
			.anyMatch(type -> !type.isWildcardType() && !type.isWildcardSubtype() && type.isCompatible(MediaType.APPLICATION_JSON_TYPE));
		if(hasJson) {
			return Response.ok()
				.type(MediaType.APPLICATION_JSON_TYPE)
				.entity(OpenApiSerializer.serialize(openapi, Format.JSON))
				.build();
		} else {
			return Response.ok()
				.type(MediaType.TEXT_PLAIN)
				.entity(OpenApiSerializer.serialize(openapi, Format.YAML))
				.build();
		}
	}
}
