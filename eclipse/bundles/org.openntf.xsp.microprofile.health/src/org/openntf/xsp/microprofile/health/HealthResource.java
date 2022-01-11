package org.openntf.xsp.microprofile.health;

import io.smallrye.health.SmallRyeHealth;
import io.smallrye.health.SmallRyeHealthReporter;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.StreamingOutput;

@Path("health")
public class HealthResource {
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Object getAll() {
		SmallRyeHealthReporter reporter = CDI.current().select(SmallRyeHealthReporter.class).get();
		SmallRyeHealth health = reporter.getHealth();
		return (StreamingOutput)(os) -> reporter.reportHealth(os, health);
	}
	
	@Path("ready")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Object getReadiness() {
		SmallRyeHealthReporter reporter = CDI.current().select(SmallRyeHealthReporter.class).get();
		SmallRyeHealth health = reporter.getReadiness();
		return (StreamingOutput)(os) -> reporter.reportHealth(os, health);
	}

	@Path("live")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Object getLiveness() {
		SmallRyeHealthReporter reporter = CDI.current().select(SmallRyeHealthReporter.class).get();
		SmallRyeHealth health = reporter.getLiveness();
		return (StreamingOutput)(os) -> reporter.reportHealth(os, health);
	}
	
	@Path("started")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Object getStarted() {
		SmallRyeHealthReporter reporter = CDI.current().select(SmallRyeHealthReporter.class).get();
		SmallRyeHealth health = reporter.getStartup();
		return (StreamingOutput)(os) -> reporter.reportHealth(os, health);
	}
}
