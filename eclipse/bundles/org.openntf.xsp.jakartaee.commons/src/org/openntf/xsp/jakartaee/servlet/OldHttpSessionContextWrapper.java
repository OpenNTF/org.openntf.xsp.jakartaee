package org.openntf.xsp.jakartaee.servlet;

import java.util.Enumeration;

import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpSessionContext;

@SuppressWarnings("deprecation")
public class OldHttpSessionContextWrapper implements HttpSessionContext {
	private final javax.servlet.http.HttpSessionContext delegate;
	
	public OldHttpSessionContextWrapper(javax.servlet.http.HttpSessionContext delegate) {
		this.delegate = delegate;
	}
	
	@Override
	public HttpSession getSession(String sessionId) {
		javax.servlet.http.HttpSession result = delegate.getSession(sessionId);
		return result == null ? null : new OldHttpSessionWrapper(result);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Enumeration<String> getIds() {
		return delegate.getIds();
	}

}
