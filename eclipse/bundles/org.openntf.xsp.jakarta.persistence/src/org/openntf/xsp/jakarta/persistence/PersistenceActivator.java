/**
 * Copyright (c) 2018-2023 Contributors to the XPages Jakarta EE Support Project
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

import java.security.PrivilegedAction;

import org.openntf.xsp.jakartaee.util.LibraryUtil;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import jakarta.persistence.spi.PersistenceProviderResolverHolder;

public class PersistenceActivator implements BundleActivator {

	@Override
	public void start(BundleContext bundleContext) throws Exception {
		LibraryUtil.doPrivileged((PrivilegedAction<Void>)() -> {
			System.setProperty("eclipselink.logging.logger", "JavaLogger"); //$NON-NLS-1$ //$NON-NLS-2$
			return null;
		});
		PersistenceProviderResolverHolder.setPersistenceProviderResolver(new EclipseLinkResolver());
	}

	@Override
	public void stop(BundleContext bundleContext) throws Exception {

	}

}
