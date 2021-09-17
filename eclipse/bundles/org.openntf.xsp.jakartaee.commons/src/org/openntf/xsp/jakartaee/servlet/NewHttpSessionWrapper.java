package org.openntf.xsp.jakartaee.servlet;

import java.util.Enumeration;

import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpSessionContext;

@SuppressWarnings({ "rawtypes", "deprecation" })
public class NewHttpSessionWrapper implements javax.servlet.http.HttpSession {
	private final HttpSession delegate;
	
	public NewHttpSessionWrapper(HttpSession delegate) {
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
	public javax.servlet.ServletContext getServletContext() {
		return new NewServletContextWrapper(delegate.getServletContext());
	}

	@Override
	public void setMaxInactiveInterval(int paramInt) {
		delegate.setMaxInactiveInterval(paramInt);
	}

	@Override
	public int getMaxInactiveInterval() {
		return delegate.getMaxInactiveInterval();
	}

	@Override
	public javax.servlet.http.HttpSessionContext getSessionContext() {
		HttpSessionContext result = delegate.getSessionContext();
		return result == null ? null : new NewHttpSessionContextWrapper(result);
	}

	@Override
	public Object getAttribute(String paramString) {
		return delegate.getAttribute(paramString);
	}

	@Override
	public Object getValue(String paramString) {
		return delegate.getValue(paramString);
	}

	@Override
	public Enumeration getAttributeNames() {
		return delegate.getAttributeNames();
	}

	@Override
	public String[] getValueNames() {
		return delegate.getValueNames();
	}

	@Override
	public void setAttribute(String paramString, Object paramObject) {
		delegate.setAttribute(paramString, paramObject);
	}

	@Override
	public void putValue(String paramString, Object paramObject) {
		delegate.putValue(paramString, paramObject);
	}

	@Override
	public void removeAttribute(String paramString) {
		delegate.removeAttribute(paramString);
	}

	@Override
	public void removeValue(String paramString) {
		delegate.removeValue(paramString);
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
