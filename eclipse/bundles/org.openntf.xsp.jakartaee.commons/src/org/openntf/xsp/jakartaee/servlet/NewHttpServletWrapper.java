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

import java.io.IOException;
import java.util.Enumeration;

import jakarta.servlet.Servlet;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;

class NewHttpServletWrapper extends javax.servlet.http.HttpServlet {
	private static final long serialVersionUID = 1L;

	final HttpServlet delegate;

	public NewHttpServletWrapper(final Servlet delegate) {
		if(!(delegate instanceof HttpServlet)) {
			throw new IllegalArgumentException("Unsupported delegate: " + delegate);
		}
		this.delegate = (HttpServlet)delegate;
	}

	@Override
	public void destroy() {
		delegate.destroy();
	}

	@Override
	public javax.servlet.ServletConfig getServletConfig() {
		return ServletUtil.newToOld(delegate.getServletConfig());
	}

	@Override
	public String getServletInfo() {
		return delegate.getServletInfo();
	}

	@Override
	public void init(final javax.servlet.ServletConfig arg0) throws javax.servlet.ServletException {
		try {
			delegate.init(ServletUtil.oldToNew(arg0));
		} catch (ServletException e) {
			throw new javax.servlet.ServletException(e);
		}
	}

	@Override
	public String getInitParameter(final String name) {
		return delegate.getInitParameter(name);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Enumeration getInitParameterNames() {
		return delegate.getInitParameterNames();
	}

	@Override
	public javax.servlet.ServletContext getServletContext() {
		return ServletUtil.newToOld(delegate.getServletContext());
	}

	@Override
	public void log(final String msg) {
		delegate.log(msg);
	}

	@Override
	public String getServletName() {
		return delegate.getServletName();
	}

	@Override
	public void init() throws javax.servlet.ServletException {
		try {
			delegate.init();
		} catch (ServletException e) {
			throw new javax.servlet.ServletException(e);
		}
	}

	@Override
	public void log(final String message, final Throwable t) {
		delegate.log(message, t);
	}

	@Override
	public void service(final javax.servlet.ServletRequest arg0, final javax.servlet.ServletResponse arg1) throws javax.servlet.ServletException, IOException {
		javax.servlet.http.HttpServletRequest req = (javax.servlet.http.HttpServletRequest)arg0;
		javax.servlet.http.HttpServletResponse resp = (javax.servlet.http.HttpServletResponse)arg1;

		ServletConfig config = delegate.getServletConfig();
		ServletContext context = config == null ? null : config.getServletContext();
		javax.servlet.ServletContext oldContext = ServletUtil.newToOld(context);
		try {
			delegate.service(ServletUtil.oldToNew(oldContext, req), ServletUtil.oldToNew(resp));
		} catch (ServletException e) {
			throw new javax.servlet.ServletException(e);
		}
	}
}
