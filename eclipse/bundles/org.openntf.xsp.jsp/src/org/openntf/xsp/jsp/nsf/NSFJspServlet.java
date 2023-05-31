/**
 * Copyright (c) 2018-2023 Contributors to the XPages Jakarta EE Support Project
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

import org.glassfish.wasp.Constants;
import org.glassfish.wasp.servlet.JspServlet;
import org.glassfish.wasp.xmlparser.ParserUtils;
import org.openntf.xsp.jakartaee.AbstractXspLifecycleServlet;
import org.openntf.xsp.jakartaee.servlet.ServletUtil;
import org.openntf.xsp.jsp.EarlyInitFactory;
import org.openntf.xsp.jsp.el.NSFELResolver;
import org.openntf.xsp.jsp.util.DominoJspUtil;

import com.ibm.designer.runtime.domino.adapter.ComponentModule;
import com.ibm.xsp.application.ApplicationEx;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequestEvent;
import jakarta.servlet.ServletRequestListener;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 
 * @author Jesse Gallagher
 * @since 2.1.0
 */
public class NSFJspServlet extends AbstractXspLifecycleServlet {
	private static final long serialVersionUID = 1L;
	
	private final JspServlet delegate;
	
	public NSFJspServlet(ComponentModule module) {
		super(module);
		this.delegate = new JspServlet();
	}
	
	@Override
	protected void doInit(ServletConfig config) throws ServletException {
		ClassLoader current = Thread.currentThread().getContextClassLoader();
		try {
			Thread.currentThread().setContextClassLoader(new URLClassLoader(new URL[0], current));
			delegate.init(config);
		} finally {
			Thread.currentThread().setContextClassLoader(current);
		}
	}
	
	@Override
	protected void doService(HttpServletRequest request, HttpServletResponse response, ApplicationEx application)
			throws ServletException, IOException {
		try {
			AccessController.doPrivileged((PrivilegedExceptionAction<Void>)() -> {
				
				ServletContext context = request.getServletContext();
				context.setAttribute("org.glassfish.jsp.beanManagerELResolver", NSFELResolver.instance); //$NON-NLS-1$
				context.setAttribute(Constants.JSP_TLD_URI_TO_LOCATION_MAP, DominoJspUtil.buildJstlDtdMap());
				
				ClassLoader current = Thread.currentThread().getContextClassLoader();
				Thread.currentThread().setContextClassLoader(DominoJspUtil.buildJspClassLoader(current));
				ServletUtil.getListeners(context, ServletRequestListener.class)
					.forEach(l -> l.requestInitialized(new ServletRequestEvent(getServletContext(), request)));
				try {
					ParserUtils.setDtdResourcePrefix(EarlyInitFactory.getServletDtdPath().toUri().toString());
					delegate.service(request, response);
				} finally {
					ServletUtil.getListeners(context, ServletRequestListener.class)
						.forEach(l -> l.requestDestroyed(new ServletRequestEvent(getServletContext(), request)));
					Thread.currentThread().setContextClassLoader(current);
					context.removeAttribute("org.glassfish.jsp.beanManagerELResolver"); //$NON-NLS-1$
					context.removeAttribute(Constants.JSP_TLD_URI_TO_LOCATION_MAP);
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
			ServletUtil.close(response);
		}
	}
	
	@Override
	public void destroy() {
		super.destroy();
		delegate.destroy();
	}
}
