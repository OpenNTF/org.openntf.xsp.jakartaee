package org.openntf.xsp.jakarta.security.jasapi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.openntf.xsp.jakartaee.jasapi.JavaSapiRequest;

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
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpUpgradeHandler;
import jakarta.servlet.http.Part;

public class JavaSapiHttpServletRequest implements HttpServletRequest {
	private final JavaSapiRequest delegate;
	
	public JavaSapiHttpServletRequest(JavaSapiRequest delegate) {
		this.delegate = delegate;
	}

	@Override
	public Object getAttribute(String name) {
		return delegate.getAttribute(name);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Enumeration getAttributeNames() {
		return Collections.emptyEnumeration();
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
	public ServletInputStream getInputStream() throws IOException {
		return null;
	}

	@Override
	public String getLocalAddr() {
		return "127.0.0.1"; //$NON-NLS-1$
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

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Enumeration getLocales() {
		return delegate.getLocales();
	}

	@Override
	public String getParameter(String name) {
		return delegate.getParameter(name);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Map getParameterMap() {
		return delegate.getParameterMap();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Enumeration getParameterNames() {
		return delegate.getParameterNames();
	}

	@Override
	public String[] getParameterValues(String name) {
		return delegate.getParameterValues(name);
	}

	@Override
	public String getProtocol() {
		return delegate.getProtocol();
	}

	@Override
	public BufferedReader getReader() throws IOException {
		return null;
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
		return 0;
	}

	@Override
	public RequestDispatcher getRequestDispatcher(String path) {
		return null;
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
	public void removeAttribute(String name) {
		// NOP
	}

	@Override
	public void setAttribute(String name, Object value) {
		// NOP
	}

	@Override
	public void setCharacterEncoding(String enc) throws UnsupportedEncodingException {
		// NOP
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
		return delegate.getCookies();
	}

	@Override
	public long getDateHeader(String name) {
		return delegate.getDateHeader(name);
	}

	@Override
	public String getHeader(String name) {
		return delegate.getHeader(name);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Enumeration getHeaderNames() {
		return delegate.getHeaderNames();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Enumeration getHeaders(String name) {
		String header = delegate.getHeader(name);
		return header == null ? Collections.emptyEnumeration() : Collections.enumeration(List.of(header));
	}

	@Override
	public int getIntHeader(String name) {
		return delegate.getIntHeader(name);
	}

	@Override
	public String getMethod() {
		return delegate.getMethod();
	}

	@Override
	public String getPathInfo() {
		return null;
	}

	@Override
	public String getPathTranslated() {
		return null;
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
		return null;
	}

	@Override
	public String getServletPath() {
		return null;
	}

	@Override
	public HttpSession getSession() {
		return null;
	}

	@Override
	public HttpSession getSession(boolean create) {
		return null;
	}

	@Override
	public Principal getUserPrincipal() {
		return null;
	}

	@Override
	public boolean isRequestedSessionIdFromCookie() {
		return false;
	}

	@Override
	public boolean isRequestedSessionIdFromURL() {
		return false;
	}

	@Override
	public boolean isRequestedSessionIdValid() {
		return false;
	}

	@Override
	public boolean isUserInRole(String name) {
		return false;
	}

	@Override
	public long getContentLengthLong() {
		return delegate.getContentLength();
	}

	@Override
	public ServletContext getServletContext() {
		return null;
	}

	@Override
	public AsyncContext startAsync() throws IllegalStateException {
		return null;
	}

	@Override
	public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse)
			throws IllegalStateException {
		return null;
	}

	@Override
	public boolean isAsyncStarted() {
		return false;
	}

	@Override
	public boolean isAsyncSupported() {
		return false;
	}

	@Override
	public AsyncContext getAsyncContext() {
		return null;
	}

	@Override
	public DispatcherType getDispatcherType() {
		return DispatcherType.REQUEST;
	}

	@Override
	public String getRequestId() {
		return null;
	}

	@Override
	public String getProtocolRequestId() {
		return null;
	}

	@Override
	public ServletConnection getServletConnection() {
		return null;
	}

	@Override
	public String changeSessionId() {
		return null;
	}

	@Override
	public boolean authenticate(HttpServletResponse response) throws IOException, ServletException {
		return false;
	}

	@Override
	public void login(String username, String password) throws ServletException {
		
	}

	@Override
	public void logout() throws ServletException {
		
	}

	@Override
	public Collection<Part> getParts() throws IOException, ServletException {
		return Collections.emptySet();
	}

	@Override
	public Part getPart(String name) throws IOException, ServletException {
		return null;
	}

	@Override
	public <T extends HttpUpgradeHandler> T upgrade(Class<T> handlerClass) throws IOException, ServletException {
		return null;
	}

}