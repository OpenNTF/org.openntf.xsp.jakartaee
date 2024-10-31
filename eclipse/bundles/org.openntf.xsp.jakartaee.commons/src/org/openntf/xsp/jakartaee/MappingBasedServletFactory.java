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
package org.openntf.xsp.jakartaee;

import java.io.UncheckedIOException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.Servlet;
import javax.servlet.ServletException;

import com.ibm.commons.util.StringUtil;
import com.ibm.designer.runtime.domino.adapter.ComponentModule;
import com.ibm.designer.runtime.domino.adapter.IServletFactory;
import com.ibm.designer.runtime.domino.adapter.ServletMatch;

import org.openntf.xsp.jakartaee.util.LibraryUtil;
import org.openntf.xsp.jakartaee.util.ModuleUtil;

import jakarta.servlet.ServletContext;

public abstract class MappingBasedServletFactory implements IServletFactory {
	private static final Map<String, Map<String, Servlet>> MODULE_SERVLETS = new ConcurrentHashMap<>();
	private ComponentModule module;
	private long lastUpdate;
	private Map<String, String> explicitEndpoints = new ConcurrentHashMap<>();

	public MappingBasedServletFactory() {
	}

	@Override
	public void init(final ComponentModule module) {
		this.module = module;
	}

	public ComponentModule getModule() {
		return module;
	}

	/**
	 * Adds an additional endpoint that should be mapped to this
	 * factory, beyond the normal mapping based on file extension.
	 *
	 * @param endpoint the endpoint to add, e.g. {@code "/xsp/foo"}
	 * @param pathName the translated path to the actual file, e.g.
	 *        {@code "/foo.xhtml"}
	 * @since 3.0.0
	 */
	public void addExplicitEndpoint(final String endpoint, final String pathName) {
		if(StringUtil.isNotEmpty(endpoint)) {
			explicitEndpoints.put(endpoint, pathName);
		}
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
	public final ServletMatch getServletMatch(final String contextPath, final String path) throws ServletException {
		try {
			String lib = getLibraryId();
			if(StringUtil.isEmpty(lib) || LibraryUtil.usesLibrary(lib, module)) {
				for(Map.Entry<String, String> mapping : this.explicitEndpoints.entrySet()) {
					if(path.equals(mapping.getKey()) || path.startsWith(mapping.getKey()+'/')) {
						String servletPath = mapping.getValue();
						String pathInfo = path.substring(mapping.getKey().length());
						return new ServletMatch(getExecutorServlet(), servletPath, pathInfo);
					}
				}

				for(String ext : getExtensions()) {
					int extIndex = StringUtil.toString(path).indexOf(ext);
					if (extIndex > -1) {
						String servletPath = path.substring(0, extIndex+ext.length());
						String pathInfo = path.substring(extIndex+ext.length());
						if(checkExists(servletPath, pathInfo)) {
							return new ServletMatch(getExecutorServlet(), servletPath, pathInfo);
						}
					}
				}
			}
		} catch (UncheckedIOException e) {
			throw new ServletException(e);
		}
		return null;
	}

	/**
	 * This method checks to ensure that a match potentially identified by
	 * {@link #getServletMatch(String, String)} actually exists. The default behavior
	 * is to immediately return {@code true}, but implementation classes can override
	 * this when applicable.
	 *
	 * @param servletPath the path to the servlet matched, e.g. {@code "/foo.bar"}
	 * @param pathInfo any information following the matched path
	 * @return {@code true} if the servlet exists; {@code false} otherwise
	 */
	protected boolean checkExists(final String servletPath, final String pathInfo) {
		return true;
	}

	public final Servlet getExecutorServlet() throws ServletException {
		Servlet servlet = getServlet();
		if (servlet == null || lastUpdate < this.module.getLastRefresh()) {
			if(servlet != null) {
				servlet.destroy();
			}
			servlet = createExecutorServlet(this.module);
			setServlet(servlet);
			lastUpdate = this.module.getLastRefresh();
		}
		return servlet;
	}

	protected Servlet getServlet() {
		String id = ModuleUtil.getModuleId(this.module);
		return MODULE_SERVLETS.computeIfAbsent(id, key -> new ConcurrentHashMap<>())
			.get(getClass().getName());
	}
	protected void setServlet(final Servlet servlet) {
		String id = ModuleUtil.getModuleId(this.module);
		MODULE_SERVLETS.computeIfAbsent(id, key -> new ConcurrentHashMap<>())
			.put(getClass().getName(), servlet);
	}
}
