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
package org.openntf.xsp.jsp.nsf;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.Servlet;
import javax.servlet.ServletException;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.osgi.util.ManifestElement;
import org.openntf.xsp.jakartaee.MappingBasedServletFactory;
import org.openntf.xsp.jakartaee.servlet.ServletUtil;
import org.openntf.xsp.jakartaee.util.LibraryUtil;
import org.openntf.xsp.jsp.JspLibrary;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.osgi.framework.FrameworkUtil;

import com.ibm.commons.util.StringUtil;
import com.ibm.designer.runtime.domino.adapter.ComponentModule;
import com.ibm.xsp.extlib.util.ExtLibUtil;

/**
 * 
 * @author Jesse Gallagher
 * @since 2.1.0
 */
public class JspServletFactory extends MappingBasedServletFactory {
	private static final String PATH_SEP = AccessController.doPrivileged((PrivilegedAction<String>)() -> System.getProperty("path.separator")); //$NON-NLS-1$

	public JspServletFactory() {
	}
	
	@Override
	public Set<String> getExtensions() {
		return new HashSet<>(Arrays.asList(".jsp", ".jspx")); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	@Override
	public String getLibraryId() {
		return JspLibrary.LIBRARY_ID;
	}
	
	@Override
	public String getServletClassName() {
		return NSFJspServlet.class.getName();
	}
	
	@Override
	public Servlet createExecutorServlet(ComponentModule module) throws ServletException {
		try {
			return AccessController.doPrivileged((PrivilegedExceptionAction<Servlet>)() -> {
				Map<String, String> params = new HashMap<>();
				String classpath = buildBundleClassPath()
					.stream()
					.map(File::toString)
					.collect(Collectors.joining(PATH_SEP));
				params.put("classpath", classpath); //$NON-NLS-1$
				params.put("development", Boolean.toString(ExtLibUtil.isDevelopmentMode())); //$NON-NLS-1$

				Path tempDir = LibraryUtil.getTempDirectory();
				tempDir = tempDir.resolve(getClass().getName());
				String moduleName = module.getModuleName();
				if(StringUtil.isEmpty(moduleName)) {
					moduleName = Integer.toString(System.identityHashCode(module));
				}
				tempDir = tempDir.resolve(moduleName);
				Files.createDirectories(tempDir);
				params.put("scratchdir", tempDir.toString()); //$NON-NLS-1$
				
				return module.createServlet(ServletUtil.newToOld((jakarta.servlet.Servlet)new NSFJspServlet(module)), "XSP JSP Servlet", params); //$NON-NLS-1$
			});
		} catch (PrivilegedActionException e) {
			throw new ServletException(e.getCause());
		}
	}

	public static List<File> buildBundleClassPath() throws BundleException, IOException {
		Bundle bundle = FrameworkUtil.getBundle(JspServletFactory.class);
		List<File> classpath = new ArrayList<>();
		toClasspathEntry(bundle, classpath);
		
		return classpath;
	}
	
	private static void toClasspathEntry(Bundle bundle, List<File> classpath) throws BundleException, IOException {
		// These entries MUST be filesystem paths
		classpath.add(FileLocator.getBundleFile(bundle));
		
		String req = bundle.getHeaders().get("Require-Bundle"); //$NON-NLS-1$
		if(StringUtil.isNotEmpty(req)) {
			ManifestElement[] elements = ManifestElement.parseHeader("Require-Bundle", req); //$NON-NLS-1$
			for(ManifestElement element : elements) {
				String visibility = element.getDirective("visibility"); //$NON-NLS-1$
				if("reexport".equals(visibility)) { //$NON-NLS-1$
					Optional<Bundle> dep = LibraryUtil.getBundle(element.getValue());
					if(dep.isPresent()) {
						toClasspathEntry(dep.get(), classpath);
					}
				}
			}
		}
	}
}
