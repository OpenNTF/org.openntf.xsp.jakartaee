/**
 * Copyright © 2018-2021 Jesse Gallagher
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
package org.openntf.xsp.jsp;

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

import org.apache.jasper.runtime.JspFactoryImpl;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

import com.ibm.commons.util.StringUtil;

//import org.openntf.xsp.jsp.webapp.JspExtensionFactory;

import com.ibm.designer.runtime.domino.adapter.HttpService;
import com.ibm.designer.runtime.domino.adapter.IServiceFactory;
import com.ibm.designer.runtime.domino.adapter.LCDEnvironment;
import com.ibm.domino.napi.c.Os;
import com.ibm.domino.xsp.module.nsf.NSFService;
//import com.ibm.ws.webcontainer.WebContainer;
//import com.ibm.wsspi.webcontainer.logging.LoggerFactory;

import jakarta.servlet.Servlet;
import jakarta.servlet.jsp.JspFactory;

/**
 * This {@link IServiceFactory} doesn't provide any HTTP services, but is used to
 * enable hooks very early in the HTTP init process.
 * 
 * @author Jesse Gallagher
 * @since 2.1.0
 */
public class EarlyInitFactory implements IServiceFactory {
	public static boolean debug = true;

	@Override
	public HttpService[] getServices(LCDEnvironment env) {
		try {
			initWebContainer();
		} catch(Throwable t) {
			t.printStackTrace();
		}
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
	 * Adds JSP support to bundle-based web applications.
	 */
	private void initWebContainer() {
//		if(debug) {
//			Logger logger = LoggerFactory.getInstance().getLogger("com.ibm.ws.webcontainer.servlet"); //$NON-NLS-1$
//			logger.setLevel(Level.ALL);
//		}
//		WebContainer.addExtensionFactory(new JspExtensionFactory());
	}
	
	/**
	 * Adds JSP support to NSFs.
	 */
	private void initNsf() {
		// Register ".jsp" with the NSF service, which will then pass along to JspServletFactory
		NSFService.addHandledExtensions(".jsp"); //$NON-NLS-1$
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
	
	public static Path getServletDtdPath() {
		String data = Os.OSGetDataDirectory();
		Path dataDir = Paths.get(data);
		return dataDir.resolve("jakarta").resolve("dtd"); //$NON-NLS-1$ //$NON-NLS-2$
	}

}
