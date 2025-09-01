package org.openntf.xsp.microprofile.config.sources;

import java.util.Properties;
import java.util.Set;

import org.eclipse.microprofile.config.spi.ConfigSource;
import org.openntf.xsp.jakartaee.module.ComponentModuleLocator;
import org.openntf.xsp.jakartaee.module.jakarta.AbstractJakartaModule;

/**
 * This config source reads from a map provided to the active
 * ComponentModule by the loading environment.
 * 
 * @since 3.5.0
 */
public class ModuleConfigSource implements ConfigSource {
	private final Properties properties;
	
	public ModuleConfigSource() {
		this.properties = ComponentModuleLocator.getDefault()
			.map(ComponentModuleLocator::getActiveModule)
			.map(mod -> mod.getAttributes().get(AbstractJakartaModule.PROP_MPCONFIG))
			.map(Properties.class::cast)
			.orElseGet(Properties::new);
	}

	@Override
	public Set<String> getPropertyNames() {
		return this.properties.stringPropertyNames();
	}

	@Override
	public String getValue(String propertyName) {
		return this.properties.getProperty(propertyName);
	}

	@Override
	public String getName() {
		return "ModulePropertiesSource"; //$NON-NLS-1$
	}

}
