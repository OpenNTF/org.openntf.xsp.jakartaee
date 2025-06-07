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
package org.openntf.xsp.jakartaee.module.jakartansf;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;

import org.openntf.xsp.jakartaee.module.jakartansf.io.NSFJakartaFileSystem;
import org.openntf.xsp.jakartaee.module.jakartansf.io.NSFJakartaURLStreamHandlerService;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.url.URLConstants;
import org.osgi.service.url.URLStreamHandlerService;

public class NSFJakartaModuleActivator implements BundleActivator {
	private final Collection<ServiceRegistration<?>> registrations = new ArrayList<>();
	
	@Override
	public void start(BundleContext context) throws Exception {
		Hashtable<String, Object> urlProps = new Hashtable<>();
		urlProps.put(URLConstants.URL_HANDLER_PROTOCOL, new String[] { NSFJakartaFileSystem.URLSCHEME });
		
		registrations.add(context.registerService(URLStreamHandlerService.class, new NSFJakartaURLStreamHandlerService(), urlProps));
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		registrations.forEach(ServiceRegistration::unregister);
	}

}
