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
package org.openntf.xsp.test.beanbundle.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.security.AccessController;
import java.security.PrivilegedAction;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jboss.resteasy.plugins.server.servlet.HttpServletDispatcher;
import org.openntf.xsp.jakartaee.servlet.ServletUtil;

import com.ibm.designer.runtime.domino.adapter.util.XSPErrorPage;

public class RootServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private final HttpServletDispatcher delegate = new HttpServletDispatcher();
	private ServletContext context;
	
	@Override
	public void init() throws ServletException {
		super.init();
		try {
			delegate.init();
		} catch (jakarta.servlet.ServletException e) {
			throw new ServletException(e);
		}
	}
	
	@Override
	public void init(ServletConfig servletConfig) throws ServletException {
		this.context = servletConfig.getServletContext();
		
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		AccessController.doPrivileged((PrivilegedAction<Void>) () -> {
			Thread.currentThread().setContextClassLoader(RootServlet.class.getClassLoader());
			return null;
		});
		try {
			delegate.init(ServletUtil.oldToNew(servletConfig));
		} catch (jakarta.servlet.ServletException e) {
			throw new ServletException(e);
		} finally {
			AccessController.doPrivileged((PrivilegedAction<Void>) () -> {
				Thread.currentThread().setContextClassLoader(cl);
				return null;
			});
		}
	}
	
	@Override
	public void service(final HttpServletRequest request, final HttpServletResponse response)
			throws ServletException, IOException {
		jakarta.servlet.http.HttpServletRequest newReq = ServletUtil.oldToNew(this.context, request);

		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		try {
			AccessController.doPrivileged((PrivilegedAction<Void>) () -> {
				Thread.currentThread().setContextClassLoader(RootServlet.class.getClassLoader());

				return null;
			});

			delegate.service(newReq, ServletUtil.oldToNew(response));
		} catch(Throwable t) {
			try(PrintWriter w = response.getWriter()) {
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				XSPErrorPage.handleException(w, t, request.getRequestURL().toString(), false);
			} catch (javax.servlet.ServletException e) {
				throw new IOException(e);
			} catch(IllegalStateException e) {
				// Happens when the writer or output has already been opened
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			}
		} finally {
			AccessController.doPrivileged((PrivilegedAction<Void>) () -> {
				Thread.currentThread().setContextClassLoader(cl);
				return null;
			});
		}
	}
}
