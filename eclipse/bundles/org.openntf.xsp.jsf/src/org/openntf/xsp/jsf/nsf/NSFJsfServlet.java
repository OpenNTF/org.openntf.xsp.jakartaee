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

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.annotation.Annotation;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.openntf.xsp.cdi.context.AbstractProxyingContext;
import org.openntf.xsp.cdi.util.ContainerUtil;
import org.openntf.xsp.jakartaee.servlet.ServletUtil;
import org.openntf.xsp.jakartaee.util.ModuleUtil;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.osgi.framework.FrameworkUtil;

import com.ibm.designer.runtime.domino.adapter.ComponentModule;
import com.ibm.domino.xsp.module.nsf.NSFComponentModule;

import jakarta.faces.FactoryFinder;
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

/**
 * 
 * @author Jesse Gallagher
 * @since 2.3.0
 */
public class NSFJsfServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private final ComponentModule module;
	private FacesServlet delegate;
	private boolean initialized;
	
	private ServletContextListener configureListener;
	private ServletRequestListener configureRequestListener;
	
	private final Map<String, Object> jsfStashedAttributes = new ConcurrentHashMap<>();
	
	public NSFJsfServlet(ComponentModule module) {
		super();
		this.module = module;
	}
	
	public void doInit(HttpServletRequest req, ServletConfig config) throws ServletException {
		ClassLoader current = Thread.currentThread().getContextClassLoader();
		try {
			// Set the extension provider
			System.setProperty("com.sun.faces.InjectionProvider", "com.sun.faces.vendor.WebContainerInjectionProvider");
			
			Thread.currentThread().setContextClassLoader(buildJsfClassLoader(current));
			
			// Do this reflectively due to lack of bundle export
			Bundle b = FrameworkUtil.getBundle(FacesServlet.class);
			{
				@SuppressWarnings("unchecked")
				Class<? extends ServletContainerInitializer> c = (Class<? extends ServletContainerInitializer>) b.loadClass("com.sun.faces.config.FacesInitializer"); //$NON-NLS-1$
				ServletContainerInitializer initializer = c.newInstance();
				// TODO figure out why this throws a reflection checking whether @HandlesTypes is set
	//			Set<Class<?>> classes = null;
	//			if(initializer.getClass().isAnnotationPresent(HandlesTypes.class)) {
	//				classes = buildMatchingClasses(initializer.getClass().getAnnotation(HandlesTypes.class));
	//			}
				initializer.onStartup(null, getServletContext());
			}
			
			{
				@SuppressWarnings("unchecked")
				Class<? extends ServletContextListener> c = (Class<? extends ServletContextListener>)b.loadClass("com.sun.faces.config.ConfigureListener"); //$NON-NLS-1$
				this.configureListener = c.newInstance();
				
				// Re-wrap the ServletContext to provide the context path
				javax.servlet.ServletContext oldCtx = ServletUtil.newToOld(getServletContext());
				ServletContext ctx = ServletUtil.oldToNew(req.getContextPath(), oldCtx, 5, 0);
				this.configureListener.contextInitialized(new ServletContextEvent(ctx));
				this.configureRequestListener = (ServletRequestListener)configureListener;
				
				// TODO register as context attribute listener and session attribute listener
				// TODO register as request attribute listener
			}

			this.delegate = new FacesServlet();
			delegate.init(config);
		} catch (BundleException | IOException | InstantiationException | IllegalAccessException | ClassNotFoundException e) {
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
					FactoryFinder.getFactory(FactoryFinder.RENDER_KIT_FACTORY);
					if(this.configureRequestListener != null) {
						this.configureRequestListener.requestInitialized(new ServletRequestEvent(getServletContext(), req));
					}
					delegate.service(req, resp);
				} finally {
					if(this.configureRequestListener != null) {
						this.configureRequestListener.requestDestroyed(new ServletRequestEvent(getServletContext(), req));
					}
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
		
		if(configureListener != null) {
			configureListener.contextDestroyed(new ServletContextEvent(getServletContext()));
		}
		
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
	
	private Set<Class<?>> buildMatchingClasses(HandlesTypes types) {
		if(module instanceof NSFComponentModule) {
			// TODO consider whether we can handle other ComponentModules, were someone to make one
			
			@SuppressWarnings("unchecked")
			Set<Class<?>> result = ModuleUtil.getClassNames((NSFComponentModule)module)
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
				.collect(Collectors.toSet());
			
			if(!result.isEmpty()) {
				return result;
			} else {
				return null;
			}
		}
		return null;
	}
}
