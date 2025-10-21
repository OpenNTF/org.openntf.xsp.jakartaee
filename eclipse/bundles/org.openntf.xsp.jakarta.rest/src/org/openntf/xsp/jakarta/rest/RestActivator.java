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
package org.openntf.xsp.jakarta.rest;

import org.jboss.resteasy.core.providerfactory.ResteasyProviderFactoryImpl;
import org.openntf.xsp.jakarta.rest.spi.RestActivationParticipant;
import org.openntf.xsp.jakartaee.util.LibraryUtil;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import jakarta.ws.rs.ext.RuntimeDelegate;

/**
 * @since 2.16.0
 */
public class RestActivator implements BundleActivator {

	@Override
	public void start(final BundleContext context) throws Exception {
		if(!LibraryUtil.isNotes()) {
			RuntimeDelegate.setInstance(new ResteasyProviderFactoryImpl());
	
			for(RestActivationParticipant p : LibraryUtil.findExtensions(RestActivationParticipant.class)) {
				p.start(context);
			}
		}
	}

	@Override
	public void stop(final BundleContext context) throws Exception {
		for(RestActivationParticipant p : LibraryUtil.findExtensions(RestActivationParticipant.class)) {
			p.stop(context);
		}
	}

}
