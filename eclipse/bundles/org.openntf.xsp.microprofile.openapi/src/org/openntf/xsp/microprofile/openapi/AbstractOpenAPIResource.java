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
package org.openntf.xsp.microprofile.openapi;

import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.eclipse.microprofile.openapi.models.info.Info;
import org.eclipse.microprofile.openapi.models.servers.Server;
import org.jboss.jandex.Index;
import org.openntf.xsp.jakartaee.DelegatingClassLoader;
import org.openntf.xsp.jakartaee.module.ComponentModuleLocator;
import org.openntf.xsp.jakartaee.util.ModuleUtil;
import org.openntf.xsp.jaxrs.JAXRSServletFactory;

import com.ibm.commons.util.PathUtil;

import io.smallrye.openapi.api.OpenApiConfig;
import io.smallrye.openapi.api.OpenApiConfigImpl;
import io.smallrye.openapi.api.models.servers.ServerImpl;
import io.smallrye.openapi.runtime.OpenApiProcessor;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Configuration;
import jakarta.ws.rs.core.Context;

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

	
	protected OpenAPI buildOpenAPI() throws IOException {
		Set<Class<?>> classes = new HashSet<>();
		classes.addAll(application.getClasses());
		classes.add(application.getClass());

		Optional<ComponentModuleLocator> module = ComponentModuleLocator.getDefault();
		module.map(ComponentModuleLocator::getActiveModule)
			.map(ModuleUtil::getClasses)
			.ifPresent(moduleClasses -> moduleClasses.forEach(classes::add));
		
		Index index = Index.of(classes);
		
		Config mpConfig = CDI.current().select(Config.class).get();
		OpenApiConfig config = OpenApiConfigImpl.fromConfig(mpConfig);
		ClassLoader cl = new DelegatingClassLoader(OpenApiProcessor.class.getClassLoader(), Thread.currentThread().getContextClassLoader());
		OpenAPI openapi;
		synchronized(OpenApiProcessor.class) {
			// OpenApiProcessor appears to be not thread-safe
			openapi = OpenApiProcessor.bootstrap(config, index, cl);
		}
		
		Info info = openapi.getInfo();
		String existingTitle = config.getInfoTitle();
		if(existingTitle == null || existingTitle.isEmpty()) {
			info.setTitle(module.map(ComponentModuleLocator::getTitle).orElse(null));
		} else {
			info.setTitle(existingTitle);
		}
		String existingVersion = config.getInfoVersion();
		if(existingVersion == null || existingVersion.isEmpty()) {
			String version = module.flatMap(ComponentModuleLocator::getVersion).orElse(null);
			info.setVersion(version);
		} else {
			info.setVersion(existingVersion);
		}
	
		// Build a URI to the base of JAX-RS
		Collection<String> servers = config.servers();
		if(servers == null || servers.isEmpty()) {
			Server server = new ServerImpl();
			
			URI uri = URI.create(req.getRequestURL().toString());
			
			String jaxrsRoot = module.map(ComponentModuleLocator::getActiveModule)
				.map(JAXRSServletFactory::getServletPath)
				.orElse(""); //$NON-NLS-1$
			uri = uri.resolve(PathUtil.concat(req.getContextPath(), jaxrsRoot, '/'));
			String uriString = uri.toString();
			if(uriString.endsWith("/")) { //$NON-NLS-1$
				uriString = uriString.substring(0, uriString.length()-1);
			}
			server.setUrl(uriString);
			openapi.addServer(server);
		}
		
		return openapi;
	}
}
