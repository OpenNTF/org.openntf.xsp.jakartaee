/**
 * Copyright © 2018-2022 Jesse Gallagher
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
package org.openntf.xsp.microprofile.rest.client;

import org.eclipse.microprofile.rest.client.spi.RestClientBuilderResolver;
import org.jboss.resteasy.microprofile.client.BuilderResolver;
import org.jboss.resteasy.microprofile.client.RestClientProxy;
import org.jboss.resteasy.microprofile.client.header.ClientHeaderProviders;
import org.openntf.xsp.jakartaee.util.LibraryUtil;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class RestClientActivator implements BundleActivator {

	@Override
	public void start(BundleContext bundleContext) throws Exception {
		RestClientBuilderResolver.setInstance(new BuilderResolver());
		
		// Initialize RESTEasy's MP config with its own ClassLoader to make ServiceLoader work
		try {
		LibraryUtil.withClassLoader(RestClientProxy.class.getClassLoader(), () -> {
			// This method will return an empty optional for this method - just call it to kick off the static init
			ClientHeaderProviders.getProvider(getClass().getDeclaredMethod("start", BundleContext.class)); //$NON-NLS-1$
			return null;
		});
		} catch(Throwable t) {
			t.printStackTrace();
		}
	}

	@Override
	public void stop(BundleContext bundleContext) throws Exception {

	}

}
