package org.openntf.xsp.cdi.util;

import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.osgi.util.ManifestElement;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;

import com.ibm.commons.util.StringUtil;

/**
 * @author Jesse Gallagher
 * @since 2.2.0
 */
public enum DiscoveryUtil {
	;
	
	/**
	 * Searches through the provided bundle to find all exported classes.
	 * 
	 * <p>This restricts querying to bundles with a beans.xml file and to classes within
	 * the bundle's {@code Export-Package} listing.</p>
	 * 
	 * @param bundle a {@link Bundle} instance to query
	 * @return a {@link Stream} of discovered exported classes
	 * @throws BundleException if there is a problem parsing the bundle manifest
	 */
	public static Stream<String> findExportedClasses(Bundle bundle) throws BundleException {
		if(bundle.getResource("/META-INF/beans.xml") != null || bundle.getResource("/WEB-INF/beans.xml") != null) { //$NON-NLS-1$ //$NON-NLS-2$
			String exportPackages = bundle.getHeaders().get("Export-Package"); //$NON-NLS-1$
			if(StringUtil.isNotEmpty(exportPackages)) {
				// Restrict to exported packages for sanity's sake
				ManifestElement[] elements = ManifestElement.parseHeader("Export-Package", exportPackages); //$NON-NLS-1$
				Set<String> packages = Arrays.stream(elements)
					.map(ManifestElement::getValue)
					.filter(StringUtil::isNotEmpty)
					.collect(Collectors.toSet());
				
				Set<String> classNames = new HashSet<>();
				String baseUrl = bundle.getEntry("/").toString(); //$NON-NLS-1$
				List<URL> entries = Collections.list(bundle.findEntries("/", "*.class", true)); //$NON-NLS-1$ //$NON-NLS-2$
				return entries.stream()
					.parallel()
					.map(String::valueOf)
					.map(url -> url.substring(baseUrl.length()))
					.map(DiscoveryUtil::toClassName)
					.filter(StringUtil::isNotEmpty)
					.filter(className -> packages.contains(className.substring(0, className.lastIndexOf('.'))))
					.filter(className -> !classNames.contains(className))
					.peek(classNames::add)
					.sequential();
			}
		}
		
		return Stream.empty();
	}
	
	private static String toClassName(String resourceName) {
		if(StringUtil.isEmpty(resourceName)) {
			return null;
		} else if(resourceName.startsWith("target/classes")) { //$NON-NLS-1$
			// Not a real class name
			return null;
		} else if(resourceName.startsWith("bin/")) { //$NON-NLS-1$
			// Not a real class name
			return null;
		}
		
		return resourceName
			.substring(0, resourceName.length()-".class".length()) //$NON-NLS-1$
			.replace('/', '.');
	}
}
