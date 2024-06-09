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
package org.openntf.xsp.jakarta.cdi.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.osgi.util.ManifestElement;
import org.jboss.jandex.Index;
import org.jboss.jandex.IndexReader;
import org.openntf.xsp.jakartaee.util.LibraryUtil;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.osgi.framework.BundleReference;
import org.osgi.framework.Version;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.ibm.commons.util.StringUtil;
import com.ibm.commons.xml.DOMUtil;
import com.ibm.commons.xml.XMLException;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.context.NormalScope;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Stereotype;
import jakarta.interceptor.Interceptor;

/**
 * @author Jesse Gallagher
 * @since 2.2.0
 */
public enum DiscoveryUtil {
	;
	
	private enum ScanType {
		ALL, ANNOTATED, NONE, UNDEFINED
	}
	
	private static final Map<Class<?>, Boolean> BEAN_DEFINING = new ConcurrentHashMap<>();
	private static Field WEBAPP_BUNDLE_FIELD = null;
	
	/**
	 * Searches through the provided bundle to find all exported class names that may
	 * be applicable CDI beans.
	 * 
	 * <p>This restricts querying to bundles with a beans.xml file and to classes within
	 * the bundle's {@code Export-Package} listing.</p>
	 * 
	 * @param bundle a {@link Bundle} instance to query
	 * @param nonExported {@code true} to include classes not marked as exported from the bundle
	 * @return a {@link Stream} of discovered exported classes
	 * @throws BundleException if there is a problem parsing the bundle manifest
	 */
	public static Stream<String> findCandidateBeanClassNames(Bundle bundle, boolean nonExported) throws BundleException {
		ScanType scanType = determineScanType(bundle);
		
		if(scanType == ScanType.ALL || scanType == ScanType.ANNOTATED) {
			String exportPackages = bundle.getHeaders().get("Export-Package"); //$NON-NLS-1$
			if(StringUtil.isNotEmpty(exportPackages) || nonExported) {
				// Restrict to exported packages for sanity's sake
				Set<String> packages = null;
				if(!nonExported) {
					ManifestElement[] elements = ManifestElement.parseHeader("Export-Package", exportPackages); //$NON-NLS-1$
					packages = Arrays.stream(elements)
						.map(ManifestElement::getValue)
						.filter(StringUtil::isNotEmpty)
						.collect(Collectors.toSet());
				}
				
				URL jandexUrl = bundle.getResource("/META-INF/jandex.idx"); //$NON-NLS-1$
				if(jandexUrl != null) {
					Index jandex;
					try(InputStream is = jandexUrl.openStream()) {
						jandex = new IndexReader(is).read();
					} catch (IOException e) {
						throw new UncheckedIOException(MessageFormat.format("Encountered exception reading jandex.idx for {0}", bundle.getSymbolicName()), e);
					}
					
					if(packages != null) {
						return packages.stream()
							.map(p -> jandex.getClassesInPackage(p))
							.flatMap(Collection::stream)
							.map(c -> c.name().toString())
							.distinct();
					} else {
						return jandex.getKnownClasses()
							.stream()
							.map(c -> c.name().toString())
							.distinct();
					}
				
				} else {
					// Otherwise, do a manual crawl for class names
					String baseUrl = bundle.getEntry("/").toString(); //$NON-NLS-1$
					List<URL> entries = Collections.list(bundle.findEntries("/", "*.class", true)); //$NON-NLS-1$ //$NON-NLS-2$
					Set<String> classNames = new HashSet<>();
					Set<String> fpackages = packages;
					return entries.stream()
						.parallel()
						.map(String::valueOf)
						.map(url -> url.substring(baseUrl.length()))
						.map(LibraryUtil::toClassName)
						.filter(StringUtil::isNotEmpty)
						.filter(className -> fpackages == null || fpackages.contains(className.substring(0, className.lastIndexOf('.'))))
						.filter(className -> !classNames.contains(className))
						.peek(classNames::add)
						.sequential();
				}
			}
		}
		
		return Stream.empty();
	}
	
	/**
	 * Searches through the provided bundle to find all exported classes, loading them
	 * from the bundle.
	 * 
	 * <p>This restricts querying to bundles with a beans.xml file and to classes within
	 * the bundle's {@code Export-Package} listing.</p>
	 * 
	 * @param bundle a {@link Bundle} instance to query
	 * @param nonExported {@code true} to include classes not marked as exported from the bundle
	 * @return a {@link Stream} of discovered exported classes
	 * @throws BundleException if there is a problem parsing the bundle manifest
	 * @since 2.3.0
	 */
	public static Stream<Class<?>> findBeanClasses(Bundle bundle, boolean nonExported) throws BundleException {
		ScanType scanType = determineScanType(bundle);
		
		return findCandidateBeanClassNames(bundle, nonExported)
			.map(className -> {
				try {
					return bundle.loadClass(className);
				} catch (ClassNotFoundException e) {
					throw new RuntimeException(e);
				}
			})
			.filter(c -> {
				if(scanType == ScanType.ANNOTATED) {
					return Stream.of(c.getAnnotations())
						.anyMatch(DiscoveryUtil::isBeanDefining);
				} else {
					return true;
				}
			})
			.map(c -> (Class<?>)c);
	}
	
	public static Optional<Bundle> getBundleForClassLoader(ClassLoader cl) {
		// Equinox Servlets
		if(cl instanceof BundleReference) {
			return Optional.of(((BundleReference) cl).getBundle());
		}
		
		// Bundle webapps
		if("com.ibm.pvc.internal.webcontainer.webapp.BundleWebAppClassLoader".equals(cl.getClass().getName())) { //$NON-NLS-1$
			return AccessController.doPrivileged((PrivilegedAction<Optional<Bundle>>)() -> {
				try {
					if(WEBAPP_BUNDLE_FIELD == null) {
						WEBAPP_BUNDLE_FIELD = cl.getClass().getDeclaredField("bundle"); //$NON-NLS-1$
						WEBAPP_BUNDLE_FIELD.setAccessible(true);
					}
					return Optional.ofNullable((Bundle)WEBAPP_BUNDLE_FIELD.get(cl));
				} catch(Exception e) {
					throw new RuntimeException(e);
				}
			});
		}
		
		return Optional.empty();
	}
	
	private static ScanType determineScanType(Bundle bundle) {
		URL beansXml = bundle.getResource("/META-INF/beans.xml"); //$NON-NLS-1$
		if(beansXml == null) {
			beansXml = bundle.getResource("/WEB-INF/beans.xml"); //$NON-NLS-1$
		}
		if(beansXml == null) {
			return ScanType.UNDEFINED;
		}
		
		try {
			Document doc;
			try(InputStream is = beansXml.openStream()) {
				doc = DOMUtil.createDocument(is);
			}
			
			Element beans = doc.getDocumentElement();
			String version = beans.getAttribute("version"); //$NON-NLS-1$
			String val = beans.getAttribute("bean-discovery-mode"); //$NON-NLS-1$
			if(StringUtil.isEmpty(val)) {
				// Default is all in CDI < 4
				if(StringUtil.isNotEmpty(version)) {
					Version v = new Version(version);
					if(v.getMajor() >= 4) {
						return ScanType.ANNOTATED;
					} else {
						return ScanType.ALL;
					}
				}
				return ScanType.ALL;
			} else {
				return switch (val) {
					case "annotated": //$NON-NLS-1$
						yield ScanType.ANNOTATED;
					case "none": //$NON-NLS-1$
						yield ScanType.NONE;
					case "all": //$NON-NLS-1$
					default:
						yield ScanType.ALL;
				};
			}
		} catch(XMLException e) {
			throw new RuntimeException(e);
		} catch(IOException e) {
			throw new UncheckedIOException(e);
		}
	}
	
	// https://jakarta.ee/specifications/cdi/4.0/jakarta-cdi-spec-4.0.html#bean_defining_annotations
	public static boolean isBeanDefining(Annotation a) {
		Class<?> type = a.annotationType();
		return BEAN_DEFINING.computeIfAbsent(type, t -> {
			if(ApplicationScoped.class.equals(t)) {
				return true;
			} else if(RequestScoped.class.equals(t)) {
				return true;
			} else if(t.isAnnotationPresent(NormalScope.class)) {
				return true;
			} else if(Interceptor.class.equals(t)) {
				return true;
			} else if(t.isAnnotationPresent(Stereotype.class)) {
				return true;
			} else if(Dependent.class.equals(t)) {
				return true;
			}
			
			return false;
		});
	}
}
