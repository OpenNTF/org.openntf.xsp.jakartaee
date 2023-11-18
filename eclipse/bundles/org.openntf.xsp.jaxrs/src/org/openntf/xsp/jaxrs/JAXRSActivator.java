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
package org.openntf.xsp.jaxrs;

import org.jboss.resteasy.client.jaxrs.internal.ResteasyClientBuilderImpl;
import org.jboss.resteasy.core.providerfactory.ResteasyProviderFactoryImpl;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.openntf.xsp.jaxrs.impl.DelegatingRuntimeDelegate;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import jakarta.ws.rs.ext.RuntimeDelegate;

public class JAXRSActivator implements BundleActivator {

	@Override
	public void start(BundleContext context) throws Exception {
		//ResteasyProviderFactory.setInstance(DelegatingRuntimeDelegate.INSTANCE);
		RuntimeDelegate.setInstance(new ResteasyProviderFactoryImpl());
//		ResteasyClientBuilderImpl.setProviderFactory(DelegatingRuntimeDelegate.INSTANCE);
	}

	@Override
	public void stop(BundleContext context) throws Exception {

	}

}
