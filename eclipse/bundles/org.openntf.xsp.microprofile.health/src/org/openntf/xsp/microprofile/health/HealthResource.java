package org.openntf.xsp.microprofile.health;

import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.Liveness;
import org.eclipse.microprofile.health.Readiness;
import org.eclipse.microprofile.health.Startup;

import io.smallrye.health.api.HealthRegistry;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("health")
public class HealthResource {
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Object getAll() {
		Map<String, Object> result = new LinkedHashMap<>();
		result.put("ready", getReadiness()); //$NON-NLS-1$
		result.put("live", getLiveness()); //$NON-NLS-1$
		result.put("started", getStarted()); //$NON-NLS-1$
		return result;
	}
	
	@Path("ready")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Object getReadiness() {
		HealthRegistry result = CDI.current().select(HealthRegistry.class, Readiness.Literal.INSTANCE).get();
		CDI.current().select(HealthCheck.class, Readiness.Literal.INSTANCE).forEach(result::register);
		return result;
	}

	@Path("live")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Object getLiveness() {
		return CDI.current().select(HealthRegistry.class, Liveness.Literal.INSTANCE).get();
	}
	
	@Path("started")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Object getStarted() {
		return CDI.current().select(HealthRegistry.class, Startup.Literal.INSTANCE).get();
	}
}
