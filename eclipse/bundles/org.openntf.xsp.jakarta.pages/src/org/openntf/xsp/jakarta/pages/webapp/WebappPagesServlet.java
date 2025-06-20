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
package org.openntf.xsp.jakarta.pages.webapp;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.stream.Collectors;

import com.ibm.xsp.extlib.util.ExtLibUtil;

import org.glassfish.wasp.Constants;
import org.glassfish.wasp.servlet.JspServlet;
import org.glassfish.wasp.xmlparser.ParserUtils;
import org.openntf.xsp.jakarta.pages.PagesHttpInitListener;
import org.openntf.xsp.jakarta.pages.util.DominoPagesUtil;
import org.openntf.xsp.jakartaee.servlet.ServletUtil;
import org.openntf.xsp.jakartaee.util.LibraryUtil;
import org.osgi.framework.BundleException;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequestEvent;
import jakarta.servlet.ServletRequestListener;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Legacy {@link HttpServlet} implementation that can be mapped to {@code *.jsp}
 * to provide JSP processing in a webapp.
 *
 * @author Jesse Gallagher
 * @since 2.8.0
 */
public class WebappPagesServlet extends javax.servlet.http.HttpServlet {

	private static final long serialVersionUID = 1L;

	private final JspServlet delegate;
	private ServletContext context;

	public WebappPagesServlet() {
		this.delegate = new JspServlet();
	}

	@Override
	public void init(final javax.servlet.ServletConfig config) throws javax.servlet.ServletException {
		super.init(config);

		this.context = ServletUtil.oldToNew(config.getServletContext().getContextPath(), config.getServletContext());

		try {
			String classpath = DominoPagesUtil.buildBundleClassPath()
				.stream()
				.map(File::toString)
				.collect(Collectors.joining(DominoPagesUtil.PATH_SEP));
			this.context.setInitParameter("classpath", classpath); //$NON-NLS-1$
			this.context.setInitParameter("development", Boolean.toString(ExtLibUtil.isDevelopmentMode())); //$NON-NLS-1$

			Path tempDir = LibraryUtil.getTempDirectory();
			tempDir = tempDir.resolve(getClass().getName());
			String moduleName = Integer.toString(System.identityHashCode(config.getServletContext()));
			tempDir = tempDir.resolve(moduleName);
			Files.createDirectories(tempDir);
			this.context.setInitParameter("scratchdir", tempDir.toString()); //$NON-NLS-1$
		} catch(IOException | BundleException e) {
			throw new javax.servlet.ServletException(e);
		}

		ClassLoader current = Thread.currentThread().getContextClassLoader();
		try {
			Thread.currentThread().setContextClassLoader(new URLClassLoader(new URL[0], current));
			try {
				delegate.init(ServletUtil.oldToNew(config));
			} catch (ServletException e) {
				throw ServletUtil.newToOld(e);
			}
		} finally {
			Thread.currentThread().setContextClassLoader(current);
		}
	}

	@Override
	public void service(final javax.servlet.ServletRequest oldRequest, final javax.servlet.ServletResponse oldResponse)
			throws javax.servlet.ServletException, IOException {
		try {
			HttpServletRequest request = ServletUtil.oldToNew(ServletUtil.newToOld(this.context), (javax.servlet.http.HttpServletRequest)oldRequest);
			HttpServletResponse response = ServletUtil.oldToNew((javax.servlet.http.HttpServletResponse)oldResponse);

			AccessController.doPrivileged((PrivilegedExceptionAction<Void>)() -> {

				//context.setAttribute("org.glassfish.jsp.beanManagerELResolver", NSFELResolver.instance); //$NON-NLS-1$
				context.setAttribute(Constants.JSP_TLD_URI_TO_LOCATION_MAP, DominoPagesUtil.buildJstlDtdMap());

				ClassLoader current = Thread.currentThread().getContextClassLoader();
				Thread.currentThread().setContextClassLoader(DominoPagesUtil.buildPagesClassLoader(current));
				ServletUtil.getListeners(context, ServletRequestListener.class)
					.forEach(l -> l.requestInitialized(new ServletRequestEvent(context, request)));
				try {
					ParserUtils.setDtdResourcePrefix(PagesHttpInitListener.getServletDtdPath().toUri().toString());
					delegate.service(request, response);
				} finally {
					ServletUtil.getListeners(context, ServletRequestListener.class)
						.forEach(l -> l.requestDestroyed(new ServletRequestEvent(context, request)));
					Thread.currentThread().setContextClassLoader(current);
					context.removeAttribute("org.glassfish.jsp.beanManagerELResolver"); //$NON-NLS-1$
					context.removeAttribute(Constants.JSP_TLD_URI_TO_LOCATION_MAP);

					ServletUtil.close(response);
				}
				return null;
			});
		} catch(PrivilegedActionException e) {
			e.printStackTrace();
			Throwable cause = e.getCause();
			if(cause instanceof ServletException e2) {
				throw ServletUtil.newToOld(e2);
			} else if(cause instanceof IOException e2) {
				throw e2;
			} else {
				throw new javax.servlet.ServletException(e);
			}
		} catch(Throwable t) {
			t.printStackTrace();
			throw t;
		}
	}

	@Override
	public void destroy() {
		super.destroy();
		delegate.destroy();
	}
}
