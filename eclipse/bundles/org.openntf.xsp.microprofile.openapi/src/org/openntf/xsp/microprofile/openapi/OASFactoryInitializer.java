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
package org.openntf.xsp.microprofile.openapi;

import org.eclipse.microprofile.openapi.spi.OASFactoryResolver;
import org.openntf.xsp.jakarta.rest.spi.RestActivationParticipant;
import org.osgi.framework.BundleContext;

import io.smallrye.openapi.spi.OASFactoryResolverImpl;

/**
 * @since 2.16.0
 */
public class OASFactoryInitializer implements RestActivationParticipant {

	@Override
	public void start(final BundleContext context) throws Exception {
		OASFactoryResolver.setInstance(new OASFactoryResolverImpl());
	}

	@Override
	public void stop(final BundleContext context) throws Exception {

	}

}
