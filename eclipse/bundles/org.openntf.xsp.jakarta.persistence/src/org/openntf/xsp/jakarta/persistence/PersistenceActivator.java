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
package org.openntf.xsp.jakarta.persistence;

import org.eclipse.persistence.exceptions.EclipseLinkException;
import org.openntf.xsp.jakartaee.util.LibraryUtil;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import jakarta.persistence.spi.PersistenceProviderResolverHolder;

public class PersistenceActivator implements BundleActivator {

	@Override
	public void start(final BundleContext bundleContext) throws Exception {
		LibraryUtil.setSystemProperty("eclipselink.logging.logger", "JavaLogger"); //$NON-NLS-1$ //$NON-NLS-2$
		PersistenceProviderResolverHolder.setPersistenceProviderResolver(new EclipseLinkResolver());

		EclipseLinkException.setShouldPrintInternalException(true);
	}

	@Override
	public void stop(final BundleContext bundleContext) throws Exception {

	}

}
