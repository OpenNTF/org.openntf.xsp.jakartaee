package org.openntf.xsp.microprofile.openapi;

import java.io.IOException;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.models.OpenAPI;

import io.smallrye.openapi.runtime.io.Format;
import io.smallrye.openapi.runtime.io.OpenApiSerializer;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import lotus.domino.NotesException;

@Path("openapi.json")
public class OpenAPIJSONResource extends AbstractOpenAPIResource {
	@GET
	@Operation(hidden=true)
	@Produces(MediaType.APPLICATION_JSON)
	public String get(@Context HttpHeaders headers) throws IOException, NotesException {
		OpenAPI openapi = buildOpenAPI();
		return OpenApiSerializer.serialize(openapi, Format.JSON);
	}
}
