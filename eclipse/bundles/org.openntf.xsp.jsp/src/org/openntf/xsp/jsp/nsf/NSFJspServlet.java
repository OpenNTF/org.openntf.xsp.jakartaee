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
package org.openntf.xsp.jsp.nsf;

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
import java.util.HashMap;
import java.util.List;

import org.apache.jasper.Constants;
import org.apache.jasper.servlet.JspServlet;
import org.apache.jasper.xmlparser.ParserUtils;
import org.openntf.xsp.jakartaee.AbstractXspLifecycleServlet;
import org.openntf.xsp.jsp.EarlyInitFactory;
import org.openntf.xsp.jsp.el.NSFELResolver;
import org.osgi.framework.BundleException;

import com.ibm.designer.runtime.domino.adapter.ComponentModule;
import com.ibm.xsp.application.ApplicationEx;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
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
				context.setAttribute(Constants.JSP_TLD_URI_TO_LOCATION_MAP, buildJstlDtdMap());
				
				ClassLoader current = Thread.currentThread().getContextClassLoader();
				Thread.currentThread().setContextClassLoader(buildJspClassLoader(current));
				try {
					ParserUtils.setDtdResourcePrefix(EarlyInitFactory.getServletDtdPath().toUri().toString());
					delegate.service(request, response);
				} finally {
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
			response.getWriter().flush();
			response.flushBuffer();
		}
	}
	
	@Override
	public void destroy() {
		super.destroy();
		delegate.destroy();
	}

	private ClassLoader buildJspClassLoader(ClassLoader delegate) throws BundleException, IOException {
		// TODO support extension points?
		
		List<File> classpath = new ArrayList<>();
		// Add the JSTL fragment explicitly
		// Jasper reads it as a jar: URL to get the TLD files, but then loads resources via its ClassLoader
//		classpath.add(FileLocator.getBundleFile(Platform.getBundle("org.glassfish.web.jakarta.servlet.jsp.jstl"))); //$NON-NLS-1$
//		classpath.addAll(JspServletFactory.buildBundleClassPath());
		
		URL[] path = classpath
			.stream()
			.map(File::toURI)
			// Signal to TldScanner that this is a JAR URL
			.map(uri -> "jar:" + uri + "!/") //$NON-NLS-1$ //$NON-NLS-2$
			.map(t -> {
				try {
					return new URL(t);
				} catch (MalformedURLException e) {
					throw new UncheckedIOException(e);
				}
			})
			.toArray(URL[]::new);
		return new URLClassLoader(path, delegate);
	}
	
	// Must be a HashMap, as TldScanner casts it as such
	// It's a map of URI to [JAR file path, resource name]
	// See also TagLibraryInfoImpl
	private HashMap<String, String[]> buildJstlDtdMap() throws IOException {
		String jstl = EarlyInitFactory.getDeployedJstlBundle().toUri().toString();
		
		HashMap<String, String[]> result = new HashMap<>();
		
		result.put("http://java.sun.com/jsp/jstl/functions", new String[] { jstl, "META-INF/fn.tld" }); //$NON-NLS-1$ //$NON-NLS-2$
		result.put("http://java.sun.com/jsp/jstl/core", new String[] { jstl, "META-INF/c.tld" }); //$NON-NLS-1$ //$NON-NLS-2$
		result.put("http://java.sun.com/jsp/jstl/fmt", new String[] { jstl, "META-INF/fmt.tld" }); //$NON-NLS-1$ //$NON-NLS-2$
		result.put("http://java.sun.com/jsp/jstl/sql", new String[] { jstl, "META-INF/sql.tld" }); //$NON-NLS-1$ //$NON-NLS-2$
		result.put("http://java.sun.com/jsp/jstl/xml", new String[] { jstl, "META-INF/x.tld" }); //$NON-NLS-1$ //$NON-NLS-2$
		
		return result;
	}
}
