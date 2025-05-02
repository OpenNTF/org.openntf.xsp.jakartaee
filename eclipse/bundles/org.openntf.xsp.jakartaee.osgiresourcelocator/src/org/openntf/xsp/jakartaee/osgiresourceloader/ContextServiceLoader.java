package org.openntf.xsp.jakartaee.osgiresourceloader;

/**
 * @since 3.4.0
 */
public interface ContextServiceLoader {
	@SuppressWarnings("rawtypes")
	Iterable<Class> resolveModuleServices(final Class<?> serviceClass);
}
