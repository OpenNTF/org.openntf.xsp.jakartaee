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
package org.openntf.xsp.jsf.nsf;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.Servlet;
import javax.servlet.ServletException;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.eclipse.osgi.util.ManifestElement;
import org.openntf.xsp.jakartaee.LibraryUtil;
import org.openntf.xsp.jakartaee.servlet.ServletUtil;
import org.openntf.xsp.jsf.JsfLibrary;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.osgi.framework.FrameworkUtil;

import com.ibm.commons.util.StringUtil;
import com.ibm.designer.runtime.domino.adapter.ComponentModule;
import com.ibm.designer.runtime.domino.adapter.IServletFactory;
import com.ibm.designer.runtime.domino.adapter.ServletMatch;

/**
 * 
 * @author Jesse Gallagher
 * @since 2.1.0
 */
public class JsfServletFactory implements IServletFactory {
	private static final String PATH_SEP = AccessController.doPrivileged((PrivilegedAction<String>)() -> System.getProperty("path.separator")); //$NON-NLS-1$

	private ComponentModule module;
	private Servlet servlet;
	private long lastUpdate;

	public JsfServletFactory() {
	}

	@Override
	public void init(ComponentModule module) {
		this.module = module;
		this.lastUpdate = module.getLastRefresh();
	}

	@Override
	public ServletMatch getServletMatch(String contextPath, String path) throws ServletException {
		try {
			if(LibraryUtil.usesLibrary(JsfLibrary.LIBRARY_ID, module)) {
				int jspIndex = StringUtil.toString(path).indexOf(".xhtml"); //$NON-NLS-1$
				if (jspIndex > -1) {
					String servletPath = path.substring(0, jspIndex+4);
					String pathInfo = path.substring(jspIndex+4);
					return new ServletMatch(getExecutorServlet(), servletPath, pathInfo);
				}
			}
		} catch (IOException e) {
			throw new ServletException(e);
		}
		return null;
	}
	
	public synchronized Servlet getExecutorServlet() throws ServletException {
		if (servlet == null || lastUpdate < this.module.getLastRefresh()) {
			try {
				this.servlet = AccessController.doPrivileged((PrivilegedExceptionAction<Servlet>)() -> {
					ClassLoader current = Thread.currentThread().getContextClassLoader();
					try {

						
						Map<String, String> params = new HashMap<>();
						
						@SuppressWarnings("deprecation")
						URL[] urls = buildBundleClassPath().stream()
							.map(t -> {
								try {
									return t.toURL();
								} catch (MalformedURLException e) {
									throw new UncheckedIOException(e);
								}
							})
							.toArray(URL[]::new);
						Thread.currentThread().setContextClassLoader(new URLClassLoader(urls, current));
						
						return module.createServlet(ServletUtil.newToOld((jakarta.servlet.Servlet)new NSFJsfServlet(module)), "XSP JSP Servlet", params); //$NON-NLS-1$
					} finally {
						Thread.currentThread().setContextClassLoader(current);
					}
				});
			} catch (PrivilegedActionException e) {
				throw new ServletException(e.getCause());
			}
			lastUpdate = this.module.getLastRefresh();
		}
		return servlet;
	}

	public static List<File> buildBundleClassPath() throws BundleException, IOException {
		Bundle bundle = FrameworkUtil.getBundle(JsfServletFactory.class);
		List<File> classpath = new ArrayList<>();
		toClasspathEntry(bundle, classpath);
		
		return classpath;
	}
	
	private static void toClasspathEntry(Bundle bundle, List<File> classpath) throws BundleException, IOException {
		// These entries MUST be filesystem paths
		classpath.add(FileLocator.getBundleFile(bundle));
		
		String req = bundle.getHeaders().get("Require-Bundle"); //$NON-NLS-1$
		if(StringUtil.isNotEmpty(req)) {
			ManifestElement[] elements = ManifestElement.parseHeader("Require-Bundle", req); //$NON-NLS-1$
			for(ManifestElement element : elements) {
				String visibility = element.getDirective("visibility"); //$NON-NLS-1$
				if("reexport".equals(visibility)) { //$NON-NLS-1$
					Bundle dep = Platform.getBundle(element.getValue());
					if(dep != null) {
						toClasspathEntry(dep, classpath);
					}
				}
			}
		}
	}
}
