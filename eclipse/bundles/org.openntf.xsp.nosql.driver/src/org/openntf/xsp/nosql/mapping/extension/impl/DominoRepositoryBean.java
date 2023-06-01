/**
 * Copyright (c) 2018-2023 Contributors to the XPages Jakarta EE Support Project
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
import org.openntf.xsp.nosql.mapping.extension.DominoRepository;
import org.openntf.xsp.nosql.mapping.extension.DominoTemplate;
import org.openntf.xsp.nosql.mapping.extension.RepositoryProvider;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.Default;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.enterprise.inject.spi.PassivationCapable;
import jakarta.enterprise.util.AnnotationLiteral;
import jakarta.nosql.mapping.Repository;
import jakarta.nosql.mapping.document.DocumentRepositoryProducer;

/**
 * Bean for producing {@link DominoRepository} instances.
 * 
 * @author Jesse Gallagher
 * @since 2.5.0
 */
public class DominoRepositoryBean implements Bean<DominoRepository<?, ?>>, PassivationCapable {

	private final Class<?> type;
	private final BeanManager beanManager;
	private final Set<Type> types;

	@SuppressWarnings("serial")
	private final Set<Annotation> qualifiers = Collections.singleton(new AnnotationLiteral<Default>() {
	});

	public DominoRepositoryBean(Class<?> type, BeanManager beanManager) {
		this.type = type;
		this.beanManager = beanManager;
		this.types = Collections.singleton(type);
	}

	@Override
	public DominoRepository<?, ?> create(CreationalContext<DominoRepository<?, ?>> creationalContext) {
		DominoTemplate template;
		RepositoryProvider producerAnnotation = type.getAnnotation(RepositoryProvider.class);
		if (producerAnnotation != null) {
			template = getInstance(DominoTemplate.class, producerAnnotation.value())
				.orElseThrow(() -> 
					new IllegalStateException("Unable to locate producer method for @Database(value = DatabaseType.DOCUMENT, provider = \""+ producerAnnotation.value() + "\")")
				);
		} else {
			template = getInstance(DominoTemplate.class, "") //$NON-NLS-1$
				.orElseThrow(() ->
					new IllegalStateException("Unable to locate producer method for @Database(value = DatabaseType.DOCUMENT, provider = \"\")")
				);
		}
		DocumentRepositoryProducer producer = getInstance(DocumentRepositoryProducer.class)
			.orElseThrow(() -> new IllegalStateException("Unable to locate bean for " + DocumentRepositoryProducer.class));
		// The default DocumentRepositoryProducer uses Class#getClassLoader
		return AccessController.doPrivileged((PrivilegedAction<DominoRepository<?, ?>>)() -> {
			@SuppressWarnings("unchecked")
			Repository<Object, String> repository = producer.get((Class<Repository<Object, String>>) type, template);

			DominoDocumentRepositoryProxy<DominoRepository<?, String>> handler = new DominoDocumentRepositoryProxy<>(template,
					type, repository);
			return (DominoRepository<?, ?>) Proxy.newProxyInstance(type.getClassLoader(), new Class[] { type }, handler);
		});
	}

	@SuppressWarnings("unchecked")
	private <T> Optional<T> getInstance(Class<T> clazz) {
		Iterator<Bean<?>> iter = beanManager.getBeans(clazz).iterator();
		if(!iter.hasNext()) {
			return Optional.empty();
		}
		Bean<T> bean = (Bean<T>) iter.next();
		CreationalContext<T> ctx = beanManager.createCreationalContext(bean);
		return Optional.of((T) beanManager.getReference(bean, clazz, ctx));
	}

	@SuppressWarnings("unchecked")
	private <T> Optional<T> getInstance(Class<T> clazz, String provider) {
		Iterator<Bean<?>> iter = beanManager.getBeans(clazz, DatabaseQualifier.ofDocument(provider)).iterator();
		if(!iter.hasNext()) {
			return Optional.empty();
		}
		Bean<T> bean = (Bean<T>) iter.next();
		CreationalContext<T> ctx = beanManager.createCreationalContext(bean);
		return Optional.of((T) beanManager.getReference(bean, clazz, ctx));
	}

	@Override
	public void destroy(DominoRepository<?, ?> instance, CreationalContext<DominoRepository<?, ?>> creationalContext) {
		// NOP
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
	public Class<? extends Annotation> getScope() {
		return ApplicationScoped.class;
	}

	@Override
	public String getName() {
		return null;
	}

	@Override
	public Set<Class<? extends Annotation>> getStereotypes() {
		return Collections.emptySet();
	}

	@Override
	public boolean isAlternative() {
		return false;
	}

	@Override
	public String getId() {
		return type.getName() + "@domino"; //$NON-NLS-1$
	}

	@Override
	public Class<?> getBeanClass() {
		return type;
	}

	@Override
	public Set<InjectionPoint> getInjectionPoints() {
		return Collections.emptySet();
	}

}
