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
package org.openntf.xsp.jakarta.nosql;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import com.ibm.commons.util.StringUtil;
import com.ibm.domino.napi.c.Os;

import org.openntf.xsp.jakarta.nosql.communication.driver.lsxbe.util.DominoNoSQLUtil;
import org.openntf.xsp.jakarta.nosql.weaving.NoSQLWeavingHook;
import org.openntf.xsp.jakartaee.util.LibraryUtil;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.framework.hooks.weaving.WeavingHook;

/**
 * @author Jesse Gallagher
 * @since 2.3.0
 */
public class NoSQLActivator implements BundleActivator {
	/**
	 * notes.ini property that can be set to specify a temp directory.
	 * @since 3.1.0
	 */
	public static final String PROP_OVERRIDEQRPDIR = "Jakarta_QRPDir"; //$NON-NLS-1$

	private final List<ServiceRegistration<?>> regs = new ArrayList<>();

	@Override
	public void start(final BundleContext context) throws Exception {
		regs.add(context.registerService(WeavingHook.class.getName(), new NoSQLWeavingHook(), null));

		if(!LibraryUtil.isTycho()) {
			// Check for a notes.ini property overriding the scratch directory

			String iniTempDir = Os.OSGetEnvironmentString(PROP_OVERRIDEQRPDIR);
			if(StringUtil.isNotEmpty(iniTempDir)) {
				Path tempDir = Paths.get(iniTempDir);
				if(!tempDir.isAbsolute()) {
					Path dataDir = Paths.get(Os.OSGetDataDirectory());
					tempDir = dataDir.resolve(iniTempDir);
				}
				DominoNoSQLUtil.setQrpDirectory(tempDir);
			}

			// May have been set to a custom value
			DominoNoSQLUtil.setTempDirectory(LibraryUtil.getTempDirectory());
		}
	}

	@Override
	public void stop(final BundleContext context) throws Exception {
		regs.forEach(ServiceRegistration::unregister);
		regs.clear();
	}

}
