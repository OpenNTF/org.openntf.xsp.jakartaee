package org.openntf.xsp.jakartaee.servlet;

import java.util.Enumeration;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;

public class OldServletConfigWrapper implements ServletConfig {
	private final javax.servlet.ServletConfig delegate;
	
	public OldServletConfigWrapper(javax.servlet.ServletConfig delegate) {
		this.delegate = delegate;
	}
	
	@Override
	public String getServletName() {
		return delegate.getServletName();
	}

	@Override
	public ServletContext getServletContext() {
		javax.servlet.ServletContext result = delegate.getServletContext();
		return result == null ? null : new OldServletContextWrapper(result);
	}

	@Override
	public String getInitParameter(String name) {
		return delegate.getInitParameter(name);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Enumeration<String> getInitParameterNames() {
		return delegate.getInitParameterNames();
	}

}
