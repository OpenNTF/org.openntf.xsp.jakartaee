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

	public Object getAttribute(String name) {
		return delegate().getAttribute(name);
	}


	public String getAuthType() {
		return delegate().getAuthType();
	}


	public Cookie[] getCookies() {
		return delegate().getCookies();
	}


	public Enumeration<String> getAttributeNames() {
		return delegate().getAttributeNames();
	}


	public long getDateHeader(String name) {
		return delegate().getDateHeader(name);
	}


	public String getCharacterEncoding() {
		return delegate().getCharacterEncoding();
	}


	public void setCharacterEncoding(String env) throws UnsupportedEncodingException {
		delegate().setCharacterEncoding(env);
	}


	public String getHeader(String name) {
		return delegate().getHeader(name);
	}


	public int getContentLength() {
		return delegate().getContentLength();
	}


	public Enumeration<String> getHeaders(String name) {
		return delegate().getHeaders(name);
	}


	public long getContentLengthLong() {
		return delegate().getContentLengthLong();
	}


	public String getContentType() {
		return delegate().getContentType();
	}


	public ServletInputStream getInputStream() throws IOException {
		return delegate().getInputStream();
	}


	public Enumeration<String> getHeaderNames() {
		return delegate().getHeaderNames();
	}


	public String getParameter(String name) {
		return delegate().getParameter(name);
	}


	public int getIntHeader(String name) {
		return delegate().getIntHeader(name);
	}


	public HttpServletMapping getHttpServletMapping() {
		return delegate().getHttpServletMapping();
	}


	public Enumeration<String> getParameterNames() {
		return delegate().getParameterNames();
	}


	public String[] getParameterValues(String name) {
		return delegate().getParameterValues(name);
	}


	public Map<String, String[]> getParameterMap() {
		return delegate().getParameterMap();
	}


	public String getProtocol() {
		return delegate().getProtocol();
	}


	public String getScheme() {
		return delegate().getScheme();
	}


	public String getServerName() {
		return delegate().getServerName();
	}


	public int getServerPort() {
		return delegate().getServerPort();
	}


	public String getMethod() {
		return delegate().getMethod();
	}


	public BufferedReader getReader() throws IOException {
		return delegate().getReader();
	}


	public String getPathInfo() {
		return delegate().getPathInfo();
	}


	public String getRemoteAddr() {
		return delegate().getRemoteAddr();
	}


	public String getRemoteHost() {
		return delegate().getRemoteHost();
	}


	public String getPathTranslated() {
		return delegate().getPathTranslated();
	}


	public void setAttribute(String name, Object o) {
		delegate().setAttribute(name, o);
	}


	public PushBuilder newPushBuilder() {
		return delegate().newPushBuilder();
	}


	public void removeAttribute(String name) {
		delegate().removeAttribute(name);
	}


	public String getContextPath() {
		return delegate().getContextPath();
	}


	public Locale getLocale() {
		return delegate().getLocale();
	}


	public Enumeration<Locale> getLocales() {
		return delegate().getLocales();
	}


	public boolean isSecure() {
		return delegate().isSecure();
	}


	public RequestDispatcher getRequestDispatcher(String path) {
		return delegate().getRequestDispatcher(path);
	}


	public String getQueryString() {
		return delegate().getQueryString();
	}


	public String getRemoteUser() {
		return delegate().getRemoteUser();
	}


	public boolean isUserInRole(String role) {
		return delegate().isUserInRole(role);
	}


	public int getRemotePort() {
		return delegate().getRemotePort();
	}


	public Principal getUserPrincipal() {
		return delegate().getUserPrincipal();
	}


	public String getLocalName() {
		return delegate().getLocalName();
	}


	public String getLocalAddr() {
		return delegate().getLocalAddr();
	}


	public String getRequestedSessionId() {
		return delegate().getRequestedSessionId();
	}


	public int getLocalPort() {
		return delegate().getLocalPort();
	}


	public String getRequestURI() {
		return delegate().getRequestURI();
	}


	public ServletContext getServletContext() {
		return delegate().getServletContext();
	}


	public AsyncContext startAsync() throws IllegalStateException {
		return delegate().startAsync();
	}


	public StringBuffer getRequestURL() {
		return delegate().getRequestURL();
	}


	public String getServletPath() {
		return delegate().getServletPath();
	}


	public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse)
			throws IllegalStateException {
		return delegate().startAsync(servletRequest, servletResponse);
	}


	public HttpSession getSession(boolean create) {
		return delegate().getSession(create);
	}


	public HttpSession getSession() {
		return delegate().getSession();
	}


	public String changeSessionId() {
		return delegate().changeSessionId();
	}


	public boolean isRequestedSessionIdValid() {
		return delegate().isRequestedSessionIdValid();
	}


	public boolean isRequestedSessionIdFromCookie() {
		return delegate().isRequestedSessionIdFromCookie();
	}


	public boolean isRequestedSessionIdFromURL() {
		return delegate().isRequestedSessionIdFromURL();
	}


	public boolean authenticate(HttpServletResponse response) throws IOException, ServletException {
		return delegate().authenticate(response);
	}


	public boolean isAsyncStarted() {
		return delegate().isAsyncStarted();
	}


	public void login(String username, String password) throws ServletException {
		delegate().login(username, password);
	}


	public boolean isAsyncSupported() {
		return delegate().isAsyncSupported();
	}


	public AsyncContext getAsyncContext() {
		return delegate().getAsyncContext();
	}


	public DispatcherType getDispatcherType() {
		return delegate().getDispatcherType();
	}


	public void logout() throws ServletException {
		delegate().logout();
	}


	public Collection<Part> getParts() throws IOException, ServletException {
		return delegate().getParts();
	}


	public String getRequestId() {
		return delegate().getRequestId();
	}


	public String getProtocolRequestId() {
		return delegate().getProtocolRequestId();
	}


	public Part getPart(String name) throws IOException, ServletException {
		return delegate().getPart(name);
	}


	public ServletConnection getServletConnection() {
		return delegate().getServletConnection();
	}


	public <T extends HttpUpgradeHandler> T upgrade(Class<T> handlerClass) throws IOException, ServletException {
		return delegate().upgrade(handlerClass);
	}


	public Map<String, String> getTrailerFields() {
		return delegate().getTrailerFields();
	}


	public boolean isTrailerFieldsReady() {
		return delegate().isTrailerFieldsReady();
	}
	

	private HttpServletRequest delegate() {
		return ComponentModuleLocator.getDefault()
			.flatMap(ComponentModuleLocator::getServletRequest)
			.orElse(null);
	}
}
