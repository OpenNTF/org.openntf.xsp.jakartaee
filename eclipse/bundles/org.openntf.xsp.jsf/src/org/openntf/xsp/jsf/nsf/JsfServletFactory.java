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
package org.openntf.xsp.jsf.nsf;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.servlet.Servlet;
import javax.servlet.ServletException;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.osgi.util.ManifestElement;
import org.openntf.xsp.jakartaee.MappingBasedServletFactory;
import org.openntf.xsp.jakartaee.servlet.ServletUtil;
import org.openntf.xsp.jakartaee.util.LibraryUtil;
import org.openntf.xsp.jsf.JsfLibrary;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.osgi.framework.FrameworkUtil;

import com.ibm.commons.util.StringUtil;
import com.ibm.designer.runtime.domino.adapter.ComponentModule;

import jakarta.faces.webapp.FacesServlet;

/**
 * 
 * @author Jesse Gallagher
 * @since 2.4.0
 */
public class JsfServletFactory extends MappingBasedServletFactory {
	public JsfServletFactory() {
	}
	
	@Override
	public String getLibraryId() {
		return JsfLibrary.LIBRARY_ID;
	}
	
	@Override
	public Set<String> getExtensions() {
		return new HashSet<>(Arrays.asList(".xhtml", ".jsf")); //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Override
	public String getServletClassName() {
		return FacesServlet.class.getName();
	}
	
	@Override
	public Servlet createExecutorServlet(ComponentModule module) throws ServletException {
		try {
			return AccessController.doPrivileged((PrivilegedExceptionAction<Servlet>)() -> {
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
					
					return module.createServlet(ServletUtil.newToOld((jakarta.servlet.Servlet)new NSFJsfServlet(module)), "XSP JSF Servlet", params); //$NON-NLS-1$
				} finally {
					Thread.currentThread().setContextClassLoader(current);
				}
			});
		} catch (PrivilegedActionException e) {
			Throwable cause = e.getCause();
			if(cause instanceof ServletException) {
				throw (ServletException)cause;
			} else if(cause != null) {
				throw new ServletException(cause);
			} else {
				throw new ServletException(e);
			}
		}
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
					Optional<Bundle> dep = LibraryUtil.getBundle(element.getValue());
					if(dep.isPresent()) {
						toClasspathEntry(dep.get(), classpath);
					}
				}
			}
		}
	}
}
