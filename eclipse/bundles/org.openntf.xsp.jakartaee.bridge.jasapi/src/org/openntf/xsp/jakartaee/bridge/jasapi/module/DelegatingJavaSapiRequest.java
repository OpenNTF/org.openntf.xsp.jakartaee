/**
 * Copyright (c) 2018-2025 Contributors to the XPages Jakarta EE Support Project
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
package org.openntf.xsp.jakartaee.bridge.jasapi.module;

import java.util.Arrays;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;

import com.ibm.domino.bridge.http.jasapi.IJavaSapiHttpRequestAdapter;

import org.openntf.xsp.jakartaee.jasapi.JavaSapiRequest;
import org.openntf.xsp.jakartaee.servlet.ServletUtil;

import jakarta.servlet.http.Cookie;

/**
 *
 * @author Jesse Gallagher
 * @since 2.13.0
 */
public class DelegatingJavaSapiRequest implements JavaSapiRequest {
	private final IJavaSapiHttpRequestAdapter delegate;

	public DelegatingJavaSapiRequest(final IJavaSapiHttpRequestAdapter delegate) {
		this.delegate = delegate;
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
	public String getQueryString() {
		return delegate.getQueryString();
	}

	@Override
	public String getServerVariable(final String var1) {
		return delegate.getServerVariable(var1);
	}

	@Override
	public String getAllHeaders() {
		return delegate.getAllHeaders();
	}

	@Override
	public String getHeader(final String var1) {
		return delegate.getHeader(var1);
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
	public String getMethod() {
		return delegate.getMethod();
	}

	@Override
	public String getContextPath() {
		return delegate.getContextPath();
	}

	@Override
	public String getRemoteUser() {
		return delegate.getRemoteUser();
	}

	@Override
	public String getRemoteUserGroups() {
		return delegate.getRemoteUserGroups();
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
	public int getIntHeader(final String var1) {
		return delegate.getIntHeader(var1);
	}

	@Override
	public long getDateHeader(final String var1) {
		return delegate.getDateHeader(var1);
	}

	@Override
	public String getScheme() {
		return delegate.getScheme();
	}

	@Override
	public int getServerPort() {
		return delegate.getServerPort();
	}

	@Override
	public String getProtocol() {
		return delegate.getProtocol();
	}

	@Override
	public String getContentType() {
		return delegate.getContentType();
	}

	@Override
	public String getServerName() {
		return delegate.getServerName();
	}

	@Override
	public int getContentLength() {
		return delegate.getContentLength();
	}

	@Override
	public Enumeration<String> getHeaderNames() {
		return delegate.getHeaderNames();
	}

	@Override
	public String getAuthType() {
		return delegate.getAuthType();
	}

	@Override
	public boolean getPreviewServer() {
		return delegate.getPreviewServer();
	}

	@Override
	public boolean isSecure() {
		return delegate.isSecure();
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

	@SuppressWarnings("unchecked")
	@Override
	public Enumeration<Locale> getLocales() {
		return delegate.getLocales();
	}

	@Override
	public String getCharacterEncoding() {
		return delegate.getCharacterEncoding();
	}

	@Override
	public Object getAttribute(final String var1) {
		return delegate.getAttribute(var1);
	}

	@Override
	public String[] getParameterValues(final String var1) {
		return delegate.getParameterValues(var1);
	}

	@Override
	public String getParameter(final String var1) {
		return delegate.getParameter(var1);
	}

	@Override
	public Enumeration<String> getParameterNames() {
		return delegate.getParameterNames();
	}

	@Override
	public Map<String, String[]> getParameterMap() {
		return delegate.getParameterMap();
	}

	@Override
	public boolean isRequestProcessed() {
		return delegate.isRequestProcessed();
	}

	@Override
	public boolean userInUserCache() {
		return delegate.userInUserCache();
	}

	@Override
	public String getUserNameToAuthenticate() {
		return delegate.getUserNameToAuthenticate();
	}

	@Override
	public String getUserPasswordToAuthenticate() {
		return delegate.getUserPasswordToAuthenticate();
	}

	@Override
	public String getUserAuthMappedResource() {
		return delegate.getUserAuthMappedResource();
	}

	@Override
	public void setProcessRequestOwner(final String var1, final boolean var2) {
		delegate.setProcessRequestOwner(var1, var2);
	}

	@Override
	public void setAuthenticatedUserName(final String var1, final String var2) {
		delegate.setAuthenticatedUserName(var1, var2);
	}

	@Override
	public void setRequestHeader(final String var1, final String var2) {
		delegate.setRequestHeader(var1, var2);
	}

	@Override
	public void rewriteUrl(final String var1) {
		delegate.rewriteUrl(var1);
	}



}
