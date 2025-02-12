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
package org.openntf.xsp.jakarta.faces.nsf;

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

import com.ibm.commons.util.StringUtil;
import com.ibm.designer.runtime.domino.adapter.ComponentModule;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.osgi.util.ManifestElement;
import org.openntf.xsp.jakarta.cdi.util.ContainerUtil;
import org.openntf.xsp.jakartaee.MappingBasedServletFactory;
import org.openntf.xsp.jakartaee.servlet.ServletUtil;
import org.openntf.xsp.jakartaee.util.LibraryUtil;
import org.openntf.xsp.jakartaee.util.ModuleUtil;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.osgi.framework.FrameworkUtil;

import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.faces.annotation.View;
import jakarta.faces.application.ResourceHandler;
import jakarta.faces.view.facelets.Facelet;
import jakarta.faces.webapp.FacesServlet;

/**
 *
 * @author Jesse Gallagher
 * @since 2.4.0
 */
public class FacesServletFactory extends MappingBasedServletFactory {

	@Override
	public void init(final ComponentModule module) {
		super.init(module);

		Map<String, String> contextParams = ServletUtil.getWebXmlParams(module);
		if("true".equalsIgnoreCase(contextParams.get(FacesServlet.AUTOMATIC_EXTENSIONLESS_MAPPING_PARAM_NAME))) { //$NON-NLS-1$
			// If so, look for .xhtml and .jsf files and push them to known extensions
			Set<String> exts = getExtensions();
			ModuleUtil.listFiles(module, null)
				.filter(f -> !f.startsWith("WEB-INF/")) //$NON-NLS-1$
				.forEach(f -> {
					for(String ext : exts) {
						if(f.endsWith(ext)) {
							this.addExplicitEndpoint("/xsp/" + f.substring(0, f.length()-ext.length()), '/' + f); //$NON-NLS-1$
						}
					}
				});
		}
	}

	@Override
	public String getLibraryId() {
		return LibraryUtil.LIBRARY_UI;
	}

	@Override
	public Set<String> getExtensions() {
		return new HashSet<>(Arrays.asList(".xhtml", ".jsf", ".faces")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	@Override
	public String getServletClassName() {
		return FacesServlet.class.getName();
	}

	@Override
	protected boolean checkExists(final String servletPath, final String pathInfo) {
		if(servletPath.startsWith(ResourceHandler.RESOURCE_IDENTIFIER)) {
			return true;
		}
		ComponentModule module = getModule();
		if(module.getResourceAsStream(servletPath) != null) {
			return true;
		} else {
			// Check CDI for a matching servlet
			CDI<Object> cdi = ContainerUtil.getContainer(module);
			if(cdi != null) {
				Instance<Facelet> facelet = cdi.select(Facelet.class, View.Literal.of(servletPath));
				return facelet.isResolvable();
			}
			return false;
		}
	}

	@SuppressWarnings({ "removal", "deprecation" })
	@Override
	public Servlet createExecutorServlet(final ComponentModule module) throws ServletException {
		try {
			return AccessController.doPrivileged((PrivilegedExceptionAction<Servlet>)() -> {
				ClassLoader current = Thread.currentThread().getContextClassLoader();
				try {
					Map<String, String> params = new HashMap<>();

					URL[] urls = buildBundleClassPath().stream()
						.map(t -> {
							try {
								return t.toURI().toURL();
							} catch (MalformedURLException e) {
								throw new UncheckedIOException(e);
							}
						})
						.toArray(URL[]::new);
					Thread.currentThread().setContextClassLoader(new URLClassLoader(urls, current));

					return module.createServlet(ServletUtil.newToOld((jakarta.servlet.Servlet)new NSFFacesServlet(module)), "XSP JSF Servlet", params); //$NON-NLS-1$
				} finally {
					Thread.currentThread().setContextClassLoader(current);
				}
			});
		} catch (PrivilegedActionException e) {
			Throwable cause = e.getCause();
			if(cause instanceof ServletException se) {
				throw se;
			} else if(cause != null) {
				throw new ServletException(cause);
			} else {
				throw new ServletException(e);
			}
		}
	}

	public static List<File> buildBundleClassPath() throws BundleException, IOException {
		Bundle bundle = FrameworkUtil.getBundle(FacesServletFactory.class);
		List<File> classpath = new ArrayList<>();
		toClasspathEntry(bundle, classpath);

		return classpath;
	}

	private static void toClasspathEntry(final Bundle bundle, final List<File> classpath) throws BundleException, IOException {
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
