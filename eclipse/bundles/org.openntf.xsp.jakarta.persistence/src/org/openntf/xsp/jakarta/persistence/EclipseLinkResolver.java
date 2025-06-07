/**
 * Copyright (c) 2018-2025 Contributors to the XPages Jakarta EE Support Project
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
package org.openntf.xsp.jakarta.persistence;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import jakarta.persistence.spi.PersistenceProvider;
import jakarta.persistence.spi.PersistenceProviderResolver;

public class EclipseLinkResolver implements PersistenceProviderResolver {

	private List<PersistenceProvider> providers;

	public EclipseLinkResolver() {
		clearCachedProviders();
	}

	@Override
	public List<PersistenceProvider> getPersistenceProviders() {
		return providers;
	}

	@Override
	public void clearCachedProviders() {
		this.providers = Collections.singletonList(new XSPPersistenceProvider());
	}

	private static class XSPPersistenceProvider extends org.eclipse.persistence.jpa.PersistenceProvider {
		@Override
		public ClassLoader getClassLoader(final String emName, @SuppressWarnings("rawtypes") final Map properties) {
			return new AvoidingClassLoader(Thread.currentThread().getContextClassLoader());
		}
	}

	private static class AvoidingClassLoader extends ClassLoader {
		public AvoidingClassLoader(final ClassLoader delegate) {
			super(delegate);
		}

		@Override
		public Class<?> loadClass(final String name) throws ClassNotFoundException {
			if(name != null && name.startsWith("org.eclipse.persistence")) { //$NON-NLS-1$
				return org.eclipse.persistence.jpa.PersistenceProvider.class.getClassLoader().loadClass(name);
			} else {
				return super.loadClass(name);
			}
		}
	}

}
