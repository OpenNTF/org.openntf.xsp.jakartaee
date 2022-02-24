/**
 * Copyright © 2018-2022 Jesse Gallagher
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
package org.openntf.xsp.jsf.nsf;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedExceptionAction;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.myfaces.webapp.MyFacesContainerInitializer;
import org.apache.myfaces.webapp.StartupServletContextListener;
import org.openntf.xsp.cdi.context.AbstractProxyingContext;
import org.openntf.xsp.cdi.util.ContainerUtil;
import org.openntf.xsp.cdi.util.DiscoveryUtil;
import org.openntf.xsp.jakartaee.DelegatingClassLoader;
import org.openntf.xsp.jakartaee.servlet.ServletUtil;
import org.openntf.xsp.jakartaee.util.LibraryUtil;
import org.openntf.xsp.jakartaee.util.ModuleUtil;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.osgi.framework.FrameworkUtil;

import com.ibm.commons.util.StringUtil;
import com.ibm.designer.domino.napi.NotesAPIException;
import com.ibm.designer.runtime.domino.adapter.ComponentModule;
import com.ibm.designer.runtime.domino.adapter.util.XSPErrorPage;
import com.ibm.domino.xsp.module.nsf.NSFComponentModule;
import com.ibm.domino.xsp.module.nsf.NotesContext;

import jakarta.enterprise.inject.spi.CDI;
import jakarta.faces.application.ProjectStage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.webapp.FacesServlet;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContainerInitializer;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequestEvent;
import jakarta.servlet.ServletRequestListener;
import jakarta.servlet.annotation.HandlesTypes;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;

/**
 * 
 * @author Jesse Gallagher
 * @since 2.4.0
 */
public class NSFJsfServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private static final String PROP_SESSIONINIT = NSFJsfServlet.class.getName() + "_sessionInit"; //$NON-NLS-1$
	private static final String PROP_CLASSLOADER = NSFJsfServlet.class.getName() + "_classLoader"; //$NON-NLS-1$
	
	private final ComponentModule module;
	private FacesServlet delegate;
	private boolean initialized;
	
	public NSFJsfServlet(ComponentModule module) {
		super();
		this.module = module;
	}
	
	public void doInit(HttpServletRequest req, ServletConfig config) throws ServletException {
		try {
			CDI<Object> cdi = ContainerUtil.getContainer(NotesContext.getCurrent().getNotesDatabase());
			ServletContext context = config.getServletContext();
			context.setAttribute("jakarta.enterprise.inject.spi.BeanManager", ContainerUtil.getBeanManager(cdi)); //$NON-NLS-1$
			// TODO investigate why partial state saving doesn't work with a basic form
			context.setInitParameter("jakarta.faces.PARTIAL_STATE_SAVING", "false"); //$NON-NLS-1$ //$NON-NLS-2$
			
			Properties props = LibraryUtil.getXspProperties(module);
			String projectStage = props.getProperty(ProjectStage.PROJECT_STAGE_PARAM_NAME, ""); //$NON-NLS-1$
			context.setInitParameter(ProjectStage.PROJECT_STAGE_PARAM_NAME, projectStage);
			
			Bundle b = FrameworkUtil.getBundle(FacesServlet.class);
			{
				ServletContainerInitializer initializer = new MyFacesContainerInitializer();
				Set<Class<?>> classes = null;
				HandlesTypes types = initializer.getClass().getAnnotation(HandlesTypes.class);
				if(types != null) {
					classes = buildMatchingClasses(types, b);
				}
				initializer.onStartup(classes, getServletContext());
			}
			
			{
				// Re-wrap the ServletContext to provide the context path
				javax.servlet.ServletContext oldCtx = ServletUtil.newToOld(getServletContext());
				ServletContext ctx = ServletUtil.oldToNew(req.getContextPath(), oldCtx, 5, 0);
				ctx.addListener(StartupServletContextListener.class);
				
				ServletUtil.getListeners(ctx, ServletContextListener.class)
					.forEach(l -> l.contextInitialized(new ServletContextEvent(ctx)));
			}

			this.delegate = new FacesServlet();
			delegate.init(config);
		} catch (NotesAPIException e) {
			throw new ServletException(e);
		}
	}

	@Override
	// TODO see if synchronization can be handled better
	public synchronized void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		ServletContext ctx = req.getServletContext();
		
		try {
			AccessController.doPrivileged((PrivilegedExceptionAction<Void>)() -> {
				ClassLoader current = Thread.currentThread().getContextClassLoader();
				Thread.currentThread().setContextClassLoader(buildJsfClassLoader(ctx, current));
				try {
					if(!initialized) {
						this.doInit(req, getServletConfig());
						initialized = true;
					}
					
					ContainerUtil.setThreadContextDatabasePath(req.getContextPath().substring(1));
					AbstractProxyingContext.setThreadContextRequest(req);
					ServletUtil.getListeners(ctx, ServletRequestListener.class)
						.forEach(l -> l.requestInitialized(new ServletRequestEvent(getServletContext(), req)));

					
					// Fire the session listener if needed
					HttpSession session = req.getSession(true);
					if(!"1".equals(session.getAttribute(PROP_SESSIONINIT))) { //$NON-NLS-1$
						ServletUtil.getListeners(ctx, HttpSessionListener.class)
							.forEach(l -> l.sessionCreated(new HttpSessionEvent(session)));
						session.setAttribute(PROP_SESSIONINIT, "1"); //$NON-NLS-1$
						// TODO add a hook for session expiration?
					}

					delegate.service(req, resp);
				} finally {
					ServletUtil.getListeners(ctx, ServletRequestListener.class)
						.forEach(l -> l.requestDestroyed(new ServletRequestEvent(getServletContext(), req)));
					Thread.currentThread().setContextClassLoader(current);
					ContainerUtil.setThreadContextDatabasePath(null);
					AbstractProxyingContext.setThreadContextRequest(null);
				}
				return null;
			});
		} catch(Throwable t) {
			try(PrintWriter w = resp.getWriter()) {
				resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				XSPErrorPage.handleException(w, t, req.getRequestURL().toString(), false);
			} catch (javax.servlet.ServletException e) {
				throw new IOException(e);
			} catch(IllegalStateException e) {
				// Happens when the writer or output has already been opened
				resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			}
		} finally {
			// In case it's not flushed on its own
			// NB: resp.flushBuffer() is insufficient here
			try {
				resp.getWriter().flush();
			} catch(IllegalStateException e) {
				// Written using the stream instead
				try {
					resp.getOutputStream().flush();
				} catch(IllegalStateException e2) {
					// Well, fine.
				}
			} catch(IOException e) {
				// No need to propagate this
			}
		}
	}
	
	@Override
	public void destroy() {
		ServletContext ctx = getServletContext();
		ServletUtil.getListeners(ctx, ServletContextListener.class)
			.forEach(l -> l.contextDestroyed(new ServletContextEvent(ctx)));
		
		super.destroy();
	}
	
	// *******************************************************************************
	// * Internal utility methods
	// *******************************************************************************

	private synchronized ClassLoader buildJsfClassLoader(ServletContext context, ClassLoader delegate) throws BundleException, IOException {
		if(context.getAttribute(PROP_CLASSLOADER) == null) {
			ClassLoader apiCl = FacesContext.class.getClassLoader();
			ClassLoader implCl = MyFacesContainerInitializer.class.getClassLoader();
			ClassLoader cl = new DelegatingClassLoader(apiCl, implCl, delegate) {
				@Override
				public Class<?> loadClass(String name) throws ClassNotFoundException {
					if(name != null && name.startsWith("com.sun.faces.")) { //$NON-NLS-1$
						throw new ClassNotFoundException();
					}
					return super.loadClass(name);
				}
			};
			context.setAttribute(PROP_CLASSLOADER, cl);
		}
		return (ClassLoader)context.getAttribute(PROP_CLASSLOADER);
	}

	@SuppressWarnings("unchecked")
	private Set<Class<?>> buildMatchingClasses(HandlesTypes types, Bundle bundle) {
		Set<Class<?>> result = new HashSet<>();
		if(module instanceof NSFComponentModule) {
			// TODO consider whether we can handle other ComponentModules, were someone to make one
			
			ModuleUtil.getClassNames((NSFComponentModule)module)
				.filter(className -> !ModuleUtil.GENERATED_CLASSNAMES.matcher(className).matches())
				.map(className -> {
					try {
						return module.getModuleClassLoader().loadClass(className);
					} catch (ClassNotFoundException e) {
						throw new RuntimeException(e);
					}
				})
				.filter(c -> {
					for(Class<?> type : types.value()) {
						if(type.isAnnotation()) {
							return c.isAnnotationPresent((Class<? extends Annotation>)type);
						} else {
							return type.isAssignableFrom(c);
						}
					}
					return true;
				})
				.forEach(result::add);
		}
		
		// Find in the JSF bundle as well
		String baseUrl = bundle.getEntry("/").toString(); //$NON-NLS-1$
		List<URL> entries = Collections.list(bundle.findEntries("/", "*.class", true)); //$NON-NLS-1$ //$NON-NLS-2$
		entries.stream()
			.parallel()
			.map(String::valueOf)
			.map(url -> url.substring(baseUrl.length()))
			.map(DiscoveryUtil::toClassName)
			.filter(StringUtil::isNotEmpty)
			.sequential()
			.map(className -> {
				try {
					return bundle.loadClass(className);
				} catch (ClassNotFoundException e) {
					throw new RuntimeException(e);
				}
			})
			.filter(c -> {
				for(Class<?> type : types.value()) {
					if(type.isAnnotation()) {
						return c.isAnnotationPresent((Class<? extends Annotation>)type);
					} else {
						return type.isAssignableFrom(c);
					}
				}
				return true;
			})
			.forEach(result::add);
		

		if(!result.isEmpty()) {
			return result;
		} else {
			return null;
		}
	}
}
