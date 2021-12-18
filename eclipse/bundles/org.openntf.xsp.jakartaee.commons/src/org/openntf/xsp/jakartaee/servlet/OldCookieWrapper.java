package org.openntf.xsp.jakartaee.servlet;

import jakarta.servlet.http.Cookie;

class OldCookieWrapper extends Cookie implements Cloneable {
	private static final long serialVersionUID = 1L;
	
	final javax.servlet.http.Cookie delegate;
	private boolean httpOnly;
	
	public OldCookieWrapper(javax.servlet.http.Cookie delegate) {
		super(delegate.getName(), delegate.getValue());
		this.delegate = delegate;
	}

	public String getComment() {
		return delegate.getComment();
	}

	public String getDomain() {
		return delegate.getDomain();
	}

	public int getMaxAge() {
		return delegate.getMaxAge();
	}

	public String getName() {
		return delegate.getName();
	}

	public String getPath() {
		return delegate.getPath();
	}

	public boolean getSecure() {
		return delegate.getSecure();
	}

	public String getValue() {
		return delegate.getValue();
	}

	public int getVersion() {
		return delegate.getVersion();
	}

	public boolean isHttpOnly() {
		// TODO consider looking this up in the props if possible
		return httpOnly;
	}

	public void setComment(String purpose) {
		delegate.setComment(purpose);
	}

	public void setDomain(String domain) {
		delegate.setDomain(domain);
	}

	public void setHttpOnly(boolean isHttpOnly) {
		// TODO consider investigating how this can be set
		this.httpOnly = isHttpOnly;
	}

	public void setMaxAge(int expiry) {
		delegate.setMaxAge(expiry);
	}

	public void setPath(String uri) {
		delegate.setPath(uri);
	}

	public void setSecure(boolean flag) {
		delegate.setSecure(flag);
	}

	public void setValue(String newValue) {
		delegate.setValue(newValue);
	}

	public void setVersion(int v) {
		delegate.setVersion(v);
	}

	public String toString() {
		return delegate.toString();
	}
	
	@Override
	public int hashCode() {
		return delegate.hashCode() + getClass().hashCode();
	}
	
	@Override
	public Object clone() {
		return new OldCookieWrapper((javax.servlet.http.Cookie)delegate.clone());
	}
	

}
