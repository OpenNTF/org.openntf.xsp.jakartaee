package org.openntf.xsp.cdi.context;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.spi.CreationalContext;

/**
 * @since 1.2.0
 */
public class BasicScopeContextHolder implements Serializable {
	private static final long serialVersionUID = 1L;

	private final Map<String, BasicScopeInstance<?>> beans = new HashMap<>();

	public Map<String, BasicScopeInstance<?>> getBeans() {
		return beans;
	}

	public BasicScopeInstance<?> getBean(final String className) {
		return getBeans().get(className);
	}

	public <T> void putBean(final BasicScopeInstance<T> instance) {
		getBeans().put(instance.beanClass, instance);
	}

	public <T> void destroyBean(final BasicScopeInstance<T> instance) {
		// Good to have a stub, but this currently can't happen
		getBeans().remove(instance.beanClass);
	}

	public static class BasicScopeInstance<T> implements Serializable {
		private static final long serialVersionUID = 1L;

		String beanClass;
        CreationalContext<T> ctx;
		T instance;
    }
}
