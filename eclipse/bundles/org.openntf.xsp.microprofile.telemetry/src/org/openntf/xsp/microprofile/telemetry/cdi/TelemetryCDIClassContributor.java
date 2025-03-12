package org.openntf.xsp.microprofile.telemetry.cdi;

import java.util.Collection;
import java.util.Set;

import org.openntf.xsp.jakarta.cdi.discovery.CDIClassContributor;

import io.smallrye.opentelemetry.implementation.cdi.OpenTelemetryExtension;
import io.smallrye.opentelemetry.implementation.config.OpenTelemetryConfigProducer;
import jakarta.enterprise.inject.spi.Extension;

public class TelemetryCDIClassContributor implements CDIClassContributor {
	@Override
	public Collection<Class<?>> getBeanClasses() {
		return Set.of(OpenTelemetryConfigProducer.class);
	}
	
	@Override
	public Collection<Class<? extends Extension>> getExtensionClasses() {
		return Set.of(OpenTelemetryExtension.class);
	}
}
