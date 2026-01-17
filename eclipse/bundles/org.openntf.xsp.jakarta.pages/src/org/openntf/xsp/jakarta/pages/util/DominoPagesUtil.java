/**
 * Copyright (c) 2018-2026 Contributors to the XPages Jakarta EE Support Project
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
package org.openntf.xsp.jakarta.pages.util;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;

import com.ibm.commons.util.StringUtil;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.osgi.util.ManifestElement;
import org.openntf.xsp.jakarta.pages.PagesHttpInitListener;
import org.openntf.xsp.jakarta.pages.nsf.PagesServletFactory;
import org.openntf.xsp.jakartaee.util.LibraryUtil;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.osgi.framework.FrameworkUtil;

/**
 * Utility methods shared among distinct module-type implementations.
 *
 * @author Jesse Gallagher
 * @since 2.8.0
 */
public enum DominoPagesUtil {
	;

	public static final String PATH_SEP = LibraryUtil.getSystemProperty("path.separator"); //$NON-NLS-1$

	public static ClassLoader buildPagesClassLoader(final ClassLoader delegate) throws BundleException, IOException {
		// TODO support extension points?

		List<File> classpath = new ArrayList<>();
		// Add the Pages and Servlet bundles explicitly, so the Webapp bundle doesn't need them
		// Wasp reads it as a jar: URL to get the TLD files, but then loads resources via its ClassLoader
		classpath.addAll(buildBundleClassPath());

		URL[] path = classpath
			.stream()
			.map(File::toURI)
			// Signal to TldScanner that this is a JAR URL
			.map(uri -> "jar:" + uri + "!/") //$NON-NLS-1$ //$NON-NLS-2$
			.map(t -> {
				try {
					return URI.create(t).toURL();
				} catch (MalformedURLException e) {
					throw new UncheckedIOException(e);
				}
			})
			.toArray(URL[]::new);
		return new URLClassLoader(path, delegate);
	}

	// Must be a HashMap, as TldScanner casts it as such
	// It's a map of URI to [JAR file path, resource name]
	// See also TagLibraryInfoImpl
	public static HashMap<String, String[]> buildJstlDtdMap() throws IOException {
		String jstl = PagesHttpInitListener.getDeployedJstlBundle().toUri().toString();

		HashMap<String, String[]> result = new HashMap<>();

		result.put("http://java.sun.com/jsp/jstl/functions", new String[] { jstl, "META-INF/fn.tld" }); //$NON-NLS-1$ //$NON-NLS-2$
		result.put("http://java.sun.com/jsp/jstl/core", new String[] { jstl, "META-INF/c.tld" }); //$NON-NLS-1$ //$NON-NLS-2$
		result.put("http://java.sun.com/jsp/jstl/fmt", new String[] { jstl, "META-INF/fmt.tld" }); //$NON-NLS-1$ //$NON-NLS-2$
		result.put("http://java.sun.com/jsp/jstl/sql", new String[] { jstl, "META-INF/sql.tld" }); //$NON-NLS-1$ //$NON-NLS-2$
		result.put("http://java.sun.com/jsp/jstl/xml", new String[] { jstl, "META-INF/x.tld" }); //$NON-NLS-1$ //$NON-NLS-2$

		result.put("jakarta.tags.functions", new String[] { jstl, "META-INF/fn.tld" }); //$NON-NLS-1$ //$NON-NLS-2$
		result.put("jakarta.tags.core", new String[] { jstl, "META-INF/c.tld" }); //$NON-NLS-1$ //$NON-NLS-2$
		result.put("jakarta.tags.fmt", new String[] { jstl, "META-INF/fmt.tld" }); //$NON-NLS-1$ //$NON-NLS-2$
		result.put("jakarta.tags.sql", new String[] { jstl, "META-INF/sql.tld" }); //$NON-NLS-1$ //$NON-NLS-2$
		result.put("jakarta.tags.xml", new String[] { jstl, "META-INF/x.tld" }); //$NON-NLS-1$ //$NON-NLS-2$

		return result;
	}



	public static Collection<File> buildBundleClassPath() throws BundleException, IOException {
		Bundle bundle = FrameworkUtil.getBundle(PagesServletFactory.class);
		Collection<File> classpath = new LinkedHashSet<>();
		toClasspathEntry(bundle, classpath);

		return classpath;
	}

	private static void toClasspathEntry(final Bundle bundle, final Collection<File> classpath) throws BundleException, IOException {
		// These entries MUST be filesystem paths
		classpath.add(FileLocator.getBundleFile(bundle));

		String req = bundle.getHeaders().get("Require-Bundle"); //$NON-NLS-1$
		if(StringUtil.isNotEmpty(req)) {
			ManifestElement[] elements = ManifestElement.parseHeader("Require-Bundle", req); //$NON-NLS-1$
			for(ManifestElement element : elements) {
				String visibility = element.getDirective("visibility"); //$NON-NLS-1$
				if("reexport".equals(visibility)) { //$NON-NLS-1$
					String symbolicName = element.getValue();
					if(!symbolicName.startsWith("org.openntf.xsp.jakartaee")) { //$NON-NLS-1$
						Optional<Bundle> dep = LibraryUtil.getBundle(element.getValue());
						if(dep.isPresent()) {
							toClasspathEntry(dep.get(), classpath);
						}
					}
				}
			}
		}
	}
}
