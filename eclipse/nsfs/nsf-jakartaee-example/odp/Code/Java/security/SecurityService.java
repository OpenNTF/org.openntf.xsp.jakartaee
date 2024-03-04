package security;

import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("security")
public class SecurityService {
	
	@Inject
	private HttpServletRequest request;
	
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String get() {
		return "good for you, " + request.getRemoteUser();
	}
}
