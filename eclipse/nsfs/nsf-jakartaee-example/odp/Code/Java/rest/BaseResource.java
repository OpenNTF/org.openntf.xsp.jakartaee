package rest;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("")
public class BaseResource {
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String get() {
		return "I am the base resource.";
	}
}
