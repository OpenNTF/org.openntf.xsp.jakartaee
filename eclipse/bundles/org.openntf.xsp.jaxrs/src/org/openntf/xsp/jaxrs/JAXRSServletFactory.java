/**
 * Copyright © 2018-2021 Martin Pradny and Jesse Gallagher
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

import java.util.HashMap;
import java.util.Map;

import javax.servlet.Servlet;
import javax.servlet.ServletException;

import org.jboss.resteasy.plugins.server.servlet.ResteasyContextParameters;
import org.openntf.xsp.jakartaee.servlet.ServletUtil;
import org.openntf.xsp.jaxrs.impl.FacesJAXRSServletContainer;
import org.openntf.xsp.jaxrs.impl.NSFJAXRSApplication;

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

	private ComponentModule module;
	public static final String SERVLET_PATH = "/xsp/.jaxrs/"; //$NON-NLS-1$
	private Servlet servlet;
	private long lastUpdate;

	@Override
	public ServletMatch getServletMatch(String contextPath, String path) throws ServletException {
		if (path.startsWith(SERVLET_PATH)) { // $NON-NLS-1$
			int len = SERVLET_PATH.length(); // $NON-NLS-1$
			String servletPath = path.substring(0, len);
			String pathInfo = path.substring(len);
			return new ServletMatch(getExecutorServlet(), servletPath, pathInfo);
		}
		return null;
	}

	@Override
	public void init(ComponentModule module) {
		this.module = module;
		this.lastUpdate = module.getLastRefresh();
	}

	public synchronized Servlet getExecutorServlet() throws ServletException {
		if (servlet == null || lastUpdate < this.module.getLastRefresh()) {
			Map<String, String> params = new HashMap<>();
			params.put("jakarta.ws.rs.Application", NSFJAXRSApplication.class.getName()); //$NON-NLS-1$
			// TODO move this to the fragment somehow
			params.put("resteasy.injector.factory", "org.openntf.xsp.jaxrs.weld.NSFCdiInjectorFactory"); //$NON-NLS-1$ //$NON-NLS-2$
			params.put(ResteasyContextParameters.RESTEASY_SERVLET_MAPPING_PREFIX, SERVLET_PATH);
			
			servlet = module.createServlet(ServletUtil.newToOld((jakarta.servlet.Servlet)new FacesJAXRSServletContainer()), "XSP JAX-RS Servlet", params); //$NON-NLS-1$
			lastUpdate = this.module.getLastRefresh();
		}
		return servlet;
	}
}
