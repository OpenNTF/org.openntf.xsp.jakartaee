/**
 * Copyright (c) 2018-2026 Contributors to the XPages Jakarta EE Support Project
 * Copyright (c) 2024, 2025 Contributors to the Eclipse Foundation.
 * Copyright (c) 2022, 2024 Payara Foundation and/or its affiliates.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */
package org.openntf.xsp.jakarta.cdi.concurrency;

import jakarta.enterprise.concurrent.Asynchronous;
import jakarta.enterprise.concurrent.ContextService;
import jakarta.enterprise.concurrent.ContextServiceDefinition;
import jakarta.enterprise.concurrent.ManagedExecutorDefinition;
import jakarta.enterprise.concurrent.ManagedExecutorService;
import jakarta.enterprise.concurrent.ManagedScheduledExecutorDefinition;
import jakarta.enterprise.concurrent.ManagedScheduledExecutorService;
import jakarta.enterprise.concurrent.ManagedThreadFactory;
import jakarta.enterprise.concurrent.ManagedThreadFactoryDefinition;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Default;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.spi.AfterBeanDiscovery;
import jakarta.enterprise.inject.spi.AnnotatedMethod;
import jakarta.enterprise.inject.spi.AnnotatedType;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.BeforeBeanDiscovery;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.ProcessAnnotatedType;
import jakarta.enterprise.inject.spi.ProcessBean;
import jakarta.enterprise.inject.spi.WithAnnotations;
import jakarta.transaction.Transactional;

import java.lang.System.Logger;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.glassfish.concurro.cdi.ConcurrencyManagedCDIBeans;
import org.glassfish.concurro.cdi.Lock;
import org.glassfish.concurro.cdi.QualifierAnnotationProxy;
import org.glassfish.concurro.cdi.asynchronous.AsynchronousInterceptor;
import org.glassfish.concurro.cdi.lock.LockInterceptor;

import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.Logger.Level.ERROR;
import static java.lang.System.Logger.Level.TRACE;

/**
 * CDI Extension for Jakarta Concurrent implementation backported from Payara.
 *
 * @author Petr Aubrecht (Payara)
 * @deprecated This variant should be replaced with the stock version if possible
 * @see <a href="https://github.com/OpenNTF/org.openntf.xsp.jakartaee/issues/708">Issue #708</a>
 */
@Deprecated
public class DominoConcurrentCDIExtension implements Extension {

    private static final Logger LOG = System.getLogger(DominoConcurrentCDIExtension.class.getName());
    private boolean isCSProduced = false;
    private boolean isMTFProduced = false;

    private ConcurrencyManagedCDIBeans configs = new ConcurrencyManagedCDIBeans();

    public void beforeBeanDiscovery(@Observes BeforeBeanDiscovery beforeBeanDiscovery, BeanManager beanManager) {
        LOG.log(TRACE, "ConcurrentCDIExtension.beforeBeanDiscovery");

        // Add each of the Concurrent interceptors
        addAnnotatedTypes(beforeBeanDiscovery, beanManager,
            Asynchronous.class, AsynchronousInterceptor.class,
            Lock.class, LockInterceptor.class);
    }

    /**
     * Check correct usage of the {@link Asynchronous} annotation.
     *
     * @param <T>
     * @param processAnnotatedType
     * @param beanManager
     * @throws Exception
     */
    public <T> void processAnnotatedType(@Observes @WithAnnotations({Asynchronous.class}) ProcessAnnotatedType<T> processAnnotatedType,
            BeanManager beanManager) throws Exception {
        LOG.log(TRACE, "ConcurrentCDIExtension.processAnnotatedType");
        AnnotatedType<T> annotatedType = processAnnotatedType.getAnnotatedType();

        // Validate the Asynchronous annotations for each annotated method
        Set<AnnotatedMethod<? super T>> annotatedMethods = annotatedType.getMethods();
        for (AnnotatedMethod<?> annotatedMethod : annotatedMethods) {
            Method method = annotatedMethod.getJavaMember();
            if (method.getDeclaringClass().equals(AsynchronousInterceptor.class)) {
                // skip interceptor
                continue;
            }
            Asynchronous annotation = method.getAnnotation(Asynchronous.class);
            if (annotation == null) {
                // method in the class, which is NOT annotated @Asynchronous
                continue;
            }
            Class<?> returnType = method.getReturnType();
            boolean validReturnType = returnType.equals(Void.TYPE)
                || returnType.equals(CompletableFuture.class)
                || returnType.equals(CompletionStage.class);
            if (!validReturnType) {
                throw new UnsupportedOperationException(
                    "Method \"" + method.getName() + "\"" + " annotated with " + Asynchronous.class.getCanonicalName()
                        + " does not return a CompletableFuture, CompletableFuture or void.");
            }
            Transactional transactionalAnnotation = annotatedMethod.getAnnotation(Transactional.class);
            if (transactionalAnnotation != null
                && transactionalAnnotation.value() != Transactional.TxType.REQUIRES_NEW
                && transactionalAnnotation.value() != Transactional.TxType.NOT_SUPPORTED) {
                throw new UnsupportedOperationException("Method \"" + method.getName() + "\"" + " annotated with "
                    + Asynchronous.class.getCanonicalName()
                    + " is annotated with @Transactional, but not one of the allowed types: REQUIRES_NEW or NOT_SUPPORTED.");
            }
        }
    }

    private void addDefinition(ConcurrencyManagedCDIBeans.Type type, Class<?>[] qualifiers, String jndiName) {
        if (qualifiers.length > 0) {
            configs.addDefinition(type,
                    Stream.of(qualifiers).map(c -> c.getName()).collect(Collectors.toSet()),
                    jndiName);
        }
    }

    /**
     * Process concurrency annotations on CDI beans.
     *
     * @param <T>
     * @param processAnnotatedType A class containing the annotations
     * @param beanManager CDI bean manager
     * @throws Exception no exception is thrown from this method
     */
    public <T> void processAnnotatedContextServiceDefinition(
            @Observes @WithAnnotations({ContextServiceDefinition.class, ManagedExecutorDefinition.class, ManagedThreadFactoryDefinition.class, ManagedScheduledExecutorDefinition.class}) ProcessAnnotatedType<T> processAnnotatedType,
            BeanManager beanManager) throws Exception {
        Set<ContextServiceDefinition> contextServiceDefinitions = processAnnotatedType.getAnnotatedType().getAnnotations(ContextServiceDefinition.class);
        for (ContextServiceDefinition definition : contextServiceDefinitions) {
            addDefinition(ConcurrencyManagedCDIBeans.Type.CONTEXT_SERVICE, definition.qualifiers(), definition.name());
        }
        Set<ManagedThreadFactoryDefinition> managedThreadFactoryDefinitions = processAnnotatedType.getAnnotatedType().getAnnotations(ManagedThreadFactoryDefinition.class);
        for (ManagedThreadFactoryDefinition definition : managedThreadFactoryDefinitions) {
            addDefinition(ConcurrencyManagedCDIBeans.Type.MANAGED_THREAD_FACTORY, definition.qualifiers(), definition.name());
        }
        Set<ManagedExecutorDefinition> managedExecutorDefinitions = processAnnotatedType.getAnnotatedType().getAnnotations(ManagedExecutorDefinition.class);
        for (ManagedExecutorDefinition definition : managedExecutorDefinitions) {
            addDefinition(ConcurrencyManagedCDIBeans.Type.MANAGED_EXECUTOR_SERVICE, definition.qualifiers(), definition.name());
        }
        Set<ManagedScheduledExecutorDefinition> managedScheduledExecutorDefinitions = processAnnotatedType.getAnnotatedType().getAnnotations(ManagedScheduledExecutorDefinition.class);
        for (ManagedScheduledExecutorDefinition definition : managedScheduledExecutorDefinitions) {
            addDefinition(ConcurrencyManagedCDIBeans.Type.MANAGED_SCHEDULED_EXECUTOR_SERVICE, definition.qualifiers(), definition.name());
        }
    }

    /**
     * Check, which default types are available via factories.
     *
     * @param <T>
     * @param event
     */
    public <T> void processBean(@Observes ProcessBean<T> event) {
        Bean<T> bean = event.getBean(); //bean.getBeanClass().getName().startsWith("ee.jakarta.tck")
        Set<Type> types = bean.getTypes();
        // Check, if there is a producer method for the default beans
        boolean defaultQualifiers = bean.getQualifiers().equals(new HashSet<>(Arrays.asList(Default.Literal.INSTANCE, Any.Literal.INSTANCE)));
        if (defaultQualifiers) {
            isCSProduced |= types.contains(ContextService.class);
            isMTFProduced |= types.contains(ManagedThreadFactory.class);
        }
    }

    /**
     * During AfterBeanDiscovery event, define the CDI beans depending on data
     * collected during annotations scan.
     *
     * @param event
     */
    void afterBeanDiscovery(@Observes final AfterBeanDiscovery event, BeanManager beanManager) {
        LOG.log(TRACE, "ConcurrentCDIExtension.afterBeanDiscovery");
        try {
            // define default beans, if there is no user-defined factory
            if (!isCSProduced) {
                event.addBean()
                        .beanClass(ContextService.class)
                        .types(ContextService.class)
                        .scope(ApplicationScoped.class)
                        .addQualifiers(Default.Literal.INSTANCE, Any.Literal.INSTANCE)
                        .produceWith((Instance<Object> inst) -> createInstanceContextService(inst, "java:comp/DefaultContextService"));
            }
            if (!isMTFProduced) {
                event.addBean()
                        .beanClass(ManagedThreadFactory.class)
                        .types(ManagedThreadFactory.class)
                        .scope(ApplicationScoped.class)
                        .addQualifiers(Default.Literal.INSTANCE, Any.Literal.INSTANCE)
                        .produceWith((Instance<Object> inst) -> createInstanceContextService(inst, "java:comp/DefaultManagedThreadFactory"));
            }

            try {
                // pick up ConcurrencyManagedCDIBeans definitions from JNDI, merge with CDI scanning, JNDI has a priority
                InitialContext ctx = new InitialContext();
                ConcurrencyManagedCDIBeans jndiConfigs = (ConcurrencyManagedCDIBeans) ctx.lookup(ConcurrencyManagedCDIBeans.JNDI_NAME);
                for (Map.Entry<String, ConcurrencyManagedCDIBeans.ConfiguredCDIBean> beanDefinitionEntry : jndiConfigs.getBeans().entrySet()) {
                    configs.addDefinition(beanDefinitionEntry.getValue().definitionType(),
                            beanDefinitionEntry.getValue().qualifiers(),
                            beanDefinitionEntry.getKey());

                }
            } catch (NamingException ex) {
                LOG.log(DEBUG, "Unable to load '" + ConcurrencyManagedCDIBeans.JNDI_NAME
                    + "' from JNDI, probably no concurrency definitions annotations found during scanning.", ex);
            }

            for (Map.Entry<String, ConcurrencyManagedCDIBeans.ConfiguredCDIBean> beanDefinitionEntry : configs.getBeans().entrySet()) {
                ConcurrencyManagedCDIBeans.ConfiguredCDIBean beanDefinition = beanDefinitionEntry.getValue();
                String jndiName = beanDefinitionEntry.getKey();
                Set<Annotation> annotations = new HashSet<>();
                Set<String> classNames = beanDefinition.qualifiers();
                if (!classNames.isEmpty()) {
                    for (String className : classNames) {
                        Class<? extends Annotation> annoCls = Thread.currentThread().getContextClassLoader().loadClass(className).asSubclass(Annotation.class);
                        Annotation annotationProxy = Annotation.class.cast(Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
                                new Class<?>[]{Annotation.class, annoCls},
                                new QualifierAnnotationProxy(annoCls)));
                        annotations.add(annotationProxy);
                    }

                    Class<?> beanClass = switch (beanDefinition.definitionType()) {
                        case CONTEXT_SERVICE ->
                            ContextService.class;
                        case MANAGED_THREAD_FACTORY ->
                            ManagedThreadFactory.class;
                        case MANAGED_EXECUTOR_SERVICE ->
                            ManagedExecutorService.class;
                        case MANAGED_SCHEDULED_EXECUTOR_SERVICE ->
                            ManagedScheduledExecutorService.class;
                    };

                    // register bean
                    event.addBean()
                            .beanClass(beanClass)
                            .types(beanClass)
                            .scope(ApplicationScoped.class)
                            .addQualifiers(annotations)
                            .produceWith((Instance<Object> inst) -> createInstanceContextService(inst, jndiName));
                }
            }
        } catch (ClassNotFoundException ex) {
            LOG.log(ERROR, "Unable to load class from application's classloader: " + ex.getMessage(), ex);
        }
    }

// This is not working as the annotation doesn't need to be on CDI bean
//    public <T> void processAnnotatedType(@Observes @WithAnnotations(ContextServiceDefinition.class) ProcessAnnotatedType<T> pat) {
//    }
//
    private Object createInstanceContextService(Instance<Object> inst, String jndi) {
        try {
            InitialContext ctx = new InitialContext();
            Object concurrencyObject = ctx.lookup(jndi);
            return concurrencyObject;
        } catch (NamingException ex) {
            throw new RuntimeException("Unable to fine JNDI '" + jndi + "': " + ex.getMessage(), ex);
        }
    }

    private static void addAnnotatedTypes(BeforeBeanDiscovery beforeBean, BeanManager beanManager, Class<?>... types) {
        for (Class<?> type : types) {
            beforeBean.addAnnotatedType(beanManager.createAnnotatedType(type), "Concurro " + type.getName());
        }
    }

}