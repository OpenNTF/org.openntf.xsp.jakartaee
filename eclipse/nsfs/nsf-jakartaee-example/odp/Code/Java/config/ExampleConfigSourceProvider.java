package config;

import java.util.Collections;

import org.eclipse.microprofile.config.spi.ConfigSource;
import org.eclipse.microprofile.config.spi.ConfigSourceProvider;

/**
 * Demonstrates the use of a custom {@link ConfigSourceProvider} implementation registered
 * in {@code /META-INF/services/org.eclipse.microprofile.config.spi.ConfigSourceProvider}
 * 
 * @since 2.11.0
 */
public class ExampleConfigSourceProvider implements ConfigSourceProvider {
	private static final Iterable<ConfigSource> SOURCES = Collections.singleton(new ExampleProvidedConfigSource());

	@Override
	public Iterable<ConfigSource> getConfigSources(ClassLoader forClassLoader) {
		return SOURCES;
	}

}
