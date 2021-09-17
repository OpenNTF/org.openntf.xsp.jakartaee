package org.openntf.xsp.jakartaee.servlet;

import java.util.Enumeration;

import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpSessionContext;

@SuppressWarnings("deprecation")
public class NewHttpSessionContextWrapper implements javax.servlet.http.HttpSessionContext {
	private final HttpSessionContext delegate;
	
	public NewHttpSessionContextWrapper(HttpSessionContext delegate) {
		this.delegate = delegate;
	}

	@Override
	public javax.servlet.http.HttpSession getSession(String paramString) {
		HttpSession result = delegate.getSession(paramString);
		return result == null ? null : new NewHttpSessionWrapper(result);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Enumeration getIds() {
		return delegate.getIds();
	}
}
