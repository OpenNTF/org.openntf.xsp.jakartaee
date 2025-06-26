package org.openntf.xsp.microprofile.config;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.WeakHashMap;

import org.eclipse.microprofile.config.Config;
import org.openntf.xsp.jakartaee.module.ComponentModuleLocator;
import org.openntf.xsp.microprofile.config.sources.ImplicitAppConfigSourceFactory;
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
 * This is a variant of {@code SmallRyeConfigProviderResolver} that uses a
 * weak-keyed
 * map for its ClassLoader-to-Config cache to avoid retaining ClassLoaders
 * indefinitely as apps are refreshed.
 * 
 * @since 3.5.0
 */
public class JakartaConfigProviderResolver extends SmallRyeConfigProviderResolver {
	private final Map<ClassLoader, Config> configsForClassLoader = new WeakHashMap<>();

	static final ClassLoader SYSTEM_CL;

	static {
		final SecurityManager sm = System.getSecurityManager();
		if (sm != null) {
			SYSTEM_CL = AccessController
					.doPrivileged(
							(PrivilegedAction<ClassLoader>) JakartaConfigProviderResolver::calculateSystemClassLoader);
		} else {
			SYSTEM_CL = calculateSystemClassLoader();
		}
	}

	private static ClassLoader calculateSystemClassLoader() {
		ClassLoader cl = ClassLoader.getSystemClassLoader();
		if (cl == null) {
			// non-null ref that delegates to the system
			cl = new ClassLoader(null) {
			};
		}
		return cl;
	}

	@Override
	public Config getConfig() {
		return getConfig(getContextClassLoader());
	}

	@Override
	public Config getConfig(ClassLoader classLoader) {
		final ClassLoader realClassLoader = getRealClassLoader(classLoader);
		final Map<ClassLoader, Config> configsForClassLoader = this.configsForClassLoader;
		synchronized (configsForClassLoader) {
			Config config = configsForClassLoader.get(realClassLoader);
			if (config == null) {
				config = configsForClassLoader.get(realClassLoader);
				if (config == null) {
					config = getFactoryFor(realClassLoader, false).getConfigFor(this, classLoader);
					// don't cache null, as that would leak class loaders
					if (config == null) {
						throw new IllegalStateException("No configuration is available for this class loader");
					}
					configsForClassLoader.put(realClassLoader, config);
				}
			}
			return config;
		}
	}

	SmallRyeConfigFactory getFactoryFor(final ClassLoader classLoader, final boolean privileged) {
		final SecurityManager sm = System.getSecurityManager();
		if (sm != null && !privileged) {
			// run privileged so that the only things on the access control stack are us and
			// the provider
			return AccessController.doPrivileged(new PrivilegedAction<SmallRyeConfigFactory>() {
				public SmallRyeConfigFactory run() {
					return getFactoryFor(classLoader, true);
				}
			});
		}
		final ServiceLoader<SmallRyeConfigFactory> serviceLoader = ServiceLoader.load(SmallRyeConfigFactory.class,
				classLoader);
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

	@Override
	public void registerConfig(Config config, ClassLoader classLoader) {
		Objects.requireNonNull(config, "config cannot be null");
		final ClassLoader realClassLoader = getRealClassLoader(classLoader);
		final Map<ClassLoader, Config> configsForClassLoader = this.configsForClassLoader;
		synchronized (configsForClassLoader) {
			final Config existing = configsForClassLoader.putIfAbsent(realClassLoader, config);
			if (existing != null) {
				throw new IllegalStateException("Configuration already registered for the given class loader");
			}
		}
	}

	@Override
	public void releaseConfig(Config config) {
		// todo: see
		// https://github.com/eclipse/microprofile-config/issues/136#issuecomment-535962313
		// todo: see https://github.com/eclipse/microprofile-config/issues/471
		final Map<ClassLoader, Config> configsForClassLoader = this.configsForClassLoader;
		synchronized (configsForClassLoader) {
			configsForClassLoader.values().removeIf(v -> v == config);
		}
	}

	public void releaseConfig(ClassLoader classLoader) {
		final ClassLoader realClassLoader = getRealClassLoader(classLoader);
		final Map<ClassLoader, Config> configsForClassLoader = this.configsForClassLoader;
		synchronized (configsForClassLoader) {
			configsForClassLoader.remove(realClassLoader);
		}
	}

	static ClassLoader getRealClassLoader(ClassLoader classLoader) {
		if (classLoader == null) {
			classLoader = getContextClassLoader();
		}
		if (classLoader == null) {
			classLoader = SYSTEM_CL;
		}
		return classLoader;
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
