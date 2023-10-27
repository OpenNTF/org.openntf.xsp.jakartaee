package rest;

import java.util.Map;

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
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String, Object> get() {
		return configuration.getProperties();
	}
}
