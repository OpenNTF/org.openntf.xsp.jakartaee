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
import java.util.List;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpSessionAttributeListener;
import jakarta.servlet.http.HttpSessionBindingEvent;

class OldHttpSessionWrapper implements HttpSession {
	final javax.servlet.http.HttpSession delegate;

	public OldHttpSessionWrapper(final javax.servlet.http.HttpSession delegate) {
		this.delegate = delegate;
	}

	void addListener(final HttpSessionAttributeListener listener) {
		this.getAttrListeners().add(listener);
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
		return ServletUtil.oldToNew(null, delegate.getServletContext());
	}

	@Override
	public void setMaxInactiveInterval(final int interval) {
		delegate.setMaxInactiveInterval(interval);
	}

	@Override
	public int getMaxInactiveInterval() {
		return delegate.getMaxInactiveInterval();
	}

	@Override
	public Object getAttribute(final String name) {
		return delegate.getAttribute(name);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Enumeration<String> getAttributeNames() {
		return delegate.getAttributeNames();
	}

	@Override
	public void setAttribute(final String name, final Object value) {
		boolean exists = Collections.list(this.getAttributeNames()).contains(name);
		Object oldVal = delegate.getAttribute(name);
		delegate.setAttribute(name, value);
		if(exists) {
			this.getAttrListeners().forEach(listener ->
				listener.attributeReplaced(new HttpSessionBindingEvent(this, name, oldVal))
			);
		}
		this.getAttrListeners().forEach(listener ->
			listener.attributeAdded(new HttpSessionBindingEvent( this, name, value))
		);
	}

	@Override
	public void removeAttribute(final String name) {
		Object val = delegate.getAttribute(name);
		delegate.removeAttribute(name);
		this.getAttrListeners().forEach(listener ->
			listener.attributeRemoved(new HttpSessionBindingEvent(this, name, val))
		);
	}

	@Override
	public void invalidate() {
		delegate.invalidate();
	}

	@Override
	public boolean isNew() {
		return delegate.isNew();
	}

	// *******************************************************************************
	// * Internal utility methods
	// *******************************************************************************

	private List<HttpSessionAttributeListener> getAttrListeners() {
		return ServletUtil.getListeners(getServletContext(), HttpSessionAttributeListener.class);
	}

}
