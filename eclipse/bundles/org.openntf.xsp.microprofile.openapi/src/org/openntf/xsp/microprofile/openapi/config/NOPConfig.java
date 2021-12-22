package org.openntf.xsp.microprofile.openapi.config;

import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigValue;
import org.eclipse.microprofile.config.spi.ConfigSource;
import org.eclipse.microprofile.config.spi.Converter;

public class NOPConfig implements Config {

	@Override
	public <T> T getValue(String propertyName, Class<T> propertyType) {
		throw new NoSuchElementException();
	}

	@Override
	public ConfigValue getConfigValue(String propertyName) {
		return NOPConfigValue.instance;
	}

	@Override
	public <T> Optional<T> getOptionalValue(String propertyName, Class<T> propertyType) {
		return Optional.empty();
	}

	@Override
	public Iterable<String> getPropertyNames() {
		return Collections.emptySet();
	}

	@Override
	public Iterable<ConfigSource> getConfigSources() {
		return Collections.emptySet();
	}

	@Override
	public <T> Optional<Converter<T>> getConverter(Class<T> forType) {
		return Optional.empty();
	}

	@Override
	public <T> T unwrap(Class<T> type) {
		throw new IllegalArgumentException("Unwrapping not supported in NOPConfig");
	}

}
