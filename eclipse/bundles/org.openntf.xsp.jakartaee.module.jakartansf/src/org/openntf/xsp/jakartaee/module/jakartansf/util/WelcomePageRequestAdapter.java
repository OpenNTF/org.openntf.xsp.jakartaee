package org.openntf.xsp.jakartaee.module.jakartansf.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;

import com.ibm.commons.util.PathUtil;
import com.ibm.designer.runtime.domino.bootstrap.adapter.DominoHttpXspNativeContext;
import com.ibm.designer.runtime.domino.bootstrap.adapter.HttpServletRequestAdapter;

import lotus.domino.NotesException;

public class WelcomePageRequestAdapter implements HttpServletRequestAdapter, DominoHttpXspNativeContext {
	private final HttpServletRequestAdapter delegate;
	private final String welcomePage;
	
	public WelcomePageRequestAdapter(HttpServletRequestAdapter delegate, String welcomePage) {
		if(!(delegate instanceof DominoHttpXspNativeContext)) {
			throw new IllegalArgumentException("delegate must be an instance of DominoHttpXspNativeContext");
		}
		this.delegate = delegate;
		this.welcomePage = welcomePage;
	}

	public String getAuthType() {
		return delegate.getAuthType();
	}

	public String getCharacterEncoding() {
		return delegate.getCharacterEncoding();
	}

	public int getContentLength() {
		return delegate.getContentLength();
	}

	public String getContentType() {
		return delegate.getContentType();
	}

	public String getContextPath() {
		return delegate.getContextPath();
	}

	public String getConversationId() {
		return delegate.getConversationId();
	}

	public Cookie[] getCookies() {
		return delegate.getCookies();
	}

	public long getDateHeader(String arg0) {
		return delegate.getDateHeader(arg0);
	}

	public String getHeader(String arg0) {
		return delegate.getHeader(arg0);
	}

	@SuppressWarnings("rawtypes")
	public Enumeration getHeaderNames() {
		return delegate.getHeaderNames();
	}

	@SuppressWarnings("rawtypes")
	public Enumeration getHeaders(String arg0) {
		return delegate.getHeaders(arg0);
	}

	public ServletInputStream getInputStream() throws IOException {
		return delegate.getInputStream();
	}

	public int getIntHeader(String arg0) {
		return delegate.getIntHeader(arg0);
	}

	public String getLocalAddr() {
		return delegate.getLocalAddr();
	}

	public String getLocalName() {
		return delegate.getLocalName();
	}

	public int getLocalPort() {
		return delegate.getLocalPort();
	}

	public Locale getLocale() {
		return delegate.getLocale();
	}

	@SuppressWarnings("rawtypes")
	public Enumeration getLocales() {
		return delegate.getLocales();
	}

	public String getMethod() {
		return delegate.getMethod();
	}

	public String getParameter(String arg0) {
		return delegate.getParameter(arg0);
	}

	@SuppressWarnings("rawtypes")
	public Map getParameterMap() {
		return delegate.getParameterMap();
	}

	@SuppressWarnings("rawtypes")
	public Enumeration getParameterNames() {
		return delegate.getParameterNames();
	}

	public String[] getParameterValues(String arg0) {
		return delegate.getParameterValues(arg0);
	}

	public String getPathInfo() {
		return this.welcomePage;
	}

	public String getPathTranslated() {
		return delegate.getPathTranslated();
	}

	public String getProtocol() {
		return delegate.getProtocol();
	}

	public String getQueryString() {
		return delegate.getQueryString();
	}

	public BufferedReader getReader() throws IOException {
		return delegate.getReader();
	}

	public String getRealPath(String arg0) {
		return delegate.getRealPath(arg0);
	}

	public String getRemoteAddr() {
		return delegate.getRemoteAddr();
	}

	public String getRemoteHost() {
		return delegate.getRemoteHost();
	}

	public int getRemotePort() {
		return delegate.getRemotePort();
	}

	public String getRemoteUser() {
		return delegate.getRemoteUser();
	}

	public Object getRequestDispatcher(String arg0) {
		return delegate.getRequestDispatcher(arg0);
	}

	public String getRequestURI() {
		// "URI" is only the path info
		return PathUtil.concat(delegate.getRequestURI(), welcomePage, '/');
	}

	public StringBuffer getRequestURL() {
		// URL doesn't have the query string
		StringBuffer sup = delegate.getRequestURL();
		if(sup.charAt(sup.length()-1) == '/') {
			sup.append(welcomePage.substring(1));
		} else {
			sup.append(welcomePage);
		}
		return sup;
	}

	public String getRequestedSessionId() {
		return delegate.getRequestedSessionId();
	}

	public String getScheme() {
		return delegate.getScheme();
	}

	public String getServerName() {
		return delegate.getServerName();
	}

	public int getServerPort() {
		return delegate.getServerPort();
	}

	public String getServletPath() {
		return delegate.getServletPath();
	}

	public Principal getUserPrincipal() {
		return delegate.getUserPrincipal();
	}

	public boolean isRequestedSessionIdFromCookie() {
		return delegate.isRequestedSessionIdFromCookie();
	}

	public boolean isRequestedSessionIdFromURL() {
		return delegate.isRequestedSessionIdFromURL();
	}

	public boolean isRequestedSessionIdFromUrl() {
		return delegate.isRequestedSessionIdFromUrl();
	}

	public boolean isRequestedSessionIdValid() {
		return delegate.isRequestedSessionIdValid();
	}

	public boolean isSecure() {
		return delegate.isSecure();
	}

	public boolean isUserInRole(String arg0) {
		return delegate.isUserInRole(arg0);
	}

	public void setCharacterEncoding(String arg0) throws UnsupportedEncodingException {
		delegate.setCharacterEncoding(arg0);
	}

	public void setUserPrincipal(Principal arg0) {
		delegate.setUserPrincipal(arg0);
	}

	@Override
	public long getServerDBHandle() {
		return ((DominoHttpXspNativeContext)delegate).getServerDBHandle();
	}

	@Override
	public long getUserDBHandle() {
		return ((DominoHttpXspNativeContext)delegate).getUserDBHandle();
	}

	@Override
	public long getUserListHandle() {
		return ((DominoHttpXspNativeContext)delegate).getUserListHandle();
	}

	@Override
	public boolean getEnforceAccess() {
		return ((DominoHttpXspNativeContext)delegate).getEnforceAccess();
	}

	@Override
	public boolean getPreviewServer() {
		return ((DominoHttpXspNativeContext)delegate).getPreviewServer();
	}

	@Override
	public Object getLsxbeSession(String paramString) throws NotesException {
		return ((DominoHttpXspNativeContext)delegate).getLsxbeSession(paramString);
	}

	@Override
	public String getServerVariable(String paramString) {
		return ((DominoHttpXspNativeContext)delegate).getServerVariable(paramString);
	}
}
