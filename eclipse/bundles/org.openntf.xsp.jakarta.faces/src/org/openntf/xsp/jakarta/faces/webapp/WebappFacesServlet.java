/**
 * Copyright (c) 2018-2024 Contributors to the XPages Jakarta EE Support Project
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
package org.openntf.xsp.jakarta.faces.webapp;

import java.io.Closeable;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ibm.designer.runtime.domino.adapter.util.XSPErrorPage;

import org.apache.myfaces.webapp.MyFacesContainerInitializer;
import org.eclipse.core.runtime.FileLocator;
import org.openntf.xsp.jakarta.cdi.bean.HttpContextBean;
import org.openntf.xsp.jakarta.cdi.context.AbstractProxyingContext;
import org.openntf.xsp.jakarta.cdi.util.ContainerUtil;
import org.openntf.xsp.jakarta.faces.util.FacesBlockingClassLoader;
import org.openntf.xsp.jakartaee.servlet.ServletUtil;
import org.openntf.xsp.jakartaee.util.ModuleUtil;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.osgi.framework.FrameworkUtil;

import jakarta.enterprise.inject.spi.CDI;
import jakarta.faces.FactoryFinder;
import jakarta.faces.webapp.FacesServlet;
import jakarta.servlet.ServletContainerInitializer;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequestEvent;
import jakarta.servlet.ServletRequestListener;
import jakarta.servlet.annotation.HandlesTypes;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;

/**
 *
 * @author Jesse Gallagher
 * @since 3.2.0
 */
public class WebappFacesServlet extends javax.servlet.http.HttpServlet {
	private static final long serialVersionUID = 1L;

	private static final Logger log = Logger.getLogger(WebappFacesServlet.class.getName());

	private static final String PROP_SESSIONINIT = WebappFacesServlet.class.getName() + "_sessionInit"; //$NON-NLS-1$
	private static final String PROP_CLASSLOADER = WebappFacesServlet.class.getName() + "_classLoader"; //$NON-NLS-1$

	private final FacesServlet delegate;
	private final Collection<Path> tempFiles = Collections.synchronizedList(new ArrayList<>());
	private ServletContext context;
	private boolean initialized;

	public WebappFacesServlet() {
		super();
		this.delegate = new FacesServlet();
	}

	public void doInit(final javax.servlet.ServletConfig config) throws javax.servlet.ServletException {
		try {
			CDI<Object> cdi = CDI.current();
	
			this.context = ServletUtil.oldToNew(config.getServletContext().getContextPath(), config.getServletContext(), 6, 0);
			context.setAttribute("jakarta.enterprise.inject.spi.BeanManager", ContainerUtil.getBeanManager(cdi)); //$NON-NLS-1$
			
			// Add a stub mapping to make sure MyFacesContainerInitializer#onStartup doesn't try to re-register
			ServletUtil.addServletMappingStub(config.getServletContext(), FacesServlet.class.getName());
			
			Bundle b = FrameworkUtil.getBundle(FacesServlet.class);
			Bundle b2 = FrameworkUtil.getBundle(MyFacesContainerInitializer.class);
			ServletContainerInitializer initializer = new MyFacesContainerInitializer();
			Set<Class<?>> classes = null;
			HandlesTypes types = initializer.getClass().getAnnotation(HandlesTypes.class);
			if (types != null) {
				classes = ModuleUtil.buildMatchingClasses(types, null, b, b2);
			}
			initializer.onStartup(classes, this.context);

			ServletUtil.contextInitialized(this.context);
			
			delegate.init(ServletUtil.oldToNew(config));
		} catch(ServletException e) {
			throw ServletUtil.newToOld(e);
		}
	}

	@Override
	// TODO see if synchronization can be handled better
	public synchronized void service(final javax.servlet.http.HttpServletRequest oldRequest, final javax.servlet.http.HttpServletResponse oldResponse)
			throws javax.servlet.ServletException, IOException {

		HttpServletRequest request = ServletUtil.oldToNew(ServletUtil.newToOld(this.context), (javax.servlet.http.HttpServletRequest)oldRequest);
		HttpServletResponse response = ServletUtil.oldToNew((javax.servlet.http.HttpServletResponse)oldResponse);
		HttpSession session = request.getSession(true);

		HttpContextBean.setThreadResponse(response);
		try {
			ClassLoader current = Thread.currentThread().getContextClassLoader();
			Thread.currentThread().setContextClassLoader(buildJsfClassLoader(getServletContext(), current));
			try {
				if(!initialized) {
					this.doInit(this.getServletConfig());
					this.initialized = true;
				}
				
				AbstractProxyingContext.setThreadContextRequest(request);
				ServletUtil.getListeners(this.context, ServletRequestListener.class)
						.forEach(l -> l.requestInitialized(new ServletRequestEvent(this.context, request)));

				// Fire the session listener if needed
				if (!"1".equals(session.getAttribute(PROP_SESSIONINIT))) { //$NON-NLS-1$
					ServletUtil.getListeners(this.context, HttpSessionListener.class)
							.forEach(l -> l.sessionCreated(new HttpSessionEvent(session)));
					session.setAttribute(PROP_SESSIONINIT, "1"); //$NON-NLS-1$
					// TODO add a hook for session expiration?
				}


				delegate.service(request, response);
			} finally {

				ServletUtil.getListeners(this.context, ServletRequestListener.class)
						.forEach(l -> l.requestDestroyed(new ServletRequestEvent(this.context, request)));
				Thread.currentThread().setContextClassLoader(current);
				AbstractProxyingContext.setThreadContextRequest(null);
			}
		} catch (Throwable t) {
			if(log.isLoggable(Level.SEVERE)) {
				log.log(Level.SEVERE, "Encountered unhandled exception in Servlet", t);
			}

			try (PrintWriter w = response.getWriter()) {
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				XSPErrorPage.handleException(w, t, null, false);
			} catch (javax.servlet.ServletException e) {
				throw new IOException(e);
			} catch (IllegalStateException e) {
				// Happens when the writer or output has already been opened
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			}
		} finally {
			// In case it's not flushed on its own
			ServletUtil.close(response);
			HttpContextBean.setThreadResponse(null);
		}
	}

	@Override
	public void destroy() {
		ServletContext ctx = this.context;
		ServletUtil.getListeners(ctx, ServletContextListener.class)
				.forEach(l -> l.contextDestroyed(new ServletContextEvent(ctx)));

		synchronized(tempFiles) {
			tempFiles.forEach(path -> {
				try {
					Files.deleteIfExists(path);
				} catch (IOException e) {
					// Ignore
				}
			});
			tempFiles.clear();
		}

		ClassLoader cl = (ClassLoader)ctx.getAttribute(PROP_CLASSLOADER);
		if(cl != null && cl instanceof Closeable) {
			try {
				((Closeable)cl).close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		ctx.removeAttribute(PROP_CLASSLOADER);

		FacesServlet delegate = this.delegate;
		if(delegate != null) {
			delegate.destroy();
		}

		super.destroy();
	}

	// *******************************************************************************
	// * Internal utility methods
	// *******************************************************************************

	@SuppressWarnings("deprecation")
	private synchronized ClassLoader buildJsfClassLoader(final javax.servlet.ServletContext context, final ClassLoader delegate)
			throws BundleException, IOException {
		if (context.getAttribute(PROP_CLASSLOADER) == null) {

			List<URL> urls = new ArrayList<>();
			urls.add(FileLocator.getBundleFile(FrameworkUtil.getBundle(FactoryFinder.class)).toURI().toURL());
			urls.add(FileLocator.getBundleFile(FrameworkUtil.getBundle(MyFacesContainerInitializer.class)).toURI().toURL());

			FacesBlockingClassLoader cl = new FacesBlockingClassLoader(urls.toArray(new URL[urls.size()]), delegate);

			context.setAttribute(PROP_CLASSLOADER, cl);
		}
		return (ClassLoader) context.getAttribute(PROP_CLASSLOADER);
	}
}
