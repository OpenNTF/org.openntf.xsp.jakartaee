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

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Set;

import org.eclipse.jnosql.mapping.DatabaseQualifier;
import org.openntf.xsp.nosql.communication.driver.DominoDocumentCollectionManager;
import org.openntf.xsp.nosql.mapping.extension.DominoTemplate;
import org.openntf.xsp.nosql.mapping.extension.DominoTemplateProducer;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.enterprise.inject.spi.PassivationCapable;
import jakarta.nosql.mapping.DatabaseType;

/**
 * Bean for producing {@link DominoTemplate} instances.
 * 
 * @author Jesse Gallagher
 * @since 2.5.0
 */
public class DominoTemplateBean implements Bean<DominoTemplate>, PassivationCapable {

	private final BeanManager beanManager;

    private final Set<Type> types;

    private final String provider;

    private final Set<Annotation> qualifiers;

    /**
     * Constructor
     *
     * @param beanManager the beanManager
     * @param provider    the provider name, that must be a
     */
    public DominoTemplateBean(BeanManager beanManager, String provider) {
        this.beanManager = beanManager;
        this.types = Collections.singleton(DominoTemplate.class);
        this.provider = provider;
        this.qualifiers = Collections.singleton(DatabaseQualifier.ofDocument(provider));
    }

    @Override
    public Class<?> getBeanClass() {
        return DominoTemplate.class;
    }

    @Override
    public Set<InjectionPoint> getInjectionPoints() {
        return Collections.emptySet();
    }

    @Override
    public boolean isNullable() {
        return false;
    }

    @Override
    public DominoTemplate create(CreationalContext<DominoTemplate> creationalContext) {

        DominoTemplateProducer producer = getInstance(DominoTemplateProducer.class);
        DominoDocumentCollectionManager manager = getManager();
        return producer.get(manager);
    }

    private DominoDocumentCollectionManager getManager() {
        @SuppressWarnings("unchecked")
		Bean<DominoDocumentCollectionManager> bean = (Bean<DominoDocumentCollectionManager>) beanManager.getBeans(DominoDocumentCollectionManager.class,
                DatabaseQualifier.ofDocument(provider) ).iterator().next();
        CreationalContext<DominoDocumentCollectionManager> ctx = beanManager.createCreationalContext(bean);
        return (DominoDocumentCollectionManager) beanManager.getReference(bean, DominoDocumentCollectionManager.class, ctx);
    }


    @SuppressWarnings("unchecked")
    private <T> T getInstance(Class<T> clazz) {
		Bean<T> bean = (Bean<T>) beanManager.getBeans(clazz).iterator().next();
        CreationalContext<T> ctx = beanManager.createCreationalContext(bean);
        return (T) beanManager.getReference(bean, clazz, ctx);
    }


    @Override
    public void destroy(DominoTemplate instance, CreationalContext<DominoTemplate> creationalContext) {

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
        return DominoTemplate.class.getName() + DatabaseType.DOCUMENT + '-' + provider;
    }

}
