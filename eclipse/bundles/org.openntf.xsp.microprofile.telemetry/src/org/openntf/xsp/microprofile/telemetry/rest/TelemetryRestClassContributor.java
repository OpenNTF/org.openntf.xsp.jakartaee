package org.openntf.xsp.microprofile.telemetry.rest;

import java.util.Collection;
import java.util.List;

import org.openntf.xsp.jakarta.rest.RestClassContributor;

import io.smallrye.opentelemetry.implementation.rest.OpenTelemetryClientFilter;
import io.smallrye.opentelemetry.implementation.rest.OpenTelemetryServerFilter;

public class TelemetryRestClassContributor implements RestClassContributor {

	@Override
	public Collection<Class<?>> getClasses() {
		return List.of(
			OpenTelemetryServerFilter.class,
			OpenTelemetryClientFilter.class
		);
	}

}
