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
package org.openntf.xsp.test.beanbundle.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.security.AccessController;
import java.security.PrivilegedAction;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ibm.designer.runtime.domino.adapter.util.XSPErrorPage;

import org.jboss.resteasy.plugins.server.servlet.HttpServletDispatcher;
import org.openntf.xsp.jakartaee.servlet.ServletUtil;

public class RootServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private boolean initialized;

	private final HttpServletDispatcher delegate = new HttpServletDispatcher();
	
	private void initDelegate() throws ServletException {
		try {
			ClassLoader cl = Thread.currentThread().getContextClassLoader();
			AccessController.doPrivileged((PrivilegedAction<Void>) () -> {
				Thread.currentThread().setContextClassLoader(RootServlet.class.getClassLoader());
				return null;
			});
			try {
				delegate.init(ServletUtil.oldToNew(this.getServletConfig()));
			} finally {
				AccessController.doPrivileged((PrivilegedAction<Void>) () -> {
					Thread.currentThread().setContextClassLoader(cl);
					return null;
				});
			}
		} catch (jakarta.servlet.ServletException e) {
			e.printStackTrace();
			throw new ServletException(e);
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		this.initialized = true;
	}
	
	@Override
	public void service(final HttpServletRequest request, final HttpServletResponse response)
			throws ServletException, IOException {
		jakarta.servlet.http.HttpServletRequest newReq = ServletUtil.oldToNew(this.getServletContext(), request);

		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		try {
			// Delay initialization so that we have an NSF context for the CDI container
			if(!initialized) {
				initDelegate();
			}
			
			AccessController.doPrivileged((PrivilegedAction<Void>) () -> {
				Thread.currentThread().setContextClassLoader(RootServlet.class.getClassLoader());

				return null;
			});

			delegate.service(newReq, ServletUtil.oldToNew(response));
		} catch(Throwable t) {
			t.printStackTrace();
			try(PrintWriter w = response.getWriter()) {
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				XSPErrorPage.handleException(w, t, null, false);
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
