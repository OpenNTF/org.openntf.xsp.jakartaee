/**
 * Copyright © 2018-2022 Contributors to the XPages Jakarta EE Support Project
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
package org.openntf.xsp.jaxrs;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.servlet.Servlet;
import javax.servlet.ServletException;

import org.jboss.resteasy.plugins.server.servlet.ResteasyContextParameters;
import org.openntf.xsp.jakartaee.servlet.ServletUtil;
import org.openntf.xsp.jaxrs.impl.FacesJAXRSServletContainer;
import org.openntf.xsp.jaxrs.impl.NSFJAXRSApplication;

import com.ibm.commons.util.PathUtil;
import com.ibm.commons.util.StringUtil;
import com.ibm.designer.runtime.domino.adapter.ComponentModule;
import com.ibm.designer.runtime.domino.adapter.IServletFactory;
import com.ibm.designer.runtime.domino.adapter.ServletMatch;

/**
 * An {@link IServletFactory} implementation that provides a Jersey servlet in the context
 * of an NSF.
 * 
 * @author Martin Pradny
 * @author Jesse Gallagher
 * @since 1.0.0
 */
public class JAXRSServletFactory implements IServletFactory {
	public static final String SERVLET_PATH_DEFAULT = "app"; //$NON-NLS-1$
	public static final String PROP_SERVLET_PATH = "org.openntf.xsp.jaxrs.path"; //$NON-NLS-1$
	
	private static final String ATTR_PATH = JAXRSServletFactory.class.getName()+"_path"; //$NON-NLS-1$
	private static final String ATTR_REFRESH = JAXRSServletFactory.class.getName()+"_refresh"; //$NON-NLS-1$
	
	/**
	 * Determines the effective base servlet path for the provided module.
	 * 
	 * @param module the {@link ComponentModule} housing the servlet.
	 * @return the base servlet path for JAX-RS, e.g. {@code "/xsp/.jaxrs/"}
	 */
	public static String getServletPath(ComponentModule module) {
		Map<String, Object> attrs = module.getAttributes();

		// Module attributes aren't reset on app refresh, so check here
		Object refresh = attrs.get(ATTR_REFRESH);
		if(refresh == null || (Long)refresh < module.getLastRefresh()) {
			attrs.remove(ATTR_PATH);
		}
		attrs.put(ATTR_REFRESH, module.getLastRefresh());
		
		String path = (String)attrs.computeIfAbsent(JAXRSServletFactory.class.getName()+"_path", key -> { //$NON-NLS-1$
			Properties props = new Properties();
			try(InputStream is = module.getResourceAsStream("/WEB-INF/xsp.properties")) { //$NON-NLS-1$
				if(is != null) {
					props.load(is);
				} 
			} catch(IOException e) {
				throw new UncheckedIOException(e);
			}
			
			return props.getProperty(PROP_SERVLET_PATH);
		});
		if(StringUtil.isEmpty(path)) {
			path = SERVLET_PATH_DEFAULT;
		}
		path = PathUtil.concat("/xsp", path, '/'); //$NON-NLS-1$
		if(!path.endsWith("/")) { //$NON-NLS-1$
			path += "/"; //$NON-NLS-1$
		}
		return path;
	}

	private ComponentModule module;
	private Servlet servlet;
	private long lastUpdate;

	@Override
	public void init(ComponentModule module) {
		this.module = module;
		this.lastUpdate = module.getLastRefresh();
	}

	@Override
	public ServletMatch getServletMatch(String contextPath, String path) throws ServletException {
		if(LibraryUtil.isLibraryActive(JAXRSLibrary.LIBRARY_ID)) {
			String baseServletPath = getServletPath(module);
			// Match either a resource within the path or the specific base path without the trailing "/"
			String trimmedBaseServletPath = baseServletPath.substring(0, baseServletPath.length()-1);
			if (path.startsWith(baseServletPath) || path.equals(trimmedBaseServletPath)) {
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
			params.put("jakarta.ws.rs.Application", NSFJAXRSApplication.class.getName()); //$NON-NLS-1$
			// TODO move this to the fragment somehow
			params.put("resteasy.injector.factory", "org.openntf.xsp.jaxrs.weld.NSFCdiInjectorFactory"); //$NON-NLS-1$ //$NON-NLS-2$
			params.put(ResteasyContextParameters.RESTEASY_SERVLET_MAPPING_PREFIX, getServletPath(module));
			
			servlet = module.createServlet(ServletUtil.newToOld((jakarta.servlet.Servlet)new FacesJAXRSServletContainer(module)), "XSP JAX-RS Servlet", params); //$NON-NLS-1$
			lastUpdate = this.module.getLastRefresh();
		}
		return servlet;
	}
}