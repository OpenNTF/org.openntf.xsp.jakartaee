package org.openntf.xsp.cdi.session;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.spi.CreationalContext;

/**
 * @since 2020-04
 */
public class SessionScopeContextHolder implements Serializable {
	private static final long serialVersionUID = 1L;

	private final Map<String, SessionScopeInstance<?>> beans = new HashMap<>();

	public Map<String, SessionScopeInstance<?>> getBeans() {
		return beans;
	}

	public SessionScopeInstance<?> getBean(final String className) {
		return getBeans().get(className);
	}

	public <T> void putBean(final SessionScopeInstance<T> instance) {
		getBeans().put(instance.beanClass, instance);
	}

	public <T> void destroyBean(final SessionScopeInstance<T> instance) {
		// Good to have a stub, but this currently can't happen
	}

	public static class SessionScopeInstance<T> implements Serializable {
		private static final long serialVersionUID = 1L;

		String beanClass;
        CreationalContext<T> ctx;
        @SuppressWarnings("null")
		T instance;
    }
}
