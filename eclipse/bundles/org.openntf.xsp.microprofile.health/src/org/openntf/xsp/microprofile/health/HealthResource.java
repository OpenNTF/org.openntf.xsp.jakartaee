/**
 * Copyright (c) 2018-2025 Contributors to the XPages Jakarta EE Support Project
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
package org.openntf.xsp.microprofile.health;

import java.util.function.Function;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.openntf.xsp.jakartaee.metrics.MetricsIgnore;

import io.smallrye.health.SmallRyeHealth;
import io.smallrye.health.SmallRyeHealthReporter;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.StreamingOutput;

@Path("health")
@MetricsIgnore
public class HealthResource {

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(hidden=true)
	public Response getAll() {
		return emit(SmallRyeHealthReporter::getHealth);
	}

	@Path("ready")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(hidden=true)
	public Response getReadiness() {
		return emit(SmallRyeHealthReporter::getReadiness);
	}

	@Path("live")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(hidden=true)
	public Response getLiveness() {
		return emit(SmallRyeHealthReporter::getLiveness);
	}

	@Path("started")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(hidden=true)
	public Response getStarted() {
		return emit(SmallRyeHealthReporter::getStartup);
	}

	private Response emit(final Function<SmallRyeHealthReporter, SmallRyeHealth> c) {
		SmallRyeHealthReporter reporter = CDI.current().select(SmallRyeHealthReporter.class).get();
		SmallRyeHealth health = c.apply(reporter);
		return Response.status(health.isDown() ? Response.Status.SERVICE_UNAVAILABLE : Response.Status.OK)
			.type(MediaType.APPLICATION_JSON_TYPE)
			.entity((StreamingOutput)os -> reporter.reportHealth(os, health))
			.build();
	}
}
