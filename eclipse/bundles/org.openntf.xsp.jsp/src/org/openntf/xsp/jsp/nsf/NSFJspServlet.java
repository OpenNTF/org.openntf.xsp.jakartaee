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
package org.openntf.xsp.jsp.nsf;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.jasper.servlet.JspServlet;
import org.openntf.xsp.cdi.context.AbstractProxyingContext;
import org.openntf.xsp.cdi.util.ContainerUtil;
import org.openntf.xsp.jsp.el.NSFELResolver;

import com.ibm.designer.runtime.domino.adapter.ComponentModule;

/**
 * 
 * @author Jesse Gallagher
 * @since 2.1.0
 */
public class NSFJspServlet extends JspServlet {
	private static final long serialVersionUID = 1L;
	
	@SuppressWarnings("unused")
	private final ComponentModule module;
	
	public NSFJspServlet(ComponentModule module) {
		super();
		this.module = module;
	}

	@Override
	public void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		try {
			AccessController.doPrivileged((PrivilegedExceptionAction<Void>)() -> {
				
				req.getServletContext().setAttribute("org.glassfish.jsp.beanManagerELResolver", NSFELResolver.instance); //$NON-NLS-1$
				ContainerUtil.setThreadContextDatabasePath(req.getContextPath().substring(1));
				AbstractProxyingContext.setThreadContextRequest(req);
				ClassLoader current = Thread.currentThread().getContextClassLoader();
				Thread.currentThread().setContextClassLoader(new URLClassLoader(new URL[0], current));
				try {
					super.service(req, resp);
				} finally {
					Thread.currentThread().setContextClassLoader(current);
					req.getServletContext().setAttribute("org.glassfish.jsp.beanManagerELResolver", null); //$NON-NLS-1$
					ContainerUtil.setThreadContextDatabasePath(null);
					AbstractProxyingContext.setThreadContextRequest(null);
				}
				return null;
			});
		} catch(PrivilegedActionException e) {
			Throwable cause = e.getCause();
			if(cause instanceof ServletException) {
				throw (ServletException)cause;
			} else if(cause instanceof IOException) {
				throw (IOException)cause;
			} else {
				throw new ServletException(e);
			}
		} catch(Throwable t) {
			t.printStackTrace();
			throw t;
		} finally {
			// Looks like Jasper doesn't flush this on its own
			resp.getWriter().flush();
		}
	}

}
