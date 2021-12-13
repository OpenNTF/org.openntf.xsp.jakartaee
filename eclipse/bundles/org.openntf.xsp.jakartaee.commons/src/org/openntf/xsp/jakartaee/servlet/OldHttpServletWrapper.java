/**
 * Copyright © 2018-2021 Jesse Gallagher
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

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class OldHttpServletWrapper extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private final javax.servlet.Servlet delegate;
	
	public OldHttpServletWrapper(javax.servlet.Servlet delegate) {
		this.delegate = delegate;
	}

	@Override
	public void init(ServletConfig config) throws ServletException {
		try {
			delegate.init(new NewServletConfigWrapper(config));
		} catch (javax.servlet.ServletException e) {
			throw new ServletException(e);
		}
	}

	@Override
	public ServletConfig getServletConfig() {
		javax.servlet.ServletConfig result = delegate.getServletConfig();
		return ((NewServletConfigWrapper)result).getDelegate();
	}

	@Override
	public void service(ServletRequest request, ServletResponse response) throws ServletException, IOException {
		HttpServletRequest req = (HttpServletRequest)request;
		HttpServletResponse resp = (HttpServletResponse)response;
		try {
			delegate.service(new NewHttpServletRequestWrapper(req), new NewHttpServletResponseWrapper(resp));
		} catch (javax.servlet.ServletException e) {
			throw new ServletException(e);
		}
	}

	@Override
	public String getServletInfo() {
		return delegate.getServletInfo();
	}

	@Override
	public void destroy() {
		delegate.destroy();
	}

}
