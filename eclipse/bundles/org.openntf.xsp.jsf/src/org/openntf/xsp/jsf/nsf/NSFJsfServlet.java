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
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.List;

import org.openntf.xsp.cdi.context.AbstractProxyingContext;
import org.openntf.xsp.cdi.util.ContainerUtil;
import org.osgi.framework.BundleException;

import com.ibm.designer.runtime.domino.adapter.ComponentModule;

import jakarta.faces.FactoryFinder;
import jakarta.faces.webapp.FacesServlet;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContainerInitializer;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 
 * @author Jesse Gallagher
 * @since 2.1.0
 */
public class NSFJsfServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	@SuppressWarnings("unused")
	private final ComponentModule module;
	private FacesServlet delegate;
	
	public NSFJsfServlet(ComponentModule module) {
		super();
		this.module = module;
	}
	
	@Override
	public void init(ServletConfig config) throws ServletException {
		ClassLoader current = Thread.currentThread().getContextClassLoader();
		try {
			// Set the extension provider
			System.setProperty("com.sun.faces.InjectionProvider", "com.sun.faces.vendor.WebContainerInjectionProvider");
			
			Thread.currentThread().setContextClassLoader(buildJsfClassLoader(current));
			this.delegate = new FacesServlet();
			delegate.init(config);
			
			// Do this reflectively due to lack of bundle export
//			@SuppressWarnings("unchecked")
//			Class<? extends ServletContainerInitializer> c = (Class<? extends ServletContainerInitializer>) Class.forName("com.sun.faces.config.FacesInitializer");
//			ServletContainerInitializer initializer = c.newInstance();
//			initializer.onStartup(null, config.getServletContext());
		} catch (BundleException | IOException e) {
			throw new ServletException(e);
		} finally {
			Thread.currentThread().setContextClassLoader(current);
		}
	}

	@Override
	public void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		try {
			AccessController.doPrivileged((PrivilegedExceptionAction<Void>)() -> {
				ContainerUtil.setThreadContextDatabasePath(req.getContextPath().substring(1));
				AbstractProxyingContext.setThreadContextRequest(req);
				ClassLoader current = Thread.currentThread().getContextClassLoader();
				Thread.currentThread().setContextClassLoader(buildJsfClassLoader(current));
				try {
					delegate.service(req, resp);
				} finally {
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
		}
	}

	private ClassLoader buildJsfClassLoader(ClassLoader delegate) throws BundleException, IOException {
		// TODO support extension points?
		// TODO see if we can make this just delegate to bundles and not use the filesystem.
		//   The filesystem bits come from JSP/Jasper, and it's untested whether JSF has the same
		//   requirements.
		
		return new ClassLoader(delegate) {
			@Override
			public Class<?> loadClass(String name) throws ClassNotFoundException {
				if(name != null && name.startsWith("com.sun.faces")) { //$NON-NLS-1$
					return NSFJsfServlet.class.getClassLoader().loadClass(name);
				}
				return super.loadClass(name);
			}
		};
		
//		List<File> classpath = new ArrayList<>();
//		classpath.addAll(JsfServletFactory.buildBundleClassPath());
//		
//		@SuppressWarnings("deprecation")
//		URL[] path = classpath
//			.stream()
//			.map(t -> {
//				try {
//					return t.toURL();
//				} catch (MalformedURLException e) {
//					throw new UncheckedIOException(e);
//				}
//			})
//			.toArray(URL[]::new);
//		
//		// ClassLoaders look to their delegate first (which will find XPages), so wrap this in a custom subclass
//		return new URLClassLoader(path, delegate) {
//			@Override
//			protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
//				if(name != null && name.startsWith("com.sun.faces")) { //$NON-NLS-1$
//					try {
//						Class<?> c = findClass(name);
//						if(resolve) {
//							resolveClass(c);
//						}
//						return c;
//					} catch(ClassNotFoundException e) {
//						// Fall through
//					}
//				}
//				return super.loadClass(name, resolve);
//			}
//		};
		
	}
}
