/**
 * Copyright © 2018-2022 Contributors to the XPages Jakarta EE Support Project
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

class NewCookieWrapper extends javax.servlet.http.Cookie implements Cloneable {
	final Cookie delegate;
	
	public NewCookieWrapper(Cookie delegate) {
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
		return delegate.isHttpOnly();
	}

	public void setComment(String purpose) {
		delegate.setComment(purpose);
	}

	public void setDomain(String domain) {
		delegate.setDomain(domain);
	}

	public void setHttpOnly(boolean isHttpOnly) {
		delegate.setHttpOnly(isHttpOnly);
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
		return new NewCookieWrapper((Cookie)delegate.clone());
	}
	

}
