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
package org.openntf.xsp.microprofile.metrics.jaxrs;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.openntf.xsp.jakartaee.metrics.MetricsIgnore;
import org.openntf.xsp.microprofile.metrics.config.MetricsAppConfigSource;
import org.openntf.xsp.microprofile.metrics.exporter.FilteringPrometheusMetricsExporer;

import io.smallrye.metrics.exporters.Exporter;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;

@Path("metrics")
@MetricsIgnore
public class MetricsResource {
	@GET
	@Operation(hidden=true)
	public Response get(@Context HttpHeaders headers) {
		Config mpConfig = CDI.current().select(Config.class).get();
		String appName = mpConfig.getOptionalValue(MetricsAppConfigSource.CONFIG_APPNAME, String.class)
			.orElse(null);
		
		Exporter exporter = new FilteringPrometheusMetricsExporer(appName);
		// TODO limit scopes
		return Response.ok()
			.type(exporter.getContentType())
			.entity(exporter.exportAllScopes().toString())
			.build();
	}
}
