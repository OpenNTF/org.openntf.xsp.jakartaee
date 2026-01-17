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
package org.openntf.xsp.jakartaee;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.AccessController;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import com.ibm.commons.util.StringUtil;
import com.ibm.domino.napi.c.Os;

import org.openntf.xsp.jakartaee.util.LibraryUtil;
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
	/**
	 * notes.ini property that can be set to specify a temp directory.
	 * @since 3.1.0
	 */
	public static final String PROP_OVERRIDETEMPDIR = "Jakarta_TempDir"; //$NON-NLS-1$

	private final List<ServiceRegistration<?>> regs = new ArrayList<>();

	@SuppressWarnings("deprecation")
	@Override
	public void start(final BundleContext context) throws Exception {
		regs.add(context.registerService(WeavingHook.class.getName(), new UtilWeavingHook(), null));
		regs.add(context.registerService(WeavingHook.class.getName(), new MailWeavingHook(), null));

		// The below tries to load jnotes when run in Tycho Surefire
		if(!(LibraryUtil.isTycho() || LibraryUtil.isNotes())) {
			AccessController.doPrivileged((PrivilegedExceptionAction<Void>)() -> {
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

				// Look for a custom temporary directory path
				// https://github.com/OpenNTF/org.openntf.xsp.jakartaee/issues/554
				String iniTempDir = Os.OSGetEnvironmentString(PROP_OVERRIDETEMPDIR);
				if(StringUtil.isNotEmpty(iniTempDir)) {
					Path tempDir = Paths.get(iniTempDir);
					if(!tempDir.isAbsolute()) {
						Path dataDir = Paths.get(Os.OSGetDataDirectory());
						tempDir = dataDir.resolve(iniTempDir);
					}
					LibraryUtil.setTempDirectory(tempDir);
				}
				return null;
			});
		}

		// Allow UTF-8-encoded filenames in MimeMultipart
		// https://github.com/OpenNTF/org.openntf.xsp.jakartaee/issues/501
		LibraryUtil.setSystemProperty("mail.mime.allowutf8", "true"); //$NON-NLS-1$ //$NON-NLS-2$

	}

	@Override
	public void stop(final BundleContext context) throws Exception {
		regs.forEach(ServiceRegistration::unregister);
		regs.clear();
	}

	private static class MailcapAvoidanceClassLoader extends ClassLoader {
		public MailcapAvoidanceClassLoader(final ClassLoader parent) {
			super(parent);
		}

		@Override
		public Enumeration<URL> getResources(final String name) throws IOException {
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
