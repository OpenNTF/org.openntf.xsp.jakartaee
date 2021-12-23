package servlet;

import java.util.Collections;
import java.util.Map;

import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.openapi.annotations.Operation;

import bean.ApplicationGuy;

@Path("/jsonExample")
public class JsonExample {
	
	@Inject
	private ApplicationGuy applicationGuy;
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(description="Example showing a basic Map being returned")
	public Map<String, Object> get() {
		
		return Collections.singletonMap("foo", "bar");
	}
	
	@GET
	@Path("/jsonp")
	@Produces(MediaType.APPLICATION_JSON)
	public JsonObject getJsonp() {
		return Json.createObjectBuilder()
			.add("bar", "baz")
			.build();
	}
	
	@GET
	@Path("/jsonb")
	@Produces(MediaType.APPLICATION_JSON)
	public Object getJsonb() {
		return applicationGuy;
	}
}
