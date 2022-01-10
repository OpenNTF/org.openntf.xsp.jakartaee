package org.openntf.xsp.microprofile.config.sources;

import java.util.Collections;
import java.util.Set;

import org.eclipse.microprofile.config.spi.ConfigSource;

import com.ibm.xsp.application.ApplicationEx;

/**
 * {@link ConfigSource} implementation that reads properties from
 * the current NSF's xsp.properties file.
 * 
 * @author Jesse Gallagher
 * @since 2.2.0
 */
public class XspPropertiesConfigSource implements ConfigSource {
	private final ApplicationEx application;
	
	public XspPropertiesConfigSource(ApplicationEx application) {
		this.application = application;
	}
	
	@Override
	public String getName() {
		return "XSPProperties"; //$NON-NLS-1$
	}
	
	@Override
	public Set<String> getPropertyNames() {
		return Collections.emptySet();
	}

	@Override
	public String getValue(String propertyName) {
		return application.getProperty(propertyName, null);
	}

}
