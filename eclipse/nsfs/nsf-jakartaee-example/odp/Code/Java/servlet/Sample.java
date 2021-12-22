package servlet;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import bean.ApplicationGuy;

@Path("/sample")
public class Sample {
	@Inject
	private ApplicationGuy applicationGuy;

	@GET
	public Response hello() {
		try {
			return Response.ok().type(MediaType.TEXT_PLAIN).entity(applicationGuy.getMessage()).build();
		} catch (Throwable t) {
			return Response.serverError().build();
		}
	}
	
	@GET
	@Path("/xml")
	@Produces(MediaType.APPLICATION_XML)
	public Object xml() {
		return applicationGuy;
	}
}
