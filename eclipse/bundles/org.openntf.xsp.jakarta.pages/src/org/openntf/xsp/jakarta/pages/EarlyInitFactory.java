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
package org.openntf.xsp.jakarta.pages;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ibm.commons.util.StringUtil;
import com.ibm.designer.runtime.domino.adapter.HttpService;
import com.ibm.designer.runtime.domino.adapter.IServiceFactory;
import com.ibm.designer.runtime.domino.adapter.LCDEnvironment;
import com.ibm.domino.napi.NException;
import com.ibm.domino.napi.c.Os;
import com.ibm.domino.xsp.module.nsf.NSFService;

import org.eclipse.core.runtime.FileLocator;
import org.openntf.xsp.jakartaee.util.LibraryUtil;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

import jakarta.servlet.Servlet;

/**
 * This {@link IServiceFactory} doesn't provide any HTTP services, but is used to
 * enable hooks very early in the HTTP init process.
 *
 * @author Jesse Gallagher
 * @since 2.1.0
 */
public class EarlyInitFactory implements IServiceFactory {
	/**
	 * notes.ini property that can be set to specify a DTD output directory.
	 * @since 3.1.0
	 */
	public static final String PROP_OVERRIDEDTDDIR = "Jakarta_DTDDir"; //$NON-NLS-1$

	private static final Logger log = Logger.getLogger(EarlyInitFactory.class.getPackageName());

	@Override
	public HttpService[] getServices(final LCDEnvironment env) {
		try {
			initNsf();
		} catch(Throwable t) {
			t.printStackTrace();
		}
		try {
			deployServletDtds();
		} catch(Throwable t) {
			t.printStackTrace();
		}


		return null;
	}

	/**
	 * Adds Jakarta Pages support to NSFs.
	 */
	private void initNsf() {
		// Register ".jsp" with the NSF service, which will then pass along to PagesServletFactory
		NSFService.addHandledExtensions(".jsp"); //$NON-NLS-1$

		// This property is used by Wasp to see if it should use AccessController blocks
		LibraryUtil.setSystemProperty("package.definition", "org.apache.jsp"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	private void deployServletDtds() throws URISyntaxException, IOException {
		Path destDir = getServletDtdPath();
		Files.createDirectories(destDir);

		Bundle servlet = FrameworkUtil.getBundle(Servlet.class);
		Enumeration<String> resources = servlet.getEntryPaths("/jakarta/servlet/resources/"); //$NON-NLS-1$
		for(String res : Collections.list(resources)) {
			URL url = servlet.getResource(res);

			String baseName = res.substring(res.lastIndexOf('/')+1);
			if(!baseName.isEmpty()) {
				Path dest = destDir.resolve(baseName);
				if(!Files.isRegularFile(dest)) {
					try(InputStream is = url.openStream()) {
						Files.copy(is, dest, StandardCopyOption.REPLACE_EXISTING);
					}
				}
			}
		}
	}

	public static Path getDeployedJstlBundle() throws IOException {
		Path destDir = getServletDtdPath();
		Files.createDirectories(destDir);

		Bundle jstl = LibraryUtil.getBundle("org.glassfish.web.jakarta.servlet.jsp.jstl").get(); //$NON-NLS-1$
		Path jstlDest = destDir.resolve(jstl.getSymbolicName() + "_" + jstl.getVersion() + ".jar"); //$NON-NLS-1$ //$NON-NLS-2$
		if(!Files.exists(jstlDest)) {
			Path jstlSource = FileLocator.getBundleFile(jstl).toPath();
			Files.copy(jstlSource, jstlDest);
		}

		return jstlDest;
	}

	public static Path getServletDtdPath() {
		try {
			String iniDtdDir = Os.OSGetEnvironmentString(PROP_OVERRIDEDTDDIR);
			if(StringUtil.isNotEmpty(iniDtdDir)) {
				Path dtdDir = Paths.get(iniDtdDir);
				if(!dtdDir.isAbsolute()) {
					Path dataDir = Paths.get(Os.OSGetDataDirectory());
					dtdDir = dataDir.resolve(iniDtdDir);
				}
				return dtdDir;
			}
		} catch(NException e) {
			if(log.isLoggable(Level.WARNING)) {
				log.log(Level.WARNING, "Encountered exception trying to read notes.ini", e);
			}
		}

		String data = Os.OSGetDataDirectory();
		Path dataDir = Paths.get(data);
		return dataDir.resolve("domino").resolve("jakarta").resolve("dtd"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

}
