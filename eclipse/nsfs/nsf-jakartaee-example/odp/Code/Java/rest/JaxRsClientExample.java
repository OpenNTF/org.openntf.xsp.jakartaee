package rest;

import bean.RestClientBean;
import jakarta.inject.Inject;
import jakarta.json.JsonObject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("jaxrsClient")
public class JaxRsClientExample {
	
	@Inject
	private RestClientBean restClientBean;
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public JsonObject get() {
		return restClientBean.getJsonObjectViaClient();
	}
}
