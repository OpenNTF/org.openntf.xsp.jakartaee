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

import com.ibm.commons.util.PathUtil;
import com.ibm.domino.xsp.module.nsf.NotesContext;

import io.smallrye.openapi.api.OpenApiConfig;
import io.smallrye.openapi.api.OpenApiConfigImpl;
import io.smallrye.openapi.api.models.servers.ServerImpl;
import io.smallrye.openapi.runtime.OpenApiProcessor;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Configuration;
import jakarta.ws.rs.core.Context;
import lotus.domino.Database;
import lotus.domino.NotesException;

/**
 * @author Jesse Gallagher
 * @since 2.2.0
 */
public abstract class AbstractOpenAPIResource {
	
	@Context
	protected Configuration jaxrsConfig;
	
	@Context
	protected Application application;
	
	@Context
	protected HttpServletRequest req;

	
	protected OpenAPI buildOpenAPI() throws IOException, NotesException {
		Set<Class<?>> classes = new HashSet<>();
		classes.addAll(application.getClasses());
		classes.add(application.getClass());
		Index index = Index.of(classes);
		
		Config mpConfig = CDI.current().select(Config.class).get();
		OpenApiConfig config = OpenApiConfigImpl.fromConfig(mpConfig);
		ClassLoader cl = new DelegatingClassLoader(OpenApiProcessor.class.getClassLoader(), Thread.currentThread().getContextClassLoader());
		OpenAPI openapi = OpenApiProcessor.bootstrap(config, index, cl);
		
		NotesContext notesContext = NotesContext.getCurrent();
		Database database = notesContext.getCurrentDatabase();
		// TODO look up version from $TemplateBuild
		String title = openapi.getInfo().getTitle();
		if(title == null || title.isEmpty()) {
			openapi.getInfo().setTitle(database.getTitle());
		}
		
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
		
		return openapi;
	}
}
