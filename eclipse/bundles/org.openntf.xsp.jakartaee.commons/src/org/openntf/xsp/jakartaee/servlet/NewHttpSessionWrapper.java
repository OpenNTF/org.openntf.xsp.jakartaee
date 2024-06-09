/**
 * Copyright (c) 2018-2024 Contributors to the XPages Jakarta EE Support Project
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

import java.util.Collections;
import java.util.Enumeration;

import jakarta.servlet.http.HttpSession;

@SuppressWarnings({ "rawtypes", "deprecation" })
class NewHttpSessionWrapper implements javax.servlet.http.HttpSession {
	final HttpSession delegate;
	
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
		return ServletUtil.newToOld(delegate.getServletContext());
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
		// Removed in Servlet 6
		return null;
	}

	@Override
	public Object getAttribute(String paramString) {
		return delegate.getAttribute(paramString);
	}

	@Override
	public Object getValue(String paramString) {
		// Removed in Servlet 6
		return delegate.getAttribute(paramString);
	}

	@Override
	public Enumeration getAttributeNames() {
		return delegate.getAttributeNames();
	}

	@Override
	public String[] getValueNames() {
		// Removed in Servlet 6
		return Collections.list(delegate.getAttributeNames()).toArray(new String[0]);
	}

	@Override
	public void setAttribute(String paramString, Object paramObject) {
		delegate.setAttribute(paramString, paramObject);
	}

	@Override
	public void putValue(String paramString, Object paramObject) {
		// Removed in Servlet 6
		delegate.setAttribute(paramString, paramObject);
	}

	@Override
	public void removeAttribute(String paramString) {
		delegate.removeAttribute(paramString);
	}

	@Override
	public void removeValue(String paramString) {
		// Removed in Servlet 6
		delegate.removeAttribute(paramString);
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
