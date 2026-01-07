/**
 * Copyright (c) 2018-2026 Contributors to the XPages Jakarta EE Support Project
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
package org.openntf.xsp.jakarta.pages.nsf;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

import com.ibm.designer.runtime.domino.adapter.ComponentModule;
import com.ibm.xsp.application.ApplicationEx;

import org.glassfish.wasp.Constants;
import org.glassfish.wasp.servlet.JspServlet;
import org.glassfish.wasp.xmlparser.ParserUtils;
import org.openntf.xsp.jakarta.cdi.bean.HttpContextBean;
import org.openntf.xsp.jakarta.pages.PagesHttpInitListener;
import org.openntf.xsp.jakarta.pages.el.NSFELResolver;
import org.openntf.xsp.jakarta.pages.util.DominoPagesUtil;
import org.openntf.xsp.jakartaee.AbstractXspLifecycleServlet;
import org.openntf.xsp.jakartaee.servlet.ServletUtil;

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
public class NSFPagesServlet extends AbstractXspLifecycleServlet {
	private static final long serialVersionUID = 1L;

	private final JspServlet delegate;

	public NSFPagesServlet(final ComponentModule module) {
		super(module);
		this.delegate = new JspServlet();
	}

	@Override
	protected void doInit(final ServletConfig config, final HttpServletRequest request) throws ServletException {
		ClassLoader current = Thread.currentThread().getContextClassLoader();
		try {
			Thread.currentThread().setContextClassLoader(new URLClassLoader(new URL[0], current));
			delegate.init(config);
		} finally {
			Thread.currentThread().setContextClassLoader(current);
		}
	}

	@SuppressWarnings({ "removal", "deprecation" })
	@Override
	protected void doService(final HttpServletRequest request, final HttpServletResponse response, final ApplicationEx application)
			throws ServletException, IOException {
		HttpContextBean.setThreadResponse(response);
		try {
			AccessController.doPrivileged((PrivilegedExceptionAction<Void>)() -> {

				ServletContext context = request.getServletContext();
				context.setAttribute("org.glassfish.jsp.beanManagerELResolver", new NSFELResolver(getModule())); //$NON-NLS-1$
				context.setAttribute(Constants.JSP_TLD_URI_TO_LOCATION_MAP, DominoPagesUtil.buildJstlDtdMap());

				ClassLoader current = Thread.currentThread().getContextClassLoader();
				Thread.currentThread().setContextClassLoader(DominoPagesUtil.buildPagesClassLoader(current));
				ServletUtil.getListeners(context, ServletRequestListener.class)
					.forEach(l -> l.requestInitialized(new ServletRequestEvent(getServletContext(), request)));
				try {
					ParserUtils.setDtdResourcePrefix(PagesHttpInitListener.getServletDtdPath().toUri().toString());
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
			if(cause instanceof ServletException e2) {
				throw e2;
			} else if(cause instanceof IOException e2) {
				throw e2;
			} else {
				throw new ServletException(e);
			}
		} catch(Throwable t) {
			throw t;
		} finally {
			// Looks like Wasp doesn't flush this on its own
			ServletUtil.close(response);
			HttpContextBean.setThreadResponse(null);
		}
	}

	@Override
	public void destroy() {
		super.destroy();
		delegate.destroy();
	}
}
