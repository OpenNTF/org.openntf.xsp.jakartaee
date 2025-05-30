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
package org.openntf.xsp.jakarta.rest.nsf;

import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.servlet.Servlet;
import javax.servlet.ServletException;

import com.ibm.commons.util.PathUtil;
import com.ibm.commons.util.StringUtil;
import com.ibm.designer.runtime.domino.adapter.ComponentModule;
import com.ibm.designer.runtime.domino.adapter.ServletMatch;

import org.jboss.resteasy.cdi.CdiInjectorFactory;
import org.jboss.resteasy.plugins.server.servlet.ResteasyContextParameters;
import org.openntf.xsp.jakarta.rest.impl.JakartaRestServlet;
import org.openntf.xsp.jakarta.rest.impl.NSFRestApplication;
import org.openntf.xsp.jakartaee.module.JakartaIServletFactory;
import org.openntf.xsp.jakartaee.servlet.ServletUtil;
import org.openntf.xsp.jakartaee.util.LibraryUtil;
import org.openntf.xsp.jakartaee.util.ModuleUtil;

import jakarta.annotation.Priority;

/**
 * An {@code IServletFactory} implementation that provides a REST Servlet in the context
 * of an NSF.
 *
 * @author Martin Pradny
 * @author Jesse Gallagher
 * @since 1.0.0
 */
// Mark low priority so it comes last, for the case when an app wants it to be the root path
@Priority(-1)
public class RestServletFactory implements JakartaIServletFactory {
	public static final String SERVLET_PATH_DEFAULT = "app"; //$NON-NLS-1$
	public static final String PROP_SERVLET_PATH = "org.openntf.xsp.jakarta.rest.path"; //$NON-NLS-1$
	public static final String PROP_DEFER_TO_FILES = "org.openntf.xsp.jakarta.rest.deferToFiles"; //$NON-NLS-1$
	/**
	 * Determines the effective base servlet path for the provided module.
	 *
	 * @param module the {@link ComponentModule} housing the servlet.
	 * @return the base servlet path for JAX-RS, e.g. {@code "/xsp/.jaxrs/"}
	 */
	public static String getServletPath(final ComponentModule module) {
		Properties props = LibraryUtil.getXspProperties(module);
		String path = props.getProperty(PROP_SERVLET_PATH);
		if(StringUtil.isEmpty(path)) {
			path = SERVLET_PATH_DEFAULT;
		}
		String xspPrefix = ModuleUtil.getXspPrefix(module);
		if(StringUtil.isEmpty(xspPrefix)) {
			xspPrefix = "/"; //$NON-NLS-1$
		}
		path = PathUtil.concat(xspPrefix, path, '/');
		if(!path.endsWith("/")) { //$NON-NLS-1$
			path += "/"; //$NON-NLS-1$
		}
		return path;
	}

	private ComponentModule module;
	private Servlet servlet;
	private long lastUpdate;
	private boolean deferToFiles;

	@Override
	public void init(final ComponentModule module) {
		this.module = module;
		this.lastUpdate = module.getLastRefresh();
		Properties props = LibraryUtil.getXspProperties(module);
		this.deferToFiles = "true".equals(props.getProperty(PROP_DEFER_TO_FILES, null)); //$NON-NLS-1$
	}

	@Override
	public ServletMatch getServletMatch(final String contextPath, final String path) throws ServletException {
		if(LibraryUtil.usesLibrary(LibraryUtil.LIBRARY_CORE, module)) {
			String baseServletPath = getServletPath(module);
			// Match either a resource within the path or the specific base path without the trailing "/"
			String trimmedBaseServletPath = baseServletPath.substring(0, baseServletPath.length()-1);
			if (path.startsWith(baseServletPath) || path.equals(trimmedBaseServletPath)) {
				// If we defer to files, make sure there isn't a matching resource here
				if(this.deferToFiles && StringUtil.isNotEmpty(path) && !"/".equals(path)) { //$NON-NLS-1$
					String resPath = path.substring(1);
					try {
						if(module.getResource(resPath) != null) {
							return null;
						}
					} catch (MalformedURLException e) {
						// Ignore
					}
				}
				
				int len = baseServletPath.length()-1;
				String servletPath = path.substring(0, len);
				if(servletPath.endsWith("/")) { //$NON-NLS-1$
					servletPath = servletPath.substring(0, servletPath.length()-1);
				}
				String pathInfo = path.substring(len);
				return new ServletMatch(getExecutorServlet(), servletPath, pathInfo);
			}
		}
		return null;
	}

	public synchronized Servlet getExecutorServlet() throws ServletException {
		if (servlet == null || lastUpdate < this.module.getLastRefresh()) {
			Map<String, String> params = new HashMap<>();
			params.put("jakarta.ws.rs.Application", NSFRestApplication.class.getName()); //$NON-NLS-1$
			params.put("resteasy.injector.factory", CdiInjectorFactory.class.getName()); //$NON-NLS-1$
			params.put(ResteasyContextParameters.RESTEASY_SERVLET_MAPPING_PREFIX, getServletPath(module));
			params.put("resteasy.use.deployment.sensitive.factory", "true"); //$NON-NLS-1$ //$NON-NLS-2$

			servlet = module.createServlet(ServletUtil.newToOld((jakarta.servlet.Servlet)new JakartaRestServlet(module)), "Jakarta REST Servlet", params); //$NON-NLS-1$
			lastUpdate = this.module.getLastRefresh();
		}
		return servlet;
	}
}