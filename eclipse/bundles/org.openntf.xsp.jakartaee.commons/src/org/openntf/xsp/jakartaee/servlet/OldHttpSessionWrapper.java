package org.openntf.xsp.jakartaee.servlet;

import java.util.Enumeration;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpSessionContext;

@SuppressWarnings("deprecation")
public class OldHttpSessionWrapper implements HttpSession {
	private final javax.servlet.http.HttpSession delegate;
	
	public OldHttpSessionWrapper(javax.servlet.http.HttpSession delegate) {
		this.delegate = delegate;
	}
	
	@Override
	public long getCreationTime() {
		return delegate.getCreationTime();
	}

	@Override
	public String getId() {
		return delegate.getId();
	}

	@Override
	public long getLastAccessedTime() {
		return delegate.getLastAccessedTime();
	}

	@Override
	public ServletContext getServletContext() {
		javax.servlet.ServletContext result = delegate.getServletContext();
		return result == null ? null : new OldServletContextWrapper(result);
	}

	@Override
	public void setMaxInactiveInterval(int interval) {
		delegate.setMaxInactiveInterval(interval);
	}

	@Override
	public int getMaxInactiveInterval() {
		return delegate.getMaxInactiveInterval();
	}

	@Override
	public HttpSessionContext getSessionContext() {
		return new OldHttpSessionContextWrapper(delegate.getSessionContext());
	}

	@Override
	public Object getAttribute(String name) {
		return delegate.getAttribute(name);
	}

	@Override
	public Object getValue(String name) {
		return delegate.getValue(name);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Enumeration<String> getAttributeNames() {
		return delegate.getAttributeNames();
	}

	@Override
	public String[] getValueNames() {
		return delegate.getValueNames();
	}

	@Override
	public void setAttribute(String name, Object value) {
		delegate.setAttribute(name, value);
	}

	@Override
	public void putValue(String name, Object value) {
		delegate.putValue(name, value);
	}

	@Override
	public void removeAttribute(String name) {
		delegate.removeAttribute(name);
	}

	@Override
	public void removeValue(String name) {
		delegate.removeValue(name);
	}

	@Override
	public void invalidate() {
		delegate.invalidate();
	}

	@Override
	public boolean isNew() {
		return delegate.isNew();
	}

}
