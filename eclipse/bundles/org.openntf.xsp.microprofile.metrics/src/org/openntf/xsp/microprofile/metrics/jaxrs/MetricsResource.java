/**
 * Copyright © 2018-2022 Jesse Gallagher
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openntf.xsp.microprofile.metrics.jaxrs;

import org.eclipse.microprofile.openapi.annotations.Operation;

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
	@Operation(hidden=true)
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
	@Operation(hidden=true)
	public String options() {
		Exporter exporter = new JsonMetadataExporter();
		// TODO limit to scopes
		return exporter.exportAllScopes().toString();
	}
}
