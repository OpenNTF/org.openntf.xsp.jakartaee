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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

@SuppressWarnings({ "rawtypes", "deprecation" })
class NewHttpServletRequestWrapper implements javax.servlet.http.HttpServletRequest {
	final HttpServletRequest delegate;
	
	public NewHttpServletRequestWrapper(HttpServletRequest delegate) {
		this.delegate = delegate;
	}
	
	public NewHttpServletRequestWrapper(ServletRequest delegate) {
		this.delegate = (HttpServletRequest)delegate;
	}

	@Override
	public Object getAttribute(String arg0) {
		return delegate.getAttribute(arg0);
	}

	@Override
	public Enumeration getAttributeNames() {
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
	public String getContentType() {
		return delegate.getContentType();
	}

	@Override
	public javax.servlet.ServletInputStream getInputStream() throws IOException {
		return ServletUtil.newToOld(delegate.getInputStream());
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
	public Enumeration getLocales() {
		return delegate.getLocales();
	}

	@Override
	public String getParameter(String arg0) {
		return delegate.getParameter(arg0);
	}

	@Override
	public Map getParameterMap() {
		return delegate.getParameterMap();
	}

	@Override
	public Enumeration getParameterNames() {
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
	public javax.servlet.RequestDispatcher getRequestDispatcher(String arg0) {
		return ServletUtil.newToOld(delegate.getRequestDispatcher(arg0));
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
	public boolean isSecure() {
		return delegate.isSecure();
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
	public void setCharacterEncoding(String arg0) throws UnsupportedEncodingException {
		delegate.setCharacterEncoding(arg0);
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
	public javax.servlet.http.Cookie[] getCookies() {
		Cookie[] newCookies = delegate.getCookies();
		if(newCookies == null) {
			return null;
		} else {
			return Arrays.stream(delegate.getCookies())
				.map(ServletUtil::newToOld)
				.toArray(javax.servlet.http.Cookie[]::new);	
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
	public Enumeration getHeaderNames() {
		return delegate.getHeaderNames();
	}

	@Override
	public Enumeration getHeaders(String arg0) {
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
	public javax.servlet.http.HttpSession getSession() {
		return ServletUtil.newToOld(delegate.getSession());
	}

	@Override
	public javax.servlet.http.HttpSession getSession(boolean arg0) {
		return ServletUtil.newToOld(delegate.getSession(arg0));
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
}
