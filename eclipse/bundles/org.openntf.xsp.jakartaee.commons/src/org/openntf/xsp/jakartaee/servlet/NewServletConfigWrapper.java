package org.openntf.xsp.jakartaee.servlet;

import java.util.Enumeration;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;

@SuppressWarnings("rawtypes")
public class NewServletConfigWrapper implements javax.servlet.ServletConfig {
	private final ServletConfig delegate;
	
	public NewServletConfigWrapper(ServletConfig delegate) {
		this.delegate = delegate;
	}
	
	public ServletConfig getDelegate() {
		return delegate;
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
	public javax.servlet.ServletContext getServletContext() {
		ServletContext result = delegate.getServletContext();
		return result == null ? null : new NewServletContextWrapper(result);
	}

	@Override
	public String getServletName() {
		return delegate.getServletName();
	}

}
