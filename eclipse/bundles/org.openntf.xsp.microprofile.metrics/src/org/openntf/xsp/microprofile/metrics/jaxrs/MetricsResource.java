package org.openntf.xsp.microprofile.metrics.jaxrs;

import io.smallrye.metrics.exporters.Exporter;
import io.smallrye.metrics.exporters.JsonExporter;
import io.smallrye.metrics.exporters.JsonMetadataExporter;
import io.smallrye.metrics.exporters.OpenMetricsExporter;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.OPTIONS;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("metrics")
public class MetricsResource {
	@GET
	public Response get(@Context HttpHeaders headers) {
		// JSON wins if it's explicitly mentioned; otherwise it's OpenMetrics as text/plain
		boolean hasJson = headers.getAcceptableMediaTypes()
			.stream()
			.anyMatch(type -> !type.isWildcardType() && !type.isWildcardSubtype() && type.isCompatible(MediaType.APPLICATION_JSON_TYPE));
		Exporter exporter;
		if(hasJson) {
			exporter = new JsonExporter();
		} else {
			exporter = new OpenMetricsExporter();
		}
		// TODO limit scopes
		return Response.ok()
			.type(exporter.getContentType())
			.entity(exporter.exportAllScopes())
			.build();
	}
	
	@OPTIONS
	@Produces(MediaType.APPLICATION_JSON)
	public String options() {
		Exporter exporter = new JsonMetadataExporter();
		// TODO limit to scopes
		return exporter.exportAllScopes().toString();
	}
}
