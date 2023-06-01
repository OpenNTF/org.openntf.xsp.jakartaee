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
package org.openntf.xsp.jakartaee;

import java.io.IOException;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.openntf.xsp.jakartaee.weaving.MailWeavingHook;
import org.openntf.xsp.jakartaee.weaving.UtilWeavingHook;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceRegistration;
import org.osgi.framework.hooks.weaving.WeavingHook;

import jakarta.activation.CommandMap;
import jakarta.activation.MailcapCommandMap;
import jakarta.mail.Multipart;

public class JakartaActivator implements BundleActivator {
	private final List<ServiceRegistration<?>> regs = new ArrayList<>();

	@SuppressWarnings("deprecation")
	@Override
	public void start(BundleContext context) throws Exception {
		regs.add(context.registerService(WeavingHook.class.getName(), new UtilWeavingHook(), null));
		regs.add(context.registerService(WeavingHook.class.getName(), new MailWeavingHook(), null));
		
		AccessController.doPrivileged((PrivilegedExceptionAction<Void>)() -> {
			// The below tries to load jnotes when run in Tycho Surefire
			String application = String.valueOf(System.getProperty("eclipse.application")); //$NON-NLS-1$
			if(!application.contains("org.eclipse.tycho")) { //$NON-NLS-1$
				ClassLoader tccl = Thread.currentThread().getContextClassLoader();
				Thread.currentThread().setContextClassLoader(new MailcapAvoidanceClassLoader(tccl));
				try {
					// Set an explicit mailcap based on this thread context, to avoid reading mail.jar
					MailcapCommandMap map = new MailcapCommandMap();
					CommandMap.setDefaultCommandMap(map);
				} catch(Throwable t) {
					t.printStackTrace();
					throw t;
				} finally {
					Thread.currentThread().setContextClassLoader(tccl);
				}
			}
			return null;
		});
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		regs.forEach(ServiceRegistration::unregister);
		regs.clear();
	}
	
	private static class MailcapAvoidanceClassLoader extends ClassLoader {
		public MailcapAvoidanceClassLoader(ClassLoader parent) {
			super(parent);
		}
		
		@Override
		public Enumeration<URL> getResources(String name) throws IOException {
			if("META-INF/mailcap".equals(name)) { //$NON-NLS-1$
				// By default, this will find nothing. Then, in turn,
				//   Activation will search the system and find the old
				//   one in ndext. So, we give a real URL here
				return FrameworkUtil.getBundle(Multipart.class).getResources(name);
			}
			return super.getResources(name);
		}
	}
}
