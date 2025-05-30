package org.openntf.xsp.microprofile.config;

import java.security.AccessController;
import java.security.PrivilegedAction;

import org.eclipse.microprofile.config.spi.ConfigProviderResolver;
import org.openntf.xsp.jakartaee.events.JakartaHttpInitListener;
import org.openntf.xsp.jakartaee.module.ComponentModuleLocator;
import org.openntf.xsp.microprofile.config.sources.ImplicitAppConfigSourceFactory;
import org.openntf.xsp.microprofile.config.sources.NotesEnvironmentConfigSource;
import org.openntf.xsp.microprofile.config.sources.XspPropertiesConfigSourceFactory;

import io.smallrye.config.PropertiesLocationConfigSourceFactory;
import io.smallrye.config.SmallRyeConfigBuilder;
import io.smallrye.config.SmallRyeConfigProviderResolver;
import io.smallrye.config.SysPropConfigSource;
import jakarta.annotation.Priority;
import jakarta.servlet.ServletContext;

@Priority(1)
public class ConfigHttpInitListener implements JakartaHttpInitListener {
	@Override
	public void httpInit() throws Exception {
		SmallRyeConfigProviderResolver resolver = new SmallRyeConfigProviderResolver() {
			@Override
			public SmallRyeConfigBuilder getBuilder() {
				SmallRyeConfigBuilder builder = super.getBuilder();

				// Manually add sources that would come from ServiceLoader
				builder.withSources(
					new SysPropConfigSource(),
					new NotesEnvironmentConfigSource()
				);
				builder.withSources(
					new PropertiesLocationConfigSourceFactory(),
					new XspPropertiesConfigSourceFactory(),
					new ImplicitAppConfigSourceFactory()
				);

				AccessController.doPrivileged((PrivilegedAction<Void>)() -> {
					ComponentModuleLocator.getDefault()
						.flatMap(ComponentModuleLocator::getServletContext)
						.map(ServletContext::getClassLoader)
						.ifPresent(builder::forClassLoader);
					return null;
				});

				return builder;
			}
		};

		ConfigProviderResolver.setInstance(resolver);
	}
}
