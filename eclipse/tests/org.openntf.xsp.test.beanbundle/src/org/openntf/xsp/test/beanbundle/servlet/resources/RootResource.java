package org.openntf.xsp.test.beanbundle.servlet.resources;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/")
public class RootResource {
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String get() {
		return "I am root resource."; //$NON-NLS-1$
	}
}
