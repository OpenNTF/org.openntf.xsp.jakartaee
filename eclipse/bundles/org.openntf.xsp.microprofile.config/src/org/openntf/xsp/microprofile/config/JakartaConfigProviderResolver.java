/**
 * Copyright (c) 2018-2026 Contributors to the XPages Jakarta EE Support Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openntf.xsp.microprofile.config;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Iterator;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.Set;

import com.ibm.designer.runtime.domino.adapter.ComponentModule;

import org.eclipse.microprofile.config.Config;
import org.openntf.xsp.jakartaee.module.ComponentModuleLocator;
import org.openntf.xsp.microprofile.config.sources.ImplicitAppConfigSourceFactory;
import org.openntf.xsp.microprofile.config.sources.ModuleConfigSource;
import org.openntf.xsp.microprofile.config.sources.NotesEnvironmentConfigSource;
import org.openntf.xsp.microprofile.config.sources.XspPropertiesConfigSourceFactory;

import io.smallrye.config.PropertiesLocationConfigSourceFactory;
import io.smallrye.config.SmallRyeConfig;
import io.smallrye.config.SmallRyeConfigBuilder;
import io.smallrye.config.SmallRyeConfigFactory;
import io.smallrye.config.SmallRyeConfigProviderResolver;
import io.smallrye.config.SysPropConfigSource;
import jakarta.servlet.ServletContext;

/**
 * This is a variant of {@code SmallRyeConfigProviderResolver} that stores
 * configurations in active ComponentModules, ignoring ClassLoader parameters
 * 
 * @since 3.5.0
 */
public class JakartaConfigProviderResolver extends SmallRyeConfigProviderResolver {
	private static final String KEY_CONFIG = JakartaConfigProviderResolver.class.getName() + "_config"; //$NON-NLS-1$

	@Override
	public Config getConfig() {
		return getConfig(getContextClassLoader());
	}

	@Override
	public Config getConfig(ClassLoader classLoader) {
		// Ignore the ClassLoader and find the ComponentModule instead
		ComponentModule module = ComponentModuleLocator.getDefault()
			.map(ComponentModuleLocator::getActiveModule)
			.orElse(null);
		if(module == null) {
			return null;
		}
		
		Config config = (Config)module.getAttributes().get(KEY_CONFIG);
		if (config == null) {
			config = getFactoryFor().getConfigFor(this, classLoader);
			// don't cache null, as that would leak class loaders
			if (config == null) {
				throw new IllegalStateException("No configuration is available for this class loader");
			}
			module.getAttributes().put(KEY_CONFIG, config);
		}
		return config;
	}

	SmallRyeConfigFactory getFactoryFor() {
		final ServiceLoader<SmallRyeConfigFactory> serviceLoader = ServiceLoader.load(SmallRyeConfigFactory.class,
				getContextClassLoader());
		final Iterator<SmallRyeConfigFactory> iterator = serviceLoader.iterator();
		return iterator.hasNext() ? iterator.next() : DefaultFactory.INSTANCE;
	}

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
			new ImplicitAppConfigSourceFactory(),
			ctx -> Set.of(new ModuleConfigSource())
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

	@Override
	public void registerConfig(Config config, ClassLoader classLoader) {
		Objects.requireNonNull(config, "config cannot be null");

		ComponentModule module = ComponentModuleLocator.getDefault()
			.map(ComponentModuleLocator::getActiveModule)
			.orElse(null);
		if(module == null) {
			return;
		}
		
		final Config existing = (Config)module.getAttributes().putIfAbsent(KEY_CONFIG, config);
		if (existing != null) {
			throw new IllegalStateException("Configuration already registered for the given class loader");
		}
	}

	@Override
	public void releaseConfig(Config config) {
		ComponentModule module = ComponentModuleLocator.getDefault()
			.map(ComponentModuleLocator::getActiveModule)
			.orElse(null);
		if(module == null) {
			return;
		}
		
		module.getAttributes().entrySet().removeIf(e -> config == e.getValue());
	}

	static final class DefaultFactory extends SmallRyeConfigFactory {

		static final DefaultFactory INSTANCE = new DefaultFactory();

		public SmallRyeConfig getConfigFor(SmallRyeConfigProviderResolver configProviderResolver,
				ClassLoader classLoader) {
			return configProviderResolver.getBuilder()
					.forClassLoader(classLoader)
					.addDiscoveredCustomizers()
					.addDiscoveredInterceptors()
					.addDiscoveredConverters()
					.addDiscoveredSecretKeysHandlers()
					.addDefaultInterceptors()
					.addDefaultSources()
					.addDiscoveredSources()
					.addDiscoveredValidator()
					.build();
		}
	}

	static ClassLoader getContextClassLoader() {
		if (System.getSecurityManager() == null) {
			return Thread.currentThread().getContextClassLoader();
		} else {
			return AccessController.doPrivileged((PrivilegedAction<ClassLoader>) () -> {
				ClassLoader tccl = null;
				try {
					tccl = Thread.currentThread().getContextClassLoader();
				} catch (SecurityException ex) {
					throw new RuntimeException("Unable to get context classloader instance", ex);
				}
				return tccl;
			});
		}
	}
}
