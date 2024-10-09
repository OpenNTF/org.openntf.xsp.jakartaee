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
package org.openntf.xsp.jakarta.nosql;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import org.eclipse.jnosql.mapping.DatabaseQualifier;
import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.core.spi.EntityMetadataExtension;
import org.eclipse.jnosql.mapping.document.DocumentTemplate;
import org.eclipse.jnosql.mapping.document.spi.DocumentExtension;
import org.eclipse.jnosql.mapping.reflection.ConstructorException;
import org.eclipse.jnosql.mapping.semistructured.EntityConverter;
import org.eclipse.jnosql.mapping.validation.MappingValidator;
import org.openntf.xsp.jakarta.cdi.discovery.CDIClassContributor;
import org.openntf.xsp.jakarta.cdi.util.DiscoveryUtil;
import org.openntf.xsp.jakarta.nosql.bean.ContextDatabaseSupplier;
import org.openntf.xsp.jakarta.nosql.bean.ContextDocumentCollectionManagerProducer;
import org.openntf.xsp.jakarta.nosql.mapping.extension.DominoReflections;
import org.openntf.xsp.jakarta.nosql.mapping.extension.impl.DefaultDominoTemplate;
import org.openntf.xsp.jakarta.nosql.mapping.extension.impl.DominoExtension;
import org.osgi.framework.BundleException;
import org.osgi.framework.FrameworkUtil;

import jakarta.enterprise.inject.spi.Extension;

/**
 * @author Jesse Gallagher
 * @since 2.2.0
 */
public class NoSQLBeanContributor implements CDIClassContributor {

	@Override
	public Collection<Class<?>> getBeanClasses() {
		List<Class<?>> result = new ArrayList<>();
		Stream.of(DatabaseQualifier.class, DocumentTemplate.class, EntityConverter.class, ConstructorException.class, MappingValidator.class)
			.map(FrameworkUtil::getBundle)
			.flatMap(t -> {
				try {
					return DiscoveryUtil.findBeanClasses(t, false);
				} catch (BundleException e) {
					throw new RuntimeException(e);
				}
			})
			// Remove the built-in DocumentManagerSupplier, as we use an app-contextual one
			// TODO see if using the built-in one could work if we supply configuration
			.filter(c -> !"org.eclipse.jnosql.mapping.document.configuration.DocumentManagerSupplier".equals(c.getName())) //$NON-NLS-1$
			.forEach(result::add);

		result.add(ContextDocumentCollectionManagerProducer.class);
		result.add(ContextDatabaseSupplier.class);

		result.add(DefaultDominoTemplate.class);
		result.add(DominoReflections.class);

		result.add(Converters.class);
		result.add(EntityMetadataExtension.class);

		return result;
	}

	@Override
	public Collection<Extension> getExtensions() {
		return Collections.singleton(new EntityMetadataExtension());
	}

	@Override
	public Collection<Class<? extends Extension>> getExtensionClasses() {
		return Arrays.asList(
			DocumentExtension.class,

			DominoExtension.class
		);
	}

}
