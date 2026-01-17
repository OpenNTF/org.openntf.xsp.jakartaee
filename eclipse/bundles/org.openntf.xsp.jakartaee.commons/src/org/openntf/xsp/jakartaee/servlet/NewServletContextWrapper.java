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
package org.openntf.xsp.jakartaee.servlet;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Set;

import jakarta.servlet.ServletContext;

@SuppressWarnings("rawtypes")
class NewServletContextWrapper implements javax.servlet.ServletContext {
	final ServletContext delegate;

	public NewServletContextWrapper(final ServletContext delegate) {
		this.delegate = delegate;
	}

	@Override
	public Object getAttribute(final String arg0) {
		return delegate.getAttribute(arg0);
	}

	@Override
	public Enumeration getAttributeNames() {
		return delegate.getAttributeNames();
	}

	@Override
	public javax.servlet.ServletContext getContext(final String arg0) {
		return ServletUtil.newToOld(delegate.getContext(arg0));
	}

	@Override
	public String getContextPath() {
		return delegate.getContextPath();
	}

	@Override
	public String getInitParameter(final String arg0) {
		return delegate.getInitParameter(arg0);
	}

	@Override
	public Enumeration getInitParameterNames() {
		return delegate.getInitParameterNames();
	}

	@Override
	public int getMajorVersion() {
		return delegate.getMinorVersion();
	}

	@Override
	public String getMimeType(final String arg0) {
		return delegate.getMimeType(arg0);
	}

	@Override
	public int getMinorVersion() {
		return delegate.getMinorVersion();
	}

	@Override
	public javax.servlet.RequestDispatcher getNamedDispatcher(final String arg0) {
		return ServletUtil.newToOld(delegate.getNamedDispatcher(arg0));
	}

	@Override
	public String getRealPath(final String arg0) {
		return delegate.getRealPath(arg0);
	}

	@Override
	public javax.servlet.RequestDispatcher getRequestDispatcher(final String arg0) {
		return ServletUtil.newToOld(delegate.getRequestDispatcher(arg0));
	}

	@Override
	public URL getResource(final String arg0) throws MalformedURLException {
		return delegate.getResource(arg0);
	}

	@Override
	public InputStream getResourceAsStream(final String arg0) {
		return delegate.getResourceAsStream(arg0);
	}

	@Override
	public Set getResourcePaths(final String arg0) {
		return delegate.getResourcePaths(arg0);
	}

	@Override
	public String getServerInfo() {
		return delegate.getServerInfo();
	}

	@Override
	public javax.servlet.Servlet getServlet(final String arg0) throws javax.servlet.ServletException {
		// Removed in Servlet 6
		return null;
	}

	@Override
	public String getServletContextName() {
		return delegate.getServletContextName();
	}

	@Override
	public Enumeration getServletNames() {
		// Removed in Servlet 6
		return Collections.emptyEnumeration();
	}

	@Override
	public Enumeration getServlets() {
		// Removed in Servlet 6
		return Collections.emptyEnumeration();
	}

	@Override
	public void log(final String arg0) {
		delegate.log(arg0);
	}

	@Override
	public void log(final Exception arg0, final String arg1) {
		// Removed in Servlet 6
		delegate.log(arg1, arg0);
	}

	@Override
	public void log(final String arg0, final Throwable arg1) {
		delegate.log(arg0, arg1);
	}

	@Override
	public void removeAttribute(final String arg0) {
		delegate.removeAttribute(arg0);
	}

	@Override
	public void setAttribute(final String arg0, final Object arg1) {
		delegate.setAttribute(arg0, arg1);
	}
}
