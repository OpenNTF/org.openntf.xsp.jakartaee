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
package org.openntf.xsp.jakartaee.servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import jakarta.servlet.AsyncContext;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletRequestAttributeEvent;
import jakarta.servlet.ServletRequestAttributeListener;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpUpgradeHandler;
import jakarta.servlet.http.Part;

@SuppressWarnings("unchecked")
class OldHttpServletRequestWrapper implements HttpServletRequest {
	final javax.servlet.ServletContext context;
	final javax.servlet.http.HttpServletRequest delegate;
	
	public OldHttpServletRequestWrapper(javax.servlet.ServletContext context, javax.servlet.http.HttpServletRequest delegate) {
		this.context = context;
		this.delegate = delegate;
	}
	
	void addListener(ServletRequestAttributeListener listener) {
		this.getAttrListeners().add(listener);
	}

	@Override
	public AsyncContext getAsyncContext() {
		return null;
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
	public String getCharacterEncoding() {
		return delegate.getCharacterEncoding();
	}

	@Override
	public int getContentLength() {
		return delegate.getContentLength();
	}

	@Override
	public long getContentLengthLong() {
		return Integer.toUnsignedLong(delegate.getContentLength());
	}

	@Override
	public String getContentType() {
		return delegate.getContentType();
	}

	@Override
	public DispatcherType getDispatcherType() {
		return DispatcherType.REQUEST;
	}

	@Override
	public ServletInputStream getInputStream() throws IOException {
		return ServletUtil.oldToNew(delegate.getInputStream());
	}

	@Override
	public String getLocalAddr() {
		return delegate.getLocalAddr();
	}

	@Override
	public String getLocalName() {
		return delegate.getLocalName();
	}

	@Override
	public int getLocalPort() {
		return delegate.getLocalPort();
	}

	@Override
	public Locale getLocale() {
		return delegate.getLocale();
	}

	@Override
	public Enumeration<Locale> getLocales() {
		return delegate.getLocales();
	}

	@Override
	public String getParameter(String arg0) {
		return delegate.getParameter(arg0);
	}

	@Override
	public Map<String, String[]> getParameterMap() {
		return delegate.getParameterMap();
	}

	@Override
	public Enumeration<String> getParameterNames() {
		return delegate.getParameterNames();
	}

	@Override
	public String[] getParameterValues(String arg0) {
		return delegate.getParameterValues(arg0);
	}

	@Override
	public String getProtocol() {
		return delegate.getProtocol();
	}

	@Override
	public BufferedReader getReader() throws IOException {
		return delegate.getReader();
	}

	@SuppressWarnings("deprecation")
	@Override
	public String getRealPath(String arg0) {
		return delegate.getRealPath(arg0);
	}

	@Override
	public String getRemoteAddr() {
		return delegate.getRemoteAddr();
	}

	@Override
	public String getRemoteHost() {
		return delegate.getRemoteHost();
	}

	@Override
	public int getRemotePort() {
		return delegate.getRemotePort();
	}

	@Override
	public RequestDispatcher getRequestDispatcher(String arg0) {
		return ServletUtil.oldToNew(delegate.getRequestDispatcher(arg0));
	}

	@Override
	public String getScheme() {
		return delegate.getScheme();
	}

	@Override
	public String getServerName() {
		return delegate.getServerName();
	}

	@Override
	public int getServerPort() {
		return delegate.getServerPort();
	}

	@Override
	public ServletContext getServletContext() {
		return ServletUtil.oldToNew(getContextPath(), context);
	}

	@Override
	public boolean isAsyncStarted() {
		// Soft unavailable
		return false;
	}

	@Override
	public boolean isAsyncSupported() {
		return false;
	}

	@Override
	public boolean isSecure() {
		return delegate.isSecure();
	}

	@Override
	public void removeAttribute(String name) {
		Object val = delegate.getAttribute(name);
		delegate.removeAttribute(name);
		this.getAttrListeners().forEach(listener ->
			listener.attributeRemoved(new ServletRequestAttributeEvent(getServletContext(), this, name, val))
		);
	}

	@Override
	public void setAttribute(String name, Object value) {
		boolean exists = Collections.list(this.getAttributeNames()).contains(name);
		Object oldVal = delegate.getAttribute(name);
		delegate.setAttribute(name, value);
		if(exists) {
			this.getAttrListeners().forEach(listener ->
				listener.attributeReplaced(new ServletRequestAttributeEvent(getServletContext(), this, name, oldVal))
			);
		}
		this.getAttrListeners().forEach(listener ->
			listener.attributeAdded(new ServletRequestAttributeEvent(getServletContext(), this, name, value))
		);
	}

	@Override
	public void setCharacterEncoding(String arg0) throws UnsupportedEncodingException {
		delegate.setCharacterEncoding(arg0);
	}

	@Override
	public AsyncContext startAsync() throws IllegalStateException {
		throw new IllegalStateException("Async unsupported");
	}

	@Override
	public AsyncContext startAsync(ServletRequest arg0, ServletResponse arg1) throws IllegalStateException {
		throw new IllegalStateException("Async unsupported");
	}

	@Override
	public boolean authenticate(HttpServletResponse arg0) throws IOException, ServletException {
		// Soft unsupported
		return false;
	}

	@Override
	public String changeSessionId() {
		// Soft unsupported
		return null;
	}

	@Override
	public String getAuthType() {
		return delegate.getAuthType();
	}

	@Override
	public String getContextPath() {
		return delegate.getContextPath();
	}

	@Override
	public Cookie[] getCookies() {
		javax.servlet.http.Cookie[] oldCookies = delegate.getCookies();
		if(oldCookies == null) {
			return null;
		} else {
			return Arrays.stream(oldCookies)
				.map(ServletUtil::oldToNew)
				.toArray(Cookie[]::new);
		}
	}

	@Override
	public long getDateHeader(String arg0) {
		return delegate.getDateHeader(arg0);
	}

	@Override
	public String getHeader(String arg0) {
		return delegate.getHeader(arg0);
	}

	@Override
	public Enumeration<String> getHeaderNames() {
		return delegate.getHeaderNames();
	}

	@Override
	public Enumeration<String> getHeaders(String arg0) {
		return delegate.getHeaders(arg0);
	}

	@Override
	public int getIntHeader(String arg0) {
		return delegate.getIntHeader(arg0);
	}

	@Override
	public String getMethod() {
		return delegate.getMethod();
	}

	@Override
	public Part getPart(String arg0) throws IOException, ServletException {
		// Soft unsupported
		return null;
	}

	@Override
	public Collection<Part> getParts() throws IOException, ServletException {
		// Soft unsupported
		return Collections.emptySet();
	}

	@Override
	public String getPathInfo() {
		return delegate.getPathInfo();
	}

	@Override
	public String getPathTranslated() {
		return delegate.getPathTranslated();
	}

	@Override
	public String getQueryString() {
		return delegate.getQueryString();
	}

	@Override
	public String getRemoteUser() {
		return delegate.getRemoteUser();
	}

	@Override
	public String getRequestURI() {
		return delegate.getRequestURI();
	}

	@Override
	public StringBuffer getRequestURL() {
		return delegate.getRequestURL();
	}

	@Override
	public String getRequestedSessionId() {
		return delegate.getRequestedSessionId();
	}

	@Override
	public String getServletPath() {
		return delegate.getServletPath();
	}

	@Override
	public HttpSession getSession() {
		return ServletUtil.oldToNew(delegate.getSession());
	}

	@Override
	public HttpSession getSession(boolean arg0) {
		return ServletUtil.oldToNew(delegate.getSession(arg0));
	}

	@Override
	public Principal getUserPrincipal() {
		return delegate.getUserPrincipal();
	}

	@Override
	public boolean isRequestedSessionIdFromCookie() {
		return delegate.isRequestedSessionIdFromCookie();
	}

	@Override
	public boolean isRequestedSessionIdFromURL() {
		return delegate.isRequestedSessionIdFromURL();
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean isRequestedSessionIdFromUrl() {
		return delegate.isRequestedSessionIdFromUrl();
	}

	@Override
	public boolean isRequestedSessionIdValid() {
		return delegate.isRequestedSessionIdValid();
	}

	@Override
	public boolean isUserInRole(String arg0) {
		return delegate.isUserInRole(arg0);
	}

	@Override
	public void login(String arg0, String arg1) throws ServletException {
		throw new ServletException("Login unsupported");
	}

	@Override
	public void logout() throws ServletException {
		// Soft unsupported
	}

	@Override
	public <T extends HttpUpgradeHandler> T upgrade(Class<T> arg0) throws IOException, ServletException {
		throw new ServletException("Upgrade unsupported");
	}
	
	// *******************************************************************************
	// * Internal utility methods
	// *******************************************************************************
	
	private final String ATTR_LISTENERS = OldHttpServletRequestWrapper.class.getName() + "_attrListeners"; //$NON-NLS-1$
	
	private Set<ServletRequestAttributeListener> getAttrListeners() {
		Set<ServletRequestAttributeListener> result = (Set<ServletRequestAttributeListener>)delegate.getAttribute(ATTR_LISTENERS);
		if(result == null) {
			result = new HashSet<>();
			delegate.setAttribute(ATTR_LISTENERS, result);
		}
		return result;
	}
}