/**
 * Copyright © 2018-2022 Jesse Gallagher
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

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

class OldHttpServletWrapper extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	final javax.servlet.http.HttpServlet delegate;
	
	public OldHttpServletWrapper(javax.servlet.Servlet delegate) {
		if(!(delegate instanceof javax.servlet.http.HttpServlet)) {
			throw new IllegalArgumentException("Unsupported delegate: " + delegate);
		}
		this.delegate = (javax.servlet.http.HttpServlet)delegate;
	}

	@Override
	public void init(ServletConfig config) throws ServletException {
		try {
			delegate.init(ServletUtil.newToOld(config));
		} catch (javax.servlet.ServletException e) {
			throw new ServletException(e);
		}
	}

	@Override
	public ServletConfig getServletConfig() {
		return ServletUtil.oldToNew(delegate.getServletConfig());
	}

	@Override
	public void service(ServletRequest request, ServletResponse response) throws ServletException, IOException {
		HttpServletRequest req = (HttpServletRequest)request;
		HttpServletResponse resp = (HttpServletResponse)response;
		try {
			delegate.service(ServletUtil.newToOld(req), ServletUtil.newToOld(resp));
		} catch (javax.servlet.ServletException e) {
			throw new ServletException(e);
		}
	}

	@Override
	public String getServletInfo() {
		return delegate.getServletInfo();
	}

	@Override
	public String getInitParameter(String name) {
		return delegate.getInitParameter(name);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Enumeration getInitParameterNames() {
		return delegate.getInitParameterNames();
	}

	@Override
	public ServletContext getServletContext() {
		return ServletUtil.oldToNew(null, delegate.getServletContext());
	}

	@Override
	public void log(String msg) {
		delegate.log(msg);
	}

	@Override
	public String getServletName() {
		return delegate.getServletName();
	}

	@Override
	public void init() throws ServletException {
		try {
			delegate.init();
		} catch (javax.servlet.ServletException e) {
			throw new ServletException(e);
		}
	}

	@Override
	public void log(String message, Throwable t) {
		delegate.log(message, t);
	}

	@Override
	public void destroy() {
		delegate.destroy();
	}

}
