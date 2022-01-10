package org.openntf.xsp.microprofile.config.sources;

import java.util.Collections;

import org.eclipse.microprofile.config.spi.ConfigSource;

import com.ibm.xsp.application.ApplicationEx;

import io.smallrye.config.ConfigSourceContext;
import io.smallrye.config.ConfigSourceFactory;

/**
 * Factory for {@link XspPropertiesConfigSource} instances that use
 * the currently-active {@link ApplicationEx} instance.
 * 
 * @author Jesse Gallagher
 * @since 2.2.0
 */
public class XspPropertiesConfigSourceFactory implements ConfigSourceFactory {

	@Override
	public Iterable<ConfigSource> getConfigSources(ConfigSourceContext context) {
		return Collections.singleton(new XspPropertiesConfigSource(ApplicationEx.getInstance()));
	}

}
