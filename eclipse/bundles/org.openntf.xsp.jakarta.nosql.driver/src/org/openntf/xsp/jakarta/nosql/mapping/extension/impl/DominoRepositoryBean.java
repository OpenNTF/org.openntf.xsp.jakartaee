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
package org.openntf.xsp.jakarta.nosql.mapping.extension.impl;

import java.lang.annotation.Annotation;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Collections;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;

import org.eclipse.jnosql.mapping.DatabaseQualifier;
import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.core.spi.AbstractBean;
import org.eclipse.jnosql.mapping.metadata.EntitiesMetadata;
import org.openntf.xsp.jakarta.nosql.mapping.extension.DominoRepository;
import org.openntf.xsp.jakarta.nosql.mapping.extension.DominoTemplate;
import org.openntf.xsp.jakarta.nosql.mapping.extension.RepositoryProvider;

import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.Default;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.util.AnnotationLiteral;

/**
 * Bean for producing {@link DominoRepository} instances.
 *
 * @author Jesse Gallagher
 * @since 2.5.0
 */
public class DominoRepositoryBean<T, K> extends AbstractBean<DominoRepository<T, K>> {

	private final Class<?> type;
	private final BeanManager beanManager;
	private final Set<Type> types;

	private final Set<Annotation> qualifiers = Collections.singleton(new AnnotationLiteral<Default>() {
	});

	public DominoRepositoryBean(final Class<?> type, final BeanManager beanManager) {
		this.type = type;
		this.beanManager = beanManager;
		this.types = Collections.singleton(type);
	}

	@SuppressWarnings({ "removal", "deprecation", "unchecked" })
	@Override
	public DominoRepository<T, K> create(final CreationalContext<DominoRepository<T, K>> creationalContext) {
		DominoTemplate template;
		RepositoryProvider producerAnnotation = type.getAnnotation(RepositoryProvider.class);
		if (producerAnnotation != null) {
			template = getInstance(DominoTemplate.class, producerAnnotation.value())
				.orElseThrow(() ->
					new IllegalStateException("Unable to locate produced DominoTemplate for @Database(value = DatabaseType.DOCUMENT, provider = \""+ producerAnnotation.value() + "\")")
				);
		} else {
			template = getInstance(DominoTemplate.class, "") //$NON-NLS-1$
				.orElseThrow(() ->
					new IllegalStateException("Unable to locate produced DominoTemplate for provider = \"\"")
				);
		}
		// The default DocumentRepositoryProducer uses Class#getClassLoader
		return AccessController.doPrivileged((PrivilegedAction<DominoRepository<T, K>>)() -> {
			Converters converters = getInstance(Converters.class);
	        EntitiesMetadata entitiesMetadata = getInstance(EntitiesMetadata.class);
			DominoDocumentRepositoryProxy<T, K> handler = new DominoDocumentRepositoryProxy<>(template,
					type, converters, entitiesMetadata);
			return (DominoRepository<T, K>) Proxy.newProxyInstance(type.getClassLoader(), new Class[] { type }, handler);
		});
	}

	@SuppressWarnings("unchecked")
	private <S> Optional<S> getInstance(final Class<S> clazz, final String provider) {
		Iterator<Bean<?>> iter = beanManager.getBeans(clazz, DatabaseQualifier.ofDocument(provider)).iterator();
		if(!iter.hasNext()) {
			return Optional.empty();
		}
		Bean<S> bean = (Bean<S>) iter.next();
		CreationalContext<S> ctx = beanManager.createCreationalContext(bean);
		return Optional.of((S) beanManager.getReference(bean, clazz, ctx));
	}

	@Override
	public Set<Type> getTypes() {
		return types;
	}

	@Override
	public Set<Annotation> getQualifiers() {
		return qualifiers;
	}

	@Override
	public String getId() {
		return type.getName() + "@domino"; //$NON-NLS-1$
	}

	@Override
	public Class<?> getBeanClass() {
		return type;
	}

}
