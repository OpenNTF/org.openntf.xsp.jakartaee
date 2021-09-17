package org.openntf.xsp.jakartaee.servlet;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.Servlet;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;

@SuppressWarnings("rawtypes")
public class NewServletContextWrapper implements javax.servlet.ServletContext {
	private final ServletContext delegate;
	
	public NewServletContextWrapper(ServletContext delegate) {
		this.delegate = delegate;
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
	public javax.servlet.ServletContext getContext(String arg0) {
		return new NewServletContextWrapper(delegate.getContext(arg0));
	}

	@Override
	public String getContextPath() {
		return delegate.getContextPath();
	}

	@Override
	public String getInitParameter(String arg0) {
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
	public String getMimeType(String arg0) {
		return delegate.getMimeType(arg0);
	}

	@Override
	public int getMinorVersion() {
		return delegate.getMinorVersion();
	}

	@Override
	public javax.servlet.RequestDispatcher getNamedDispatcher(String arg0) {
		RequestDispatcher result = delegate.getNamedDispatcher(arg0);
		return result == null ? null : new NewRequestDispatcherWrapper(result);
	}

	@Override
	public String getRealPath(String arg0) {
		return delegate.getRealPath(arg0);
	}

	@Override
	public javax.servlet.RequestDispatcher getRequestDispatcher(String arg0) {
		RequestDispatcher result = delegate.getRequestDispatcher(arg0);
		return result == null ? null : new NewRequestDispatcherWrapper(result);
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
	public Set getResourcePaths(String arg0) {
		return delegate.getResourcePaths(arg0);
	}

	@Override
	public String getServerInfo() {
		return delegate.getServerInfo();
	}

	@SuppressWarnings("deprecation")
	@Override
	public javax.servlet.Servlet getServlet(String arg0) throws javax.servlet.ServletException {
		Servlet result;
		try {
			result = delegate.getServlet(arg0);
		} catch (ServletException e) {
			throw new javax.servlet.ServletException(e);
		}
		return result == null ? null : new NewHttpServletWrapper(result);
	}

	@Override
	public String getServletContextName() {
		return delegate.getServletContextName();
	}

	@SuppressWarnings("deprecation")
	@Override
	public Enumeration getServletNames() {
		return delegate.getServletNames();
	}

	@SuppressWarnings("deprecation")
	@Override
	public Enumeration getServlets() {
		return Collections.enumeration(
			Collections.list(delegate.getServlets())
				.stream()
				.map(NewHttpServletWrapper::new)
				.collect(Collectors.toList())
		);
	}

	@Override
	public void log(String arg0) {
		delegate.log(arg0);
	}

	@SuppressWarnings("deprecation")
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
}
