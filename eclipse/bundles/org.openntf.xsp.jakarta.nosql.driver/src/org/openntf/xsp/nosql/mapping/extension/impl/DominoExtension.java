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
package org.openntf.xsp.nosql.mapping.extension.impl;

import static org.eclipse.jnosql.mapping.DatabaseType.DOCUMENT;

import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import org.eclipse.jnosql.mapping.DatabaseMetadata;
import org.eclipse.jnosql.mapping.Databases;
import org.openntf.xsp.nosql.communication.driver.DominoDocumentManager;
import org.openntf.xsp.nosql.mapping.extension.DominoRepository;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.AfterBeanDiscovery;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.ProcessAnnotatedType;
import jakarta.enterprise.inject.spi.ProcessProducer;

/**
 * CDI extension to provide Domino-specific NoSQL capabilities.
 * 
 * @author Jesse Gallagher
 * @since 2.5.0
 */
public class DominoExtension implements Extension {
	private static final Logger LOGGER = Logger.getLogger(DominoExtension.class.getName());

	private final Set<DatabaseMetadata> databases = new HashSet<>();

	private final Collection<Class<?>> crudTypes = new HashSet<>();

	@SuppressWarnings("rawtypes")
	<T extends DominoRepository> void onProcessAnnotatedType(@Observes final ProcessAnnotatedType<T> repo) {
		Class<T> javaClass = repo.getAnnotatedType().getJavaClass();

		if (DominoRepository.class.equals(javaClass)) {
			return;
		}

		if (DominoRepository.class.isAssignableFrom(javaClass) && Modifier.isInterface(javaClass.getModifiers())) {
			crudTypes.add(javaClass);
		}
	}

	<T, X extends DominoDocumentManager> void processProducer(@Observes final ProcessProducer<T, X> pp) {
		Databases.addDatabase(pp, DOCUMENT, databases);
	}

	void onAfterBeanDiscovery(@Observes final AfterBeanDiscovery afterBeanDiscovery, final BeanManager beanManager) {
		LOGGER.info("Starting the onAfterBeanDiscovery with elements number: " + crudTypes.size()); //$NON-NLS-1$

		databases.forEach(type -> {
			final DominoTemplateBean bean = new DominoTemplateBean(beanManager, type.getProvider());
			afterBeanDiscovery.addBean(bean);
		});

		crudTypes.forEach(type -> afterBeanDiscovery.addBean(new DominoRepositoryBean(type, beanManager)));

		LOGGER.info("Finished the onAfterBeanDiscovery"); //$NON-NLS-1$
	}
}
