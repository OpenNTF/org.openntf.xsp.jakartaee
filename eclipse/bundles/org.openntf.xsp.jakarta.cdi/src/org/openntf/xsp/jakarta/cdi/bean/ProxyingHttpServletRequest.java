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
package org.openntf.xsp.jakarta.cdi.bean;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;

import org.openntf.xsp.jakartaee.module.ComponentModuleLocator;

import jakarta.servlet.AsyncContext;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletConnection;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletMapping;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpUpgradeHandler;
import jakarta.servlet.http.Part;
import jakarta.servlet.http.PushBuilder;

/**
 * @since 3.0.0
 */
class ProxyingHttpServletRequest implements HttpServletRequest {
	public static final HttpServletRequest INSTANCE = new ProxyingHttpServletRequest();

	@Override
	public Object getAttribute(final String name) {
		return delegate().getAttribute(name);
	}


	@Override
	public String getAuthType() {
		return delegate().getAuthType();
	}


	@Override
	public Cookie[] getCookies() {
		return delegate().getCookies();
	}


	@Override
	public Enumeration<String> getAttributeNames() {
		return delegate().getAttributeNames();
	}


	@Override
	public long getDateHeader(final String name) {
		return delegate().getDateHeader(name);
	}


	@Override
	public String getCharacterEncoding() {
		return delegate().getCharacterEncoding();
	}


	@Override
	public void setCharacterEncoding(final String env) throws UnsupportedEncodingException {
		delegate().setCharacterEncoding(env);
	}


	@Override
	public String getHeader(final String name) {
		return delegate().getHeader(name);
	}


	@Override
	public int getContentLength() {
		return delegate().getContentLength();
	}


	@Override
	public Enumeration<String> getHeaders(final String name) {
		return delegate().getHeaders(name);
	}


	@Override
	public long getContentLengthLong() {
		return delegate().getContentLengthLong();
	}


	@Override
	public String getContentType() {
		return delegate().getContentType();
	}


	@Override
	public ServletInputStream getInputStream() throws IOException {
		return delegate().getInputStream();
	}


	@Override
	public Enumeration<String> getHeaderNames() {
		return delegate().getHeaderNames();
	}


	@Override
	public String getParameter(final String name) {
		return delegate().getParameter(name);
	}


	@Override
	public int getIntHeader(final String name) {
		return delegate().getIntHeader(name);
	}


	@Override
	public HttpServletMapping getHttpServletMapping() {
		return delegate().getHttpServletMapping();
	}


	@Override
	public Enumeration<String> getParameterNames() {
		return delegate().getParameterNames();
	}


	@Override
	public String[] getParameterValues(final String name) {
		return delegate().getParameterValues(name);
	}


	@Override
	public Map<String, String[]> getParameterMap() {
		return delegate().getParameterMap();
	}


	@Override
	public String getProtocol() {
		return delegate().getProtocol();
	}


	@Override
	public String getScheme() {
		return delegate().getScheme();
	}


	@Override
	public String getServerName() {
		return delegate().getServerName();
	}


	@Override
	public int getServerPort() {
		return delegate().getServerPort();
	}


	@Override
	public String getMethod() {
		return delegate().getMethod();
	}


	@Override
	public BufferedReader getReader() throws IOException {
		return delegate().getReader();
	}


	@Override
	public String getPathInfo() {
		return delegate().getPathInfo();
	}


	@Override
	public String getRemoteAddr() {
		return delegate().getRemoteAddr();
	}


	@Override
	public String getRemoteHost() {
		return delegate().getRemoteHost();
	}


	@Override
	public String getPathTranslated() {
		return delegate().getPathTranslated();
	}


	@Override
	public void setAttribute(final String name, final Object o) {
		delegate().setAttribute(name, o);
	}


	@Override
	public PushBuilder newPushBuilder() {
		return delegate().newPushBuilder();
	}


	@Override
	public void removeAttribute(final String name) {
		delegate().removeAttribute(name);
	}


	@Override
	public String getContextPath() {
		return delegate().getContextPath();
	}


	@Override
	public Locale getLocale() {
		return delegate().getLocale();
	}


	@Override
	public Enumeration<Locale> getLocales() {
		return delegate().getLocales();
	}


	@Override
	public boolean isSecure() {
		return delegate().isSecure();
	}


	@Override
	public RequestDispatcher getRequestDispatcher(final String path) {
		return delegate().getRequestDispatcher(path);
	}


	@Override
	public String getQueryString() {
		return delegate().getQueryString();
	}


	@Override
	public String getRemoteUser() {
		return delegate().getRemoteUser();
	}


	@Override
	public boolean isUserInRole(final String role) {
		return delegate().isUserInRole(role);
	}


	@Override
	public int getRemotePort() {
		return delegate().getRemotePort();
	}


	@Override
	public Principal getUserPrincipal() {
		return delegate().getUserPrincipal();
	}


	@Override
	public String getLocalName() {
		return delegate().getLocalName();
	}


	@Override
	public String getLocalAddr() {
		return delegate().getLocalAddr();
	}


	@Override
	public String getRequestedSessionId() {
		return delegate().getRequestedSessionId();
	}


	@Override
	public int getLocalPort() {
		return delegate().getLocalPort();
	}


	@Override
	public String getRequestURI() {
		return delegate().getRequestURI();
	}


	@Override
	public ServletContext getServletContext() {
		return delegate().getServletContext();
	}


	@Override
	public AsyncContext startAsync() throws IllegalStateException {
		return delegate().startAsync();
	}


	@Override
	public StringBuffer getRequestURL() {
		return delegate().getRequestURL();
	}


	@Override
	public String getServletPath() {
		return delegate().getServletPath();
	}


	@Override
	public AsyncContext startAsync(final ServletRequest servletRequest, final ServletResponse servletResponse)
			throws IllegalStateException {
		return delegate().startAsync(servletRequest, servletResponse);
	}


	@Override
	public HttpSession getSession(final boolean create) {
		return delegate().getSession(create);
	}


	@Override
	public HttpSession getSession() {
		return delegate().getSession();
	}


	@Override
	public String changeSessionId() {
		return delegate().changeSessionId();
	}


	@Override
	public boolean isRequestedSessionIdValid() {
		return delegate().isRequestedSessionIdValid();
	}


	@Override
	public boolean isRequestedSessionIdFromCookie() {
		return delegate().isRequestedSessionIdFromCookie();
	}


	@Override
	public boolean isRequestedSessionIdFromURL() {
		return delegate().isRequestedSessionIdFromURL();
	}


	@Override
	public boolean authenticate(final HttpServletResponse response) throws IOException, ServletException {
		return delegate().authenticate(response);
	}


	@Override
	public boolean isAsyncStarted() {
		return delegate().isAsyncStarted();
	}


	@Override
	public void login(final String username, final String password) throws ServletException {
		delegate().login(username, password);
	}


	@Override
	public boolean isAsyncSupported() {
		return delegate().isAsyncSupported();
	}


	@Override
	public AsyncContext getAsyncContext() {
		return delegate().getAsyncContext();
	}


	@Override
	public DispatcherType getDispatcherType() {
		return delegate().getDispatcherType();
	}


	@Override
	public void logout() throws ServletException {
		delegate().logout();
	}


	@Override
	public Collection<Part> getParts() throws IOException, ServletException {
		return delegate().getParts();
	}


	@Override
	public String getRequestId() {
		return delegate().getRequestId();
	}


	@Override
	public String getProtocolRequestId() {
		return delegate().getProtocolRequestId();
	}


	@Override
	public Part getPart(final String name) throws IOException, ServletException {
		return delegate().getPart(name);
	}


	@Override
	public ServletConnection getServletConnection() {
		return delegate().getServletConnection();
	}


	@Override
	public <T extends HttpUpgradeHandler> T upgrade(final Class<T> handlerClass) throws IOException, ServletException {
		return delegate().upgrade(handlerClass);
	}


	@Override
	public Map<String, String> getTrailerFields() {
		return delegate().getTrailerFields();
	}


	@Override
	public boolean isTrailerFieldsReady() {
		return delegate().isTrailerFieldsReady();
	}


	private HttpServletRequest delegate() {
		return ComponentModuleLocator.getDefault()
			.flatMap(ComponentModuleLocator::getServletRequest)
			.orElse(null);
	}
}
