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
package org.openntf.xsp.microprofile.health;

import org.eclipse.microprofile.health.HealthCheckResponse;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import io.smallrye.health.ResponseProvider;

/**
 * @author Jesse Gallagher
 * @since 2.2.0
 */
public class HealthActivator implements BundleActivator {

	@Override
	public void start(final BundleContext bundleContext) throws Exception {
		// Set the provider instance manually since we're in Domino OSGi
		HealthCheckResponse.setResponseProvider(new ResponseProvider());
	}

	@Override
	public void stop(final BundleContext bundleContext) throws Exception {

	}

}
