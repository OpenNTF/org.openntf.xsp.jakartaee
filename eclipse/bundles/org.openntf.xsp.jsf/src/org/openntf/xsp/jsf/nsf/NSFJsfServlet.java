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
package org.openntf.xsp.jsf.nsf;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Collections;
import java.util.EventListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.openntf.xsp.cdi.context.AbstractProxyingContext;
import org.openntf.xsp.cdi.util.ContainerUtil;
import org.openntf.xsp.cdi.util.DiscoveryUtil;
import org.openntf.xsp.jakartaee.servlet.ServletUtil;
import org.openntf.xsp.jakartaee.util.ModuleUtil;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.osgi.framework.FrameworkUtil;

import com.ibm.commons.util.StringUtil;
import com.ibm.designer.domino.napi.NotesAPIException;
import com.ibm.designer.runtime.domino.adapter.ComponentModule;
import com.ibm.domino.xsp.module.nsf.NSFComponentModule;
import com.ibm.domino.xsp.module.nsf.NotesContext;

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
 * @since 2.3.0
 */
public class NSFJsfServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private static final String PROP_SESSIONINIT = NSFJsfServlet.class.getName() + "_sessionInit"; //$NON-NLS-1$
	
	private final ComponentModule module;
	private FacesServlet delegate;
	private boolean initialized;
	
	private final Map<String, Object> jsfStashedAttributes = new ConcurrentHashMap<>();
	
	public NSFJsfServlet(ComponentModule module) {
		super();
		this.module = module;
	}
	
	public void doInit(HttpServletRequest req, ServletConfig config) throws ServletException {
		ClassLoader current = Thread.currentThread().getContextClassLoader();
		try {
			ContainerUtil.getContainer(NotesContext.getCurrent().getNotesDatabase());
			Thread.currentThread().setContextClassLoader(buildJsfClassLoader(current));
			
			// Do this reflectively due to lack of bundle export
			Bundle b = FrameworkUtil.getBundle(FacesServlet.class);
			{
				@SuppressWarnings("unchecked")
				Class<? extends ServletContainerInitializer> c = (Class<? extends ServletContainerInitializer>) b.loadClass("com.sun.faces.config.FacesInitializer"); //$NON-NLS-1$
				ServletContainerInitializer initializer = c.newInstance();
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
				
				// Add the ConfigureListener
				// TODO remove when moving to > 3.0.2, as this will be set in the initializer
				@SuppressWarnings("unchecked")
				Class<? extends EventListener> c = (Class<? extends EventListener>)b.loadClass("com.sun.faces.config.ConfigureListener"); //$NON-NLS-1$
				ctx.addListener(c);
				
				ServletUtil.getListeners(ctx, ServletContextListener.class)
					.forEach(l -> l.contextInitialized(new ServletContextEvent(ctx)));
			}

			this.delegate = new FacesServlet();
			delegate.init(config);
		} catch (BundleException | IOException | InstantiationException | IllegalAccessException | ClassNotFoundException | NotesAPIException e) {
			throw new ServletException(e);
		} finally {
			Thread.currentThread().setContextClassLoader(current);
		}
	}

	@Override
	// TODO see if synchronization can be handled better
	public synchronized void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		ServletContext ctx = req.getServletContext();
		
		// Stash all com.sun.faces attributes from XPages
		Map<String, Object> incomingStashed = stashFacesAttributes(ctx);
		
		try {
			AccessController.doPrivileged((PrivilegedExceptionAction<Void>)() -> {
				if(!initialized) {
					this.doInit(req, getServletConfig());
					initialized = true;
				}
				
				ContainerUtil.setThreadContextDatabasePath(req.getContextPath().substring(1));
				AbstractProxyingContext.setThreadContextRequest(req);
				ClassLoader current = Thread.currentThread().getContextClassLoader();
				Thread.currentThread().setContextClassLoader(buildJsfClassLoader(current));
				try {
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
			
			// Restore XPages-land com.sun.faces attributes
			restoreFacesAttributes(ctx, incomingStashed);
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
	
	private Map<String, Object> stashFacesAttributes(ServletContext ctx) {
		Map<String, Object> incomingStashed = new HashMap<>();
		Collections.list(ctx.getAttributeNames())
			.stream()
			.filter(Objects::nonNull)
			.filter(attr -> attr.startsWith("com.sun.faces.")) //$NON-NLS-1$
			.forEach(attr -> {
				incomingStashed.put(attr, ctx.getAttribute(attr));
				ctx.removeAttribute(attr);
			});
		// Restore any JSF attributes stashed from last time
		jsfStashedAttributes.forEach(ctx::setAttribute);
		
		return incomingStashed;
	}
	
	private void restoreFacesAttributes(ServletContext ctx, Map<String, Object> incomingStashed) {
		// Stash all com.sun.faces attributes set by JSF
		Collections.list(ctx.getAttributeNames())
			.stream()
			.filter(Objects::nonNull)
			.filter(attr -> attr.startsWith("com.sun.faces.")) //$NON-NLS-1$
			.forEach(attr -> {
				jsfStashedAttributes.put(attr, ctx.getAttribute(attr));
				ctx.removeAttribute(attr);
			});
		// Restore any XPages attributes stashed from last time
		incomingStashed.forEach(ctx::setAttribute);
	}

	private ClassLoader buildJsfClassLoader(ClassLoader delegate) throws BundleException, IOException {
		return new ClassLoader(delegate) {
			@Override
			public Class<?> loadClass(String name) throws ClassNotFoundException {
				if(name != null && name.startsWith("com.sun.faces")) { //$NON-NLS-1$
					return NSFJsfServlet.class.getClassLoader().loadClass(name);
				}
				return super.loadClass(name);
			}
		};
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
		
		// Find in the JSF bundle as well - this works
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
