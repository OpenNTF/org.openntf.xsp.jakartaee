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
package org.openntf.xsp.jakartaee.jasapi;

import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;

import jakarta.servlet.http.Cookie;

/**
 * Request object for an incoming JavaSapi event.
 *
 * @author Jesse Gallagher
 * @since 2.13.0
 */
public interface JavaSapiRequest {
	String getRequestURI();

	StringBuffer getRequestURL();

	String getQueryString();

	String getServerVariable(String varName);

	String getAllHeaders();

	String getHeader(String headerName);

	Cookie[] getCookies();

	String getMethod();

	String getContextPath();

	String getRemoteUser();

	String getRemoteUserGroups();

	String getRemoteAddr();

	String getRemoteHost();

	int getIntHeader(String headerName);

	long getDateHeader(String headerName);

	String getScheme();

	int getServerPort();

	String getProtocol();

	String getContentType();

	String getServerName();

	int getContentLength();

	Enumeration<String> getHeaderNames();

	String getAuthType();

	boolean getPreviewServer();

	boolean isSecure();

	String getLocalName();

	int getLocalPort();

	Locale getLocale();

	Enumeration<Locale> getLocales();

	String getCharacterEncoding();

	Object getAttribute(String attributeName);

	String[] getParameterValues(String paramName);

	String getParameter(String paramName);

	Enumeration<String> getParameterNames();

	Map<String, String[]> getParameterMap();

	boolean isRequestProcessed();

	boolean userInUserCache();

	String getUserNameToAuthenticate();

	String getUserPasswordToAuthenticate();

	String getUserAuthMappedResource();

	void setProcessRequestOwner(String var1, boolean var2);

	void setAuthenticatedUserName(String userName, String authType);

	void setRequestHeader(String headerName, String value);

	void rewriteUrl(String url);
}
