package org.openntf.xsp.microprofile.openapi;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.jboss.jandex.Index;
import org.openntf.xsp.microprofile.openapi.config.NOPConfig;

import io.smallrye.openapi.api.OpenApiConfig;
import io.smallrye.openapi.api.OpenApiConfigImpl;
import io.smallrye.openapi.runtime.OpenApiProcessor;
import io.smallrye.openapi.runtime.io.Format;
import io.smallrye.openapi.runtime.io.OpenApiSerializer;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Configuration;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;

@Path("openapi")
public class OpenAPIResource {
	
	@Context
	Configuration jaxrsConfig;
	
	@Context
	Application application;
	
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public Object getYaml() throws IOException {
		Set<Class<?>> classes = new HashSet<>();
		classes.addAll(application.getClasses());
		classes.add(application.getClass());
		Index index = Index.of(classes);
		
		Config mpConfig = new NOPConfig();
		OpenApiConfig config = OpenApiConfigImpl.fromConfig(mpConfig);
		OpenAPI openapi = OpenApiProcessor.bootstrap(config, index, OpenApiProcessor.class.getClassLoader());
		
		
		return OpenApiSerializer.serialize(openapi, Format.YAML);
	}
}
