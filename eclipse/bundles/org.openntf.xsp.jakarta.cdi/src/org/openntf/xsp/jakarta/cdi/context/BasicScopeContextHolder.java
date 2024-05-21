/**
 * Copyright (c) 2018-2024 Contributors to the XPages Jakarta EE Support Project
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
package org.openntf.xsp.jakarta.cdi.context;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.enterprise.context.spi.CreationalContext;

/**
 * @since 1.2.0
 */
public class BasicScopeContextHolder implements Serializable {
	private static final long serialVersionUID = 1L;

	private final Map<String, BasicScopeInstance<?>> beans = new ConcurrentHashMap<>();

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
		
		public String getBeanClass() {
			return beanClass;
		}
		public void setBeanClass(String beanClass) {
			this.beanClass = beanClass;
		}
		public CreationalContext<T> getCtx() {
			return ctx;
		}
		public void setCtx(CreationalContext<T> ctx) {
			this.ctx = ctx;
		}
		public T getInstance() {
			return instance;
		}
		public void setInstance(T instance) {
			this.instance = instance;
		}
    }
}
