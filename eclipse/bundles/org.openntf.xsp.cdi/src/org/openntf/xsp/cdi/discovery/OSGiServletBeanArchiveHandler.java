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
package org.openntf.xsp.cdi.discovery;

import java.io.UncheckedIOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.eclipse.core.runtime.Platform;
import org.eclipse.osgi.util.ManifestElement;
import org.jboss.weld.bootstrap.spi.BeansXml;
import org.jboss.weld.environment.deployment.discovery.BeanArchiveBuilder;
import org.jboss.weld.environment.deployment.discovery.BeanArchiveHandler;
import org.openntf.xsp.cdi.util.ContainerUtil;
import org.openntf.xsp.cdi.util.DiscoveryUtil;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;

import com.ibm.commons.util.StringUtil;
import com.ibm.designer.domino.napi.NotesAPIException;
import com.ibm.designer.domino.napi.NotesDatabase;
import com.ibm.domino.osgi.core.context.ContextInfo;

import jakarta.annotation.Priority;

/**
 * 
 * @author Jesse Gallagher
 * @since 1.2.0
 */
@Priority(Integer.MAX_VALUE-1)
public class OSGiServletBeanArchiveHandler implements BeanArchiveHandler {
	public static final ThreadLocal<Bundle> PROCESSING_BUNDLE = new ThreadLocal<>();
	public static final ThreadLocal<String> PROCESSING_ID = new ThreadLocal<>();
	private static final Map<String, Collection<String>> PROCESSED_BUNDLES_BY_ID = new HashMap<>();

	@Override
	public BeanArchiveBuilder handle(String beanArchiveReference) {
		try {
			Bundle bundle = PROCESSING_BUNDLE.get();
			if(bundle == null) {
				NotesDatabase database = ContextInfo.getServerDatabase();
				if(database != null) {
					String bundleName = ContainerUtil.getApplicationCDIBundle(database);
					if(StringUtil.isNotEmpty(bundleName)) {
						bundle = Platform.getBundle(bundleName);
					} else {
						bundleName = ContainerUtil.getApplicationCDIBundleBase(database);
						if(StringUtil.isNotEmpty(bundleName)) {
							bundle = Platform.getBundle(bundleName);
						}
					}
				}
			}
			
			if(bundle != null) {
				String symbolicName = bundle.getSymbolicName();
				// Slightly customize the builder to keep some extra metadata
				BeanArchiveBuilder builder = new BeanArchiveBuilder() {
					{
						super.setBeansXml(BeansXml.EMPTY_BEANS_XML);
						super.setId(symbolicName);
					}
					
					@Override
					public BeanArchiveBuilder setBeansXml(BeansXml beansXml) {
						return this;
					}
				};
				
				Collection<String> bundleNames;
				String processingId = PROCESSING_ID.get();
				if(StringUtil.isNotEmpty(processingId)) {
					bundleNames = PROCESSED_BUNDLES_BY_ID.computeIfAbsent(processingId, k -> new HashSet<>());
				} else {
					bundleNames = new HashSet<>();
				}
				Collection<String> classNames = new HashSet<>();
				addClasses(bundle, builder, bundleNames, classNames);
				
				return builder;
			}
		} catch (NotesAPIException | UncheckedIOException | BundleException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public void addClasses(Bundle bundle, BeanArchiveBuilder builder, Collection<String> bundleNames, Collection<String> classNames) throws BundleException {
		String symbolicName = bundle.getSymbolicName();
		if(bundleNames.contains(symbolicName)) {
			return;
		}
		bundleNames.add(symbolicName);
		
		// Only look when there's a beans.xml, to be less costly
		DiscoveryUtil.findExportedClassNames(bundle, false).forEach(builder::addClass);
		
		String requireBundle = bundle.getHeaders().get("Require-Bundle"); //$NON-NLS-1$
		if(StringUtil.isNotEmpty(requireBundle)) {
			ManifestElement[] elements = ManifestElement.parseHeader("Require-Bundle", requireBundle); //$NON-NLS-1$
			for(ManifestElement el : elements) {
				String bundleName = el.getValue();
				if(StringUtil.isNotEmpty(bundleName)) {
					Bundle dependency = Platform.getBundle(bundleName);
					if(dependency != null) {
						addClasses(dependency, builder, bundleNames, classNames);
					}
				}
			}
		}
	}
}
