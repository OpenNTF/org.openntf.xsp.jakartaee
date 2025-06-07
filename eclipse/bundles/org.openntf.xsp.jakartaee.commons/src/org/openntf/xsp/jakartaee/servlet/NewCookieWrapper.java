/**
 * Copyright (c) 2018-2025 Contributors to the XPages Jakarta EE Support Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openntf.xsp.jakartaee.servlet;

import jakarta.servlet.http.Cookie;

class NewCookieWrapper extends javax.servlet.http.Cookie {
	final Cookie delegate;

	public NewCookieWrapper(final Cookie delegate) {
		super(delegate.getName(), delegate.getValue());
		this.delegate = delegate;
	}

	@Override
	public String getComment() {
		return delegate.getComment();
	}

	@Override
	public String getDomain() {
		return delegate.getDomain();
	}

	@Override
	public int getMaxAge() {
		return delegate.getMaxAge();
	}

	@Override
	public String getName() {
		return delegate.getName();
	}

	@Override
	public String getPath() {
		return delegate.getPath();
	}

	@Override
	public boolean getSecure() {
		return delegate.getSecure();
	}

	@Override
	public String getValue() {
		return delegate.getValue();
	}

	@Override
	public int getVersion() {
		return delegate.getVersion();
	}

	public boolean isHttpOnly() {
		return delegate.isHttpOnly();
	}

	@Override
	public void setComment(final String purpose) {
		delegate.setComment(purpose);
	}

	@Override
	public void setDomain(final String domain) {
		delegate.setDomain(domain);
	}

	public void setHttpOnly(final boolean isHttpOnly) {
		delegate.setHttpOnly(isHttpOnly);
	}

	@Override
	public void setMaxAge(final int expiry) {
		delegate.setMaxAge(expiry);
	}

	@Override
	public void setPath(final String uri) {
		delegate.setPath(uri);
	}

	@Override
	public void setSecure(final boolean flag) {
		delegate.setSecure(flag);
	}

	@Override
	public void setValue(final String newValue) {
		delegate.setValue(newValue);
	}

	@Override
	public void setVersion(final int v) {
		delegate.setVersion(v);
	}

	@Override
	public String toString() {
		return delegate.toString();
	}

	@Override
	public int hashCode() {
		return delegate.hashCode() + getClass().hashCode();
	}

	@Override
	public Object clone() {
		return new NewCookieWrapper((Cookie)delegate.clone());
	}


}
