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
package org.openntf.xsp.jakartaee;

import java.io.UncheckedIOException;
import java.util.Collection;
import java.util.Set;

import javax.servlet.Servlet;
import javax.servlet.ServletException;

import org.openntf.xsp.jakartaee.util.LibraryUtil;

import com.ibm.commons.util.StringUtil;
import com.ibm.designer.runtime.domino.adapter.ComponentModule;
import com.ibm.designer.runtime.domino.adapter.IServletFactory;
import com.ibm.designer.runtime.domino.adapter.ServletMatch;

import jakarta.servlet.ServletContext;

public abstract class MappingBasedServletFactory implements IServletFactory {
	private ComponentModule module;
	private Servlet servlet;
	private long lastUpdate;
	
	
	public MappingBasedServletFactory() {
	}
	
	@Override
	public void init(ComponentModule module) {
		this.module = module;
	}
	
	public ComponentModule getModule() {
		return module;
	}
	
	/**
	 * Retrieves a list of file extensions (e.g. ".jsp") supported by this factory
	 * 
	 * @return a {@link Collection} of supported extensions
	 */
	public abstract Set<String> getExtensions();
	
	/**
	 * Retrieves the name of the XSP library that must be present in the application
	 * in order for this factory to be active.
	 * 
	 * @return a library name, or {@code null} if it should always be active
	 */
	public abstract String getLibraryId();
	
	/**
	 * Method to create the executing Servlet, called when the module is new or has
	 * been modified.
	 * 
	 * @param module the active module to contain the Servlet
	 * @return a {@link Servlet} to handle requests
	 */
	public abstract Servlet createExecutorServlet(ComponentModule module) throws ServletException;
	
	/**
	 * Retrieves the name of the Servlet class created by this factory.
	 * 
	 * <p>This method is used by {@link ServletContext#getServletRegistrations()} and does
	 * not have to match the actual implementation Servlet.</p>
	 * 
	 * @return a string representing a servlet type
	 */
	public abstract String getServletClassName();
	
	@Override
	public final ServletMatch getServletMatch(String contextPath, String path) throws ServletException {
		try {
			String lib = getLibraryId();
			if(StringUtil.isEmpty(lib) || LibraryUtil.usesLibrary(lib, module)) {
				for(String ext : getExtensions()) {
					int extIndex = StringUtil.toString(path).indexOf(ext);
					if (extIndex > -1) {
						String servletPath = path.substring(0, extIndex+ext.length());
						String pathInfo = path.substring(extIndex+ext.length());
						return new ServletMatch(getExecutorServlet(), servletPath, pathInfo);
					}
				}
			}
		} catch (UncheckedIOException e) {
			throw new ServletException(e);
		}
		return null;
	}
	
	public final Servlet getExecutorServlet() throws ServletException {
		if (servlet == null || lastUpdate < this.module.getLastRefresh()) {
			this.servlet = createExecutorServlet(this.module);
			lastUpdate = this.module.getLastRefresh();
		}
		return servlet;
	}
}
