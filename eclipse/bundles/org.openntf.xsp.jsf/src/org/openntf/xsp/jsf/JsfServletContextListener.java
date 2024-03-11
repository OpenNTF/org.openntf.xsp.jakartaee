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
package org.openntf.xsp.jsf;

import org.apache.myfaces.webapp.StartupServletContextListener;
import org.openntf.xsp.jakartaee.util.LibraryUtil;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;

/**
 * This wrapper around {@link StartupServletContextListener} checks to ensure
 * that the current application opts in to JSF.
 * 
 * @author Jesse Gallagher
 * @since 2.10.0
 */
public class JsfServletContextListener implements ServletContextListener {
	private final StartupServletContextListener delegate = new StartupServletContextListener();
	
	@Override
	public void contextInitialized(ServletContextEvent sce) {
		if(LibraryUtil.isLibraryActive(JsfLibrary.LIBRARY_ID)) {
			delegate.contextInitialized(sce);
		}
	}
	
	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		if(LibraryUtil.isLibraryActive(JsfLibrary.LIBRARY_ID)) {
			delegate.contextDestroyed(sce);
		}
	}
}
