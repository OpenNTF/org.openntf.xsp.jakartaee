package config;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.eclipse.microprofile.config.spi.ConfigSource;

/**
 * Demonstrates the use of a custom {@link ConfigSource} implementation provided
 * by {@link ExampleConfigSourceProvider}.
 * 
 * @since 2.11.0
 */
public class ExampleProvidedConfigSource implements ConfigSource {

	private final Map<String, String> properties;
	
	public ExampleProvidedConfigSource() {
		properties = Collections.unmodifiableMap(Collections.singletonMap("ExampleProvidedConfig", "I am the example value from a provided source"));
	}
	
	@Override
	public Set<String> getPropertyNames() {
		return properties.keySet();
	}

	@Override
	public String getValue(String propertyName) {
		return properties.get(propertyName);
	}

	@Override
	public String getName() {
		return getClass().getSimpleName();
	}

}
