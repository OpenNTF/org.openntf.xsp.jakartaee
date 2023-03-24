package config;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.eclipse.microprofile.config.spi.ConfigSource;

/**
 * Demonstrates the use of a custom {@link ConfigSource} implementation registered
 * in {@code /META-INF/services/org.eclipse.microprofile.config.spi.ConfigSource}
 * 
 * @since 2.11.0
 */
public class ExampleConfigSource implements ConfigSource {
	
	private final Map<String, String> properties;
	
	public ExampleConfigSource() {
		properties = Collections.unmodifiableMap(Collections.singletonMap("ExampleConfig", "I am the example value"));
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
