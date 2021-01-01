/**
 * Copyright © 2018-2020 Jesse Gallagher
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
package org.openntf.xsp.jsp.nsf;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.Servlet;
import javax.servlet.ServletException;

import org.openntf.xsp.jakartaee.LibraryUtil;
import org.openntf.xsp.jsp.JspLibrary;

import com.ibm.commons.util.StringUtil;
import com.ibm.designer.runtime.domino.adapter.ComponentModule;
import com.ibm.designer.runtime.domino.adapter.IServletFactory;
import com.ibm.designer.runtime.domino.adapter.ServletMatch;

/**
 * 
 * @author Jesse Gallagher
 * @since 1.2.0
 */
public class JspServletFactory implements IServletFactory {

	private ComponentModule module;
	private Servlet servlet;
	private long lastUpdate;

	public JspServletFactory() {
	}

	@Override
	public void init(ComponentModule module) {
		this.module = module;
		this.lastUpdate = module.getLastRefresh();
	}

	@Override
	public ServletMatch getServletMatch(String contextPath, String path) throws ServletException {
		try {
			if(LibraryUtil.usesLibrary(JspLibrary.LIBRARY_ID, module)) {
				int jspIndex = StringUtil.toString(path).indexOf(".jsp"); //$NON-NLS-1$
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
			Map<String, String> params = new HashMap<>();
			
			try {
				this.servlet = AccessController.doPrivileged((PrivilegedExceptionAction<Servlet>)() -> {
					ClassLoader current = Thread.currentThread().getContextClassLoader();
					try {
						Thread.currentThread().setContextClassLoader(new URLClassLoader(new URL[0], current));
						return module.createServlet(new NSFJspServlet(module), "XSP JSP Servlet", params); //$NON-NLS-1$
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

}
