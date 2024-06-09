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
package org.openntf.xsp.jakarta.pages.nsf;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.Servlet;
import javax.servlet.ServletException;

import org.openntf.xsp.jakarta.pages.util.DominoPagesUtil;
import org.openntf.xsp.jakartaee.MappingBasedServletFactory;
import org.openntf.xsp.jakartaee.servlet.ServletUtil;
import org.openntf.xsp.jakartaee.util.LibraryUtil;

import com.ibm.commons.util.StringUtil;
import com.ibm.designer.runtime.domino.adapter.ComponentModule;
import com.ibm.xsp.extlib.util.ExtLibUtil;

/**
 * 
 * @author Jesse Gallagher
 * @since 2.1.0
 */
public class PagesServletFactory extends MappingBasedServletFactory {
	public PagesServletFactory() {
	}
	
	@Override
	public Set<String> getExtensions() {
		return new HashSet<>(Arrays.asList(".jsp", ".jspx")); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	@Override
	public String getLibraryId() {
		return LibraryUtil.LIBRARY_UI;
	}
	
	@Override
	public String getServletClassName() {
		return NSFPagesServlet.class.getName();
	}
	
	@Override
	protected boolean checkExists(String servletPath, String pathInfo) {
		ComponentModule module = getModule();
		return module.getResourceAsStream(servletPath) != null;
	}
	
	@SuppressWarnings({ "removal", "deprecation" })
	@Override
	public Servlet createExecutorServlet(ComponentModule module) throws ServletException {
		try {
			return AccessController.doPrivileged((PrivilegedExceptionAction<Servlet>)() -> {
				Map<String, String> params = new HashMap<>();
				String classpath = DominoPagesUtil.buildBundleClassPath()
					.stream()
					.map(File::toString)
					.collect(Collectors.joining(DominoPagesUtil.PATH_SEP));
				params.put("classpath", classpath); //$NON-NLS-1$
				params.put("development", Boolean.toString(ExtLibUtil.isDevelopmentMode())); //$NON-NLS-1$
				params.put("compilerSourceVM", "17"); //$NON-NLS-1$ //$NON-NLS-2$
				params.put("compilerTargetVM", "17"); //$NON-NLS-1$ //$NON-NLS-2$

				Path tempDir = LibraryUtil.getTempDirectory();
				tempDir = tempDir.resolve(getClass().getName());
				String moduleName = module.getModuleName();
				if(StringUtil.isEmpty(moduleName)) {
					moduleName = Integer.toString(System.identityHashCode(module));
				}
				tempDir = tempDir.resolve(moduleName);
				Files.createDirectories(tempDir);
				params.put("scratchdir", tempDir.toString()); //$NON-NLS-1$
				
				return module.createServlet(ServletUtil.newToOld((jakarta.servlet.Servlet)new NSFPagesServlet(module)), "XSP JSP Servlet", params); //$NON-NLS-1$
			});
		} catch (PrivilegedActionException e) {
			throw new ServletException(e.getCause());
		}
	}
}
