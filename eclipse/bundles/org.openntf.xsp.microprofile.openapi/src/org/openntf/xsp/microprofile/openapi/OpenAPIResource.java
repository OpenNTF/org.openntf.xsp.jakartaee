/**
 * Copyright (c) 2018-2023 Contributors to the XPages Jakarta EE Support Project
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
package org.openntf.xsp.microprofile.openapi;

import java.io.IOException;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.openntf.xsp.jakartaee.metrics.MetricsIgnore;

import io.smallrye.openapi.runtime.io.Format;
import io.smallrye.openapi.runtime.io.OpenApiSerializer;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("openapi")
@MetricsIgnore
public class OpenAPIResource extends AbstractOpenAPIResource {
	
	@GET
	@Operation(hidden=true)
	public Response get(@Context HttpHeaders headers) throws IOException {
		OpenAPI openapi = buildOpenAPI();

		// JSON wins if it's explicitly mentioned; otherwise it's YAML as text/plain
		boolean hasJson = headers.getAcceptableMediaTypes()
			.stream()
			.anyMatch(type -> !type.isWildcardType() && !type.isWildcardSubtype() && type.isCompatible(MediaType.APPLICATION_JSON_TYPE));
		if(hasJson) {
			return Response.ok()
				.type(MediaType.APPLICATION_JSON_TYPE)
				.entity(OpenApiSerializer.serialize(openapi, Format.JSON))
				.build();
		} else {
			return Response.ok()
				.type(MediaType.TEXT_PLAIN)
				.entity(OpenApiSerializer.serialize(openapi, Format.YAML))
				.build();
		}
	}
}
