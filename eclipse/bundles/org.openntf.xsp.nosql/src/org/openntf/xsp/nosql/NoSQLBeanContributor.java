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
package org.openntf.xsp.nosql;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jnosql.mapping.DatabaseQualifier;
import org.eclipse.jnosql.mapping.document.DefaultDocumentQueryPaginationProvider;
import org.eclipse.jnosql.mapping.document.spi.DocumentExtension;
import org.eclipse.jnosql.mapping.reflection.ClassMappingExtension;
import org.openntf.xsp.cdi.discovery.WeldBeanClassContributor;
import org.openntf.xsp.cdi.util.DiscoveryUtil;
import org.openntf.xsp.jakartaee.util.LibraryUtil;
import org.openntf.xsp.nosql.bean.ContextDatabaseSupplier;
import org.openntf.xsp.nosql.bean.ContextDocumentCollectionManagerProducer;
import org.osgi.framework.BundleException;
import org.osgi.framework.FrameworkUtil;

import jakarta.enterprise.inject.spi.Extension;

/**
 * @author Jesse Gallagher
 * @since 2.2.0
 */
public class NoSQLBeanContributor implements WeldBeanClassContributor {

	@Override
	public Collection<Class<?>> getBeanClasses() {
		if(LibraryUtil.isLibraryActive(NoSQLLibrary.LIBRARY_ID)) {
			// These classes are housed in fragments, so access those in a roundabout way
			List<Class<?>> result = new ArrayList<>();
			Stream.of(DatabaseQualifier.class, DefaultDocumentQueryPaginationProvider.class)
				.map(FrameworkUtil::getBundle)
				.map(Platform::getFragments)
				.flatMap(Arrays::stream)
				.flatMap(t -> {
					try {
						return DiscoveryUtil.findExportedClassNames(t, true)
							.map(className -> {
								try {
									return Platform.getHosts(t)[0].loadClass(className);
								} catch (ClassNotFoundException e) {
									throw new RuntimeException(e);
								}
							});
					} catch (BundleException e) {
						throw new RuntimeException(e);
					}
				})
				.forEach(result::add);
			
			result.add(ContextDocumentCollectionManagerProducer.class);
			result.add(ContextDatabaseSupplier.class);
			
			return result;
		} else {
			return Collections.emptySet();
		}
	}

	@Override
	public Collection<Extension> getExtensions() {
		if(LibraryUtil.isLibraryActive(NoSQLLibrary.LIBRARY_ID)) {
			return Arrays.asList(
				new ClassMappingExtension(),
				new DocumentExtension()
			);
		} else {
			return Collections.emptySet();
		}
	}

}
