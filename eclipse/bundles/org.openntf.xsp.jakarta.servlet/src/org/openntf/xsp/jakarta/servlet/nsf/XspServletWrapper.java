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
package org.openntf.xsp.jakarta.servlet.nsf;

import java.io.IOException;
import java.util.Enumeration;

import com.ibm.designer.runtime.domino.adapter.ComponentModule;
import com.ibm.xsp.application.ApplicationEx;

import org.openntf.xsp.jakarta.cdi.bean.HttpContextBean;
import org.openntf.xsp.jakartaee.AbstractXspLifecycleServlet;

import jakarta.servlet.Servlet;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Wraps a provided {@link Servlet} implementation with behavior to participate
 * in the XPages request lifecycle.
 *
 * @author Jesse Gallagher
 * @since 2.5.0
 */
public class XspServletWrapper extends AbstractXspLifecycleServlet {
	private static final long serialVersionUID = 1L;

	private final HttpServlet delegate;

	public XspServletWrapper(final ComponentModule module, final Servlet delegate) {
		super(module);
		this.delegate = (HttpServlet)delegate;
	}

	@Override
	public void init() throws ServletException {
		super.init();
		delegate.init();
	}

	@Override
	protected void doInit(final ServletConfig config, final HttpServletRequest request) throws ServletException {
		delegate.init(config);
	}

	@Override
	protected void doService(final HttpServletRequest request, final HttpServletResponse response, final ApplicationEx application)
			throws ServletException, IOException {
		HttpContextBean.setThreadResponse(response);
		try {
			delegate.service(request, response);
		} finally {
			HttpContextBean.setThreadResponse(null);
		}
	}

	@Override
	public void destroy() {
		super.destroy();
		delegate.destroy();
	}

	@Override
	public String getInitParameter(final String name) {
		return delegate.getInitParameter(name);
	}

	@Override
	public Enumeration<String> getInitParameterNames() {
		return delegate.getInitParameterNames();
	}

	@Override
	public String getServletInfo() {
		return delegate.getServletInfo();
	}

	@Override
	public void log(final String msg) {
		delegate.log(msg);
	}

	@Override
	public void log(final String message, final Throwable t) {
		delegate.log(message, t);
	}

	@Override
	public String getServletName() {
		return delegate.getServletName();
	}

}
