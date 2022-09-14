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
package org.openntf.xsp.jakarta.example.webapp;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Optional;

import org.openntf.xsp.jakartaee.module.ComponentModuleLocator;
import org.openntf.xsp.jakartaee.util.LibraryUtil;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@SuppressWarnings("nls")
public class LocatorTestServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		try(PrintWriter w = resp.getWriter()) {
			try {
				w.println("I am going to try to locate context information");
				
				w.println("Active locators:");
				LibraryUtil.findExtensionsSorted(ComponentModuleLocator.class, false)
					.stream()
					.filter(ComponentModuleLocator::isActive)
					.forEach(loc -> w.println(">> " + loc));
				w.println();
				
				ComponentModuleLocator loc = ComponentModuleLocator.getDefault().get();
				w.println("Module: " + loc.getActiveModule());
				
				Optional<HttpServletRequest> request = loc.getServletRequest();
				w.println("Request: " + request);
				if(request.isPresent()) {
					w.println("- Context path: " + request.get().getContextPath());
					w.println("- Servlet path: " + request.get().getServletPath());
					w.println("- URI: " + request.get().getRequestURI());
					w.println("- URL: " + request.get().getRequestURL());
				}
				
				Optional<ServletContext> context = loc.getServletContext();
				w.println("Context: " + context);
				if(context.isPresent()) {
					w.println("- Context path: " + context.get().getContextPath());
				}
				
			} catch(Exception e) {
				e.printStackTrace(w);
			}
		}
	}

}
