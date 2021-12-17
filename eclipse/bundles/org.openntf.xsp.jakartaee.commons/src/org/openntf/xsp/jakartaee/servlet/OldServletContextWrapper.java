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
package org.openntf.xsp.jakartaee.servlet;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Enumeration;
import java.util.EventListener;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterRegistration;
import jakarta.servlet.FilterRegistration.Dynamic;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.Servlet;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRegistration;
import jakarta.servlet.SessionCookieConfig;
import jakarta.servlet.SessionTrackingMode;
import jakarta.servlet.descriptor.JspConfigDescriptor;

@SuppressWarnings({ "unchecked", "deprecation" })
class OldServletContextWrapper implements ServletContext {
	private static final String UNAVAILABLE_MESSAGE = "Unable to call method on Servlet 2.5 delegate"; //$NON-NLS-1$
	final javax.servlet.ServletContext delegate;
	
	public OldServletContextWrapper(javax.servlet.ServletContext delegate) {
		this.delegate = delegate;
	}

	@Override
	public Dynamic addFilter(String arg0, String arg1) {
		throw unavailable();
	}

	@Override
	public Dynamic addFilter(String arg0, Filter arg1) {
		throw unavailable();
	}

	@Override
	public Dynamic addFilter(String arg0, Class<? extends Filter> arg1) {
		throw unavailable();
	}

	@Override
	public jakarta.servlet.ServletRegistration.Dynamic addJspFile(String arg0, String arg1) {
		throw unavailable();
	}

	@Override
	public void addListener(String arg0) {
		throw unavailable();
	}

	@Override
	public <T extends EventListener> void addListener(T arg0) {
		throw unavailable();
	}

	@Override
	public void addListener(Class<? extends EventListener> arg0) {
		throw unavailable();
	}

	@Override
	public jakarta.servlet.ServletRegistration.Dynamic addServlet(String arg0, String arg1) {
		throw unavailable();
	}

	@Override
	public jakarta.servlet.ServletRegistration.Dynamic addServlet(String arg0, Servlet arg1) {
		throw unavailable();
	}

	@Override
	public jakarta.servlet.ServletRegistration.Dynamic addServlet(String arg0, Class<? extends Servlet> arg1) {
		throw unavailable();
	}

	@Override
	public <T extends Filter> T createFilter(Class<T> arg0) throws ServletException {
		throw unavailable();
	}

	@Override
	public <T extends EventListener> T createListener(Class<T> arg0) throws ServletException {
		throw unavailable();
	}

	@Override
	public <T extends Servlet> T createServlet(Class<T> arg0) throws ServletException {
		throw unavailable();
	}

	@Override
	public void declareRoles(String... arg0) {
		throw unavailable();
	}

	@Override
	public Object getAttribute(String arg0) {
		return delegate.getAttribute(arg0);
	}

	@Override
	public Enumeration<String> getAttributeNames() {
		return delegate.getAttributeNames();
	}

	@Override
	public ClassLoader getClassLoader() {
		return Thread.currentThread().getContextClassLoader();
	}

	@Override
	public ServletContext getContext(String arg0) {
		return ServletUtil.oldToNew(delegate.getContext(arg0));
	}

	@Override
	public String getContextPath() {
		return delegate.getContextPath();
	}

	@Override
	public Set<SessionTrackingMode> getDefaultSessionTrackingModes() {
		return EnumSet.of(SessionTrackingMode.COOKIE);
	}

	@Override
	public int getEffectiveMajorVersion() {
		return 2;
	}

	@Override
	public int getEffectiveMinorVersion() {
		return 5;
	}

	@Override
	public Set<SessionTrackingMode> getEffectiveSessionTrackingModes() {
		return EnumSet.of(SessionTrackingMode.COOKIE);
	}

	@Override
	public FilterRegistration getFilterRegistration(String arg0) {
		// Soft unavailable
		return null;
	}

	@Override
	public Map<String, ? extends FilterRegistration> getFilterRegistrations() {
		// Soft unavailable
		return Collections.emptyMap();
	}

	@Override
	public String getInitParameter(String arg0) {
		return delegate.getInitParameter(arg0);
	}

	@Override
	public Enumeration<String> getInitParameterNames() {
		return delegate.getInitParameterNames();
	}

	@Override
	public JspConfigDescriptor getJspConfigDescriptor() {
		throw unavailable();
	}

	@Override
	public int getMajorVersion() {
		return 2;
	}

	@Override
	public String getMimeType(String arg0) {
		return delegate.getMimeType(arg0);
	}

	@Override
	public int getMinorVersion() {
		return 5;
	}

	@Override
	public RequestDispatcher getNamedDispatcher(String arg0) {
		return ServletUtil.oldToNew(delegate.getNamedDispatcher(arg0));
	}

	@Override
	public String getRealPath(String arg0) {
		return delegate.getRealPath(arg0);
	}

	@Override
	public String getRequestCharacterEncoding() {
		// TODO not assume? This could come from checking headers
		return "UTF-8"; //$NON-NLS-1$
	}

	@Override
	public RequestDispatcher getRequestDispatcher(String arg0) {
		return ServletUtil.oldToNew(delegate.getRequestDispatcher(arg0));
	}

	@Override
	public URL getResource(String arg0) throws MalformedURLException {
		return delegate.getResource(arg0);
	}

	@Override
	public InputStream getResourceAsStream(String arg0) {
		return delegate.getResourceAsStream(arg0);
	}

	@Override
	public Set<String> getResourcePaths(String arg0) {
		return delegate.getResourcePaths(arg0);
	}

	@Override
	public String getResponseCharacterEncoding() {
		return "UTF-8"; //$NON-NLS-1$
	}

	@Override
	public String getServerInfo() {
		return delegate.getServerInfo();
	}

	@Override
	public Servlet getServlet(String arg0) throws ServletException {
		javax.servlet.Servlet result;
		try {
			result = delegate.getServlet(arg0);
		} catch (javax.servlet.ServletException e) {
			throw new ServletException(e);
		}
		return ServletUtil.oldToNew(result);
	}

	@Override
	public String getServletContextName() {
		return delegate.getServletContextName();
	}

	@Override
	public Enumeration<String> getServletNames() {
		return delegate.getServletNames();
	}

	@Override
	public ServletRegistration getServletRegistration(String arg0) {
		// Soft unavailable
		return null;
	}

	@Override
	public Map<String, ? extends ServletRegistration> getServletRegistrations() {
		// Soft unavailable
		return Collections.emptyMap();
	}

	@Override
	public Enumeration<Servlet> getServlets() {
		Enumeration<javax.servlet.Servlet> result = delegate.getServlets();
		if(result == null) {
			return null;
		} else {
			return Collections.enumeration(
				Collections.list(result)
					.stream()
					.map(ServletUtil::oldToNew)
					.collect(Collectors.toList())
			);
		}
	}

	@Override
	public SessionCookieConfig getSessionCookieConfig() {
		throw unavailable();
	}

	@Override
	public int getSessionTimeout() {
		// Soft unavailable
		return 30;
	}

	@Override
	public String getVirtualServerName() {
		// Soft unavailable
		return ""; //$NON-NLS-1$
	}

	@Override
	public void log(String arg0) {
		delegate.log(arg0);
	}

	@Override
	public void log(Exception arg0, String arg1) {
		delegate.log(arg0, arg1);
	}

	@Override
	public void log(String arg0, Throwable arg1) {
		delegate.log(arg0, arg1);
	}

	@Override
	public void removeAttribute(String arg0) {
		delegate.removeAttribute(arg0);
	}

	@Override
	public void setAttribute(String arg0, Object arg1) {
		delegate.setAttribute(arg0, arg1);
	}

	@Override
	public boolean setInitParameter(String arg0, String arg1) {
		// Soft unavailable
		return false;
	}

	@Override
	public void setRequestCharacterEncoding(String arg0) {
		throw unavailable();
	}

	@Override
	public void setResponseCharacterEncoding(String arg0) {
		throw unavailable();
	}

	@Override
	public void setSessionTimeout(int arg0) {
		// Soft unavailable
	}

	@Override
	public void setSessionTrackingModes(Set<SessionTrackingMode> arg0) {
		// Soft unavailable
	}

	private RuntimeException unavailable() {
		return new UnsupportedOperationException(UNAVAILABLE_MESSAGE);
	}
}
