package rest;

import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import jakarta.inject.Inject;
import jakarta.servlet.ServletContext;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Configuration;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;

@Path("jaxrsConfig")
public class JaxRsConfigResource {
	@Context
	private Configuration configuration;
	
	@Inject
	private ServletContext servletContext;
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String, Object> get() {
		return configuration.getProperties();
	}
	
	@Path("servlet")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String, Object> getServletConfig() {
		return Collections.list(servletContext.getInitParameterNames())
		.stream()
		.collect(Collectors.toMap(
			Function.identity(),
			name -> servletContext.getInitParameter(name)
		));
	}
}
