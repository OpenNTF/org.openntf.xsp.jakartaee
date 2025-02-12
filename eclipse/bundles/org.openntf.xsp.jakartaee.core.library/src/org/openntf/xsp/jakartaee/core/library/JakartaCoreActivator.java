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
package org.openntf.xsp.jakartaee.core.library;

import org.openntf.xsp.jakarta.cdi.CDIActivator;
import org.openntf.xsp.jakarta.concurrency.ConcurrencyActivator;
import org.openntf.xsp.jakarta.persistence.PersistenceActivator;
import org.openntf.xsp.jakarta.validation.ValidationActivator;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class JakartaCoreActivator implements BundleActivator {

	@Override
	public void start(final BundleContext context) throws Exception {
		// Make sure some important activators are run
		@SuppressWarnings("unused")
		Class<?>[] c = {
			PersistenceActivator.class,
			CDIActivator.class,
			ConcurrencyActivator.class,
			ValidationActivator.class
		};
	}

	@Override
	public void stop(final BundleContext context) throws Exception {

	}

}
