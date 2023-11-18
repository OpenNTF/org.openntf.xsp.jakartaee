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
package org.openntf.xsp.jaxrs.impl;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.jboss.resteasy.core.ResteasyContext;
import org.jboss.resteasy.core.providerfactory.ClientHelper;
import org.jboss.resteasy.core.providerfactory.ResteasyProviderFactoryImpl;
import org.jboss.resteasy.core.providerfactory.ServerHelper;
import org.jboss.resteasy.plugins.providers.RegisterBuiltin;
import org.jboss.resteasy.spi.AsyncClientResponseProvider;
import org.jboss.resteasy.spi.AsyncResponseProvider;
import org.jboss.resteasy.spi.AsyncStreamProvider;
import org.jboss.resteasy.spi.ContextInjector;
import org.jboss.resteasy.spi.Dispatcher;
import org.jboss.resteasy.spi.HttpRequest;
import org.jboss.resteasy.spi.HttpResponse;
import org.jboss.resteasy.spi.InjectorFactory;
import org.jboss.resteasy.spi.ProviderFactoryDelegate;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.jboss.resteasy.spi.StringParameterUnmarshaller;
import org.jboss.resteasy.spi.interception.JaxrsInterceptorRegistry;
import org.jboss.resteasy.spi.metadata.ResourceBuilder;
import org.jboss.resteasy.spi.metadata.ResourceClassProcessor;
import org.jboss.resteasy.spi.statistics.StatisticsController;
import org.jboss.resteasy.tracing.RESTEasyTracingLogger;
import org.openntf.xsp.jakartaee.module.ComponentModuleLocator;

import com.ibm.designer.runtime.domino.adapter.ComponentModule;

import jakarta.servlet.ServletContext;
import jakarta.ws.rs.RuntimeType;
import jakarta.ws.rs.client.ClientRequestFilter;
import jakarta.ws.rs.client.ClientResponseFilter;
import jakarta.ws.rs.client.RxInvoker;
import jakarta.ws.rs.client.RxInvokerProvider;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.container.DynamicFeature;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Configuration;
import jakarta.ws.rs.core.Feature;
import jakarta.ws.rs.core.Link.Builder;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response.ResponseBuilder;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.Variant.VariantListBuilder;
import jakarta.ws.rs.ext.ContextResolver;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.MessageBodyReader;
import jakarta.ws.rs.ext.MessageBodyWriter;
import jakarta.ws.rs.ext.ParamConverter;
import jakarta.ws.rs.ext.ReaderInterceptor;
import jakarta.ws.rs.ext.WriterInterceptor;

@SuppressWarnings("rawtypes")
public class DelegatingRuntimeDelegate extends ResteasyProviderFactoryImpl implements ProviderFactoryDelegate {
	private static final String KEY_DELEGATE = DelegatingRuntimeDelegate.class.getName();
	public static final DelegatingRuntimeDelegate INSTANCE = new DelegatingRuntimeDelegate();

	@Override
	public UriBuilder createUriBuilder() {
		return getDelegate().createUriBuilder();
	}

	@Override
	public ResponseBuilder createResponseBuilder() {
		return getDelegate().createResponseBuilder();
	}

	@Override
	public VariantListBuilder createVariantListBuilder() {
		return getDelegate().createVariantListBuilder();
	}

	@Override
	public <T> T createEndpoint(Application application, Class<T> endpointType)
			throws IllegalArgumentException, UnsupportedOperationException {
		return getDelegate().createEndpoint(application, endpointType);
	}

	@Override
	public <T> HeaderDelegate<T> createHeaderDelegate(Class<T> type) throws IllegalArgumentException {
		return getDelegate().createHeaderDelegate(type);
	}

	@Override
	public Builder createLinkBuilder() {
		return getDelegate().createLinkBuilder();
	}
	
	@Override
	public <T> MessageBodyReader<T> getMessageBodyReader(Class<T> type, Type genericType, Annotation[] annotations,
			MediaType mediaType) {
		return getDelegate().getMessageBodyReader(type, genericType, annotations, mediaType);
	}

	@Override
	public <T> MessageBodyWriter<T> getMessageBodyWriter(Class<T> type, Type genericType, Annotation[] annotations,
			MediaType mediaType) {
		return getDelegate().getMessageBodyWriter(type, genericType, annotations, mediaType);
	}

	@Override
	public <T extends Throwable> ExceptionMapper<T> getExceptionMapper(Class<T> type) {
		return getDelegate().getExceptionMapper(type);
	}

	@Override
	public <T> ContextResolver<T> getContextResolver(Class<T> contextType, MediaType mediaType) {
		return getDelegate().getContextResolver(contextType, mediaType);
	}

	@Override
	public String toHeaderString(Object object) {
		return getDelegate().toHeaderString(object);
	}

	@Override
	public Configuration getConfiguration() {
		return getDelegate().getConfiguration();
	}

	@Override
	public ResteasyProviderFactory property(String name, Object value) {
		return getDelegate().property(name, value);
	}

	@Override
	public ResteasyProviderFactory register(Class<?> componentClass) {
		return getDelegate().register(componentClass);
	}

	@Override
	public ResteasyProviderFactory register(Class<?> componentClass, int priority) {
		return getDelegate().register(componentClass, priority);
	}

	@Override
	public ResteasyProviderFactory register(Class<?> componentClass, Class<?>... contracts) {
		return getDelegate().register(componentClass, contracts);
	}

	@Override
	public ResteasyProviderFactory register(Class<?> componentClass, Map<Class<?>, Integer> contracts) {
		return getDelegate().register(componentClass, contracts);
	}

	@Override
	public ResteasyProviderFactory register(Object component) {
		return getDelegate().register(component);
	}

	@Override
	public ResteasyProviderFactory register(Object component, int priority) {
		return getDelegate().register(component, priority);
	}

	@Override
	public ResteasyProviderFactory register(Object component, Class<?>... contracts) {
		return getDelegate().register(component, contracts);
	}

	@Override
	public ResteasyProviderFactory register(Object component, Map<Class<?>, Integer> contracts) {
		return getDelegate().register(component, contracts);
	}

	@Override
	public RuntimeType getRuntimeType() {
		return getDelegate().getRuntimeType();
	}

	@Override
	public Map<String, Object> getProperties() {
		return getDelegate().getProperties();
	}

	@Override
	public Object getProperty(String name) {
		return getDelegate().getProperty(name);
	}

	@Override
	public Collection<String> getPropertyNames() {
		return getDelegate().getPropertyNames();
	}

	@Override
	public boolean isEnabled(Feature feature) {
		return getDelegate().isEnabled(feature);
	}



	public Set<DynamicFeature> getServerDynamicFeatures() {
		return getDelegate().getServerDynamicFeatures();
	}

	public Set<DynamicFeature> getClientDynamicFeatures() {
		return getDelegate().getClientDynamicFeatures();
	}

	public Map<Class<?>, AsyncResponseProvider> getAsyncResponseProviders() {
		return getDelegate().getAsyncResponseProviders();
	}

	public Map<Class<?>, AsyncClientResponseProvider> getAsyncClientResponseProviders() {
		return getDelegate().getAsyncClientResponseProviders();
	}

	public Map<Class<?>, AsyncStreamProvider> getAsyncStreamProviders() {
		return getDelegate().getAsyncStreamProviders();
	}

	public Map<Type, ContextInjector> getContextInjectors() {
		return getDelegate().getContextInjectors();
	}

	public Map<Type, ContextInjector> getAsyncContextInjectors() {
		return getDelegate().getAsyncContextInjectors();
	}

	public Set<Class<?>> getProviderClasses() {
		return getDelegate().getProviderClasses();
	}

	public Set<Object> getProviderInstances() {
		return getDelegate().getProviderInstances();
	}

	public <T> T getContextData(Class<T> type) {
		return getDelegate().getContextData(type);
	}

	public <T> T getContextData(Class<T> rawType, Type genericType, Annotation[] annotations, boolean unwrapAsync) {
		return getDelegate().getContextData(rawType, genericType, annotations, unwrapAsync);
	}

	public boolean isEnabled(Class<? extends Feature> featureClass) {
		return getDelegate().isEnabled(featureClass);
	}

	public boolean isRegistered(Object component) {
		return getDelegate().isRegistered(component);
	}

	public boolean isRegistered(Class<?> componentClass) {
		return getDelegate().isRegistered(componentClass);
	}

	public Map<Class<?>, Integer> getContracts(Class<?> componentClass) {
		return getDelegate().getContracts(componentClass);
	}

	public Set<Class<?>> getClasses() {
		return getDelegate().getClasses();
	}

	public boolean isRegisterBuiltins() {
		return getDelegate().isRegisterBuiltins();
	}

	public void setRegisterBuiltins(boolean registerBuiltins) {
		getDelegate().setRegisterBuiltins(registerBuiltins);
	}

	public InjectorFactory getInjectorFactory() {
		return getDelegate().getInjectorFactory();
	}

	public void setInjectorFactory(InjectorFactory injectorFactory) {
		getDelegate().setInjectorFactory(injectorFactory);
	}

	public JaxrsInterceptorRegistry<ReaderInterceptor> getServerReaderInterceptorRegistry() {
		return getDelegate().getServerReaderInterceptorRegistry();
	}

	public JaxrsInterceptorRegistry<WriterInterceptor> getServerWriterInterceptorRegistry() {
		return getDelegate().getServerWriterInterceptorRegistry();
	}

	public JaxrsInterceptorRegistry<ContainerRequestFilter> getContainerRequestFilterRegistry() {
		return getDelegate().getContainerRequestFilterRegistry();
	}

	public Set<Object> getInstances() {
		return getDelegate().getInstances();
	}

	public JaxrsInterceptorRegistry<ContainerResponseFilter> getContainerResponseFilterRegistry() {
		return getDelegate().getContainerResponseFilterRegistry();
	}

	public JaxrsInterceptorRegistry<ReaderInterceptor> getClientReaderInterceptorRegistry() {
		return getDelegate().getClientReaderInterceptorRegistry();
	}

	public JaxrsInterceptorRegistry<WriterInterceptor> getClientWriterInterceptorRegistry() {
		return getDelegate().getClientWriterInterceptorRegistry();
	}

	public JaxrsInterceptorRegistry<ClientRequestFilter> getClientRequestFilterRegistry() {
		return getDelegate().getClientRequestFilterRegistry();
	}

	public JaxrsInterceptorRegistry<ClientResponseFilter> getClientResponseFilters() {
		return getDelegate().getClientResponseFilters();
	}

	public boolean isBuiltinsRegistered() {
		return getDelegate().isBuiltinsRegistered();
	}

	public void setBuiltinsRegistered(boolean builtinsRegistered) {
		getDelegate().setBuiltinsRegistered(builtinsRegistered);
	}

	public void addHeaderDelegate(Class clazz, HeaderDelegate header) {
		getDelegate().addHeaderDelegate(clazz, header);
	}

	@SuppressWarnings("deprecation")
	public <T> MessageBodyReader<T> getServerMessageBodyReader(Class<T> type, Type genericType,
			Annotation[] annotations, MediaType mediaType) {
		return getDelegate().getServerMessageBodyReader(type, genericType, annotations, mediaType);
	}

	public <T> MessageBodyReader<T> getClientMessageBodyReader(Class<T> type, Type genericType,
			Annotation[] annotations, MediaType mediaType) {
		return getDelegate().getClientMessageBodyReader(type, genericType, annotations, mediaType);
	}

	public List<ContextResolver> getContextResolvers(Class<?> clazz, MediaType type) {
		return getDelegate().getContextResolvers(clazz, type);
	}

	public ParamConverter getParamConverter(Class clazz, Type genericType, Annotation[] annotations) {
		return getDelegate().getParamConverter(clazz, genericType, annotations);
	}

	public <T> StringParameterUnmarshaller<T> createStringParameterUnmarshaller(Class<T> clazz) {
		return getDelegate().createStringParameterUnmarshaller(clazz);
	}

	public void registerProvider(Class provider) {
		getDelegate().registerProvider(provider);
	}

	public HeaderDelegate getHeaderDelegate(Class<?> aClass) {
		return getDelegate().getHeaderDelegate(aClass);
	}

	public void registerProvider(Class provider, boolean isBuiltin) {
		getDelegate().registerProvider(provider, isBuiltin);
	}

	public void registerProvider(Class provider, Integer priorityOverride, boolean isBuiltin,
			Map<Class<?>, Integer> contracts) {
		getDelegate().registerProvider(provider, priorityOverride, isBuiltin, contracts);
	}

	public void registerProviderInstance(Object provider) {
		getDelegate().registerProviderInstance(provider);
	}

	public void registerProviderInstance(Object provider, Map<Class<?>, Integer> contracts, Integer priorityOverride,
			boolean builtIn) {
		getDelegate().registerProviderInstance(provider, contracts, priorityOverride, builtIn);
	}

	public <T> AsyncResponseProvider<T> getAsyncResponseProvider(Class<T> type) {
		return getDelegate().getAsyncResponseProvider(type);
	}

	public <T> AsyncClientResponseProvider<T> getAsyncClientResponseProvider(Class<T> type) {
		return getDelegate().getAsyncClientResponseProvider(type);
	}

	public <T> AsyncStreamProvider<T> getAsyncStreamProvider(Class<T> type) {
		return getDelegate().getAsyncStreamProvider(type);
	}

	public MediaType getConcreteMediaTypeFromMessageBodyWriters(Class<?> type, Type genericType,
			Annotation[] annotations, MediaType mediaType) {
		return getDelegate().getConcreteMediaTypeFromMessageBodyWriters(type, genericType, annotations, mediaType);
	}

	public Map<MessageBodyWriter<?>, Class<?>> getPossibleMessageBodyWritersMap(Class type, Type genericType,
			Annotation[] annotations, MediaType accept) {
		return getDelegate().getPossibleMessageBodyWritersMap(type, genericType, annotations, accept);
	}

	@SuppressWarnings("deprecation")
	public <T> MessageBodyWriter<T> getServerMessageBodyWriter(Class<T> type, Type genericType,
			Annotation[] annotations, MediaType mediaType) {
		return getDelegate().getServerMessageBodyWriter(type, genericType, annotations, mediaType);
	}

	public <T> MessageBodyWriter<T> getClientMessageBodyWriter(Class<T> type, Type genericType,
			Annotation[] annotations, MediaType mediaType) {
		return getDelegate().getClientMessageBodyWriter(type, genericType, annotations, mediaType);
	}

	public <T> T createProviderInstance(Class<? extends T> clazz) {
		return getDelegate().createProviderInstance(clazz);
	}

	public <T> T injectedInstance(Class<? extends T> clazz) {
		return getDelegate().injectedInstance(clazz);
	}

	public <T> T injectedInstance(Class<? extends T> clazz, HttpRequest request, HttpResponse response) {
		return getDelegate().injectedInstance(clazz, request, response);
	}

	public void injectProperties(Object obj) {
		getDelegate().injectProperties(obj);
	}

	public void injectProperties(Object obj, HttpRequest request, HttpResponse response) {
		getDelegate().injectProperties(obj, request, response);
	}

	public Map<String, Object> getMutableProperties() {
		return getDelegate().getMutableProperties();
	}

	public ResteasyProviderFactory setProperties(Map<String, Object> properties) {
		return getDelegate().setProperties(properties);
	}

	public Collection<Feature> getEnabledFeatures() {
		return getDelegate().getEnabledFeatures();
	}

	public <I extends RxInvoker> RxInvokerProvider<I> getRxInvokerProvider(Class<I> clazz) {
		return getDelegate().getRxInvokerProvider(clazz);
	}

	public RxInvokerProvider<?> getRxInvokerProviderFromReactiveClass(Class<?> clazz) {
		return getDelegate().getRxInvokerProviderFromReactiveClass(clazz);
	}

	public boolean isReactive(Class<?> clazz) {
		return getDelegate().isReactive(clazz);
	}

	public ResourceBuilder getResourceBuilder() {
		return getDelegate().getResourceBuilder();
	}

	public void initializeClientProviders(ResteasyProviderFactory factory) {
		getDelegate().initializeClientProviders(factory);
	}

	public StatisticsController getStatisticsController() {
		return getDelegate().getStatisticsController();
	}


	@Override
	protected void registerBuiltin() {
		RegisterBuiltin.register(this);
	}

	@Override
	public String toString(Object object, Class clazz, Type genericType, Annotation[] annotations) {
		return getDelegate().toString(object, clazz, genericType, annotations);
	}

	@Override
	protected boolean isOnServer() {
		return ResteasyContext.searchContextData(Dispatcher.class) != null;
	}

	@Override
	public void lockSnapshots() {
		getDelegate().lockSnapshots();
	}

	@Override
	public Map<Class<?>, Map<Class<?>, Integer>> getClassContracts() {
		return getDelegate().getClassContracts();
	}

	@Override
	public <T> MessageBodyReader<T> getServerMessageBodyReader(Class<T> type, Type genericType,
			Annotation[] annotations, MediaType mediaType, RESTEasyTracingLogger tracingLogger) {
		return getDelegate().getServerMessageBodyReader(type, genericType, annotations, mediaType, tracingLogger);
	}

	@Override
	public void addStringParameterUnmarshaller(Class<? extends StringParameterUnmarshaller> provider) {
		getDelegate().addStringParameterUnmarshaller(provider);
	}

	@Override
	public Set<Class<?>> getMutableProviderClasses() {
		return getDelegate().getMutableProviderClasses();
	}

	@Override
	public void addHeaderDelegate(Class provider) {
		getDelegate().addHeaderDelegate(provider);
	}

	@Override
	public ClientHelper getClientHelper() {
		return getDelegate().getClientHelper();
	}

	@Override
	public ServerHelper getServerHelper() {
		return getDelegate().getServerHelper();
	}

	@Override
	public void addHeaderDelegate(Class<? extends HeaderDelegate> provider, Class<?> headerClass) {
		getDelegate().addHeaderDelegate(provider, headerClass);
	}

	@Override
	public void addFeature(Class<? extends Feature> provider) {
		getDelegate().addFeature(provider);
	}

	@Override
	public void addInjectorFactory(Class provider) throws InstantiationException, IllegalAccessException {
		getDelegate().addInjectorFactory(provider);
	}

	@Override
	public void addContextInjector(Class provider) {
		getDelegate().addContextInjector(provider);
	}

	@Override
	public void addContextResolver(Class provider, boolean isBuiltin, int priority) {
		getDelegate().addContextResolver(provider, isBuiltin, priority);
	}

	@Override
	public void addParameterConverterProvider(Class provider, boolean isBuiltin, int priority) {
		getDelegate().addParameterConverterProvider(provider, isBuiltin, priority);
	}

	@Override
	public <T extends Throwable> ExceptionMapper<T> getExceptionMapperForClass(Class<T> type) {
		return getDelegate().getExceptionMapperForClass(type);
	}

	@Override
	public <T> MessageBodyWriter<T> getServerMessageBodyWriter(Class<T> type, Type genericType,
			Annotation[] annotations, MediaType mediaType, RESTEasyTracingLogger tracingLogger) {
		return getDelegate().getServerMessageBodyWriter(type, genericType, annotations, mediaType, tracingLogger);
	}

	@Override
	public void addResourceClassProcessor(Class<ResourceClassProcessor> processorClass, int priority) {
		getDelegate().addResourceClassProcessor(processorClass, priority);
	}
	
	@Override
	public ResteasyProviderFactoryImpl getDelegate() {
		Optional<ServletContext> ctx = ComponentModuleLocator.getDefault()
			.flatMap(ComponentModuleLocator::getServletContext);
		if(ctx.isPresent()) {
			ResteasyProviderFactoryImpl del = (ResteasyProviderFactoryImpl)ctx.get().getAttribute(KEY_DELEGATE);
			if(del == null) {
				del = new ResteasyProviderFactoryImpl();
				ctx.get().setAttribute(KEY_DELEGATE, del);
			}
			return del;
		}
		
		Optional<ComponentModule> mod = ComponentModuleLocator.getDefault()
			.map(ComponentModuleLocator::getActiveModule);
		if(mod.isPresent()) {
			ResteasyProviderFactoryImpl del = (ResteasyProviderFactoryImpl)mod.get().getAttributes().get(KEY_DELEGATE);
			if(del == null) {
				del = new ResteasyProviderFactoryImpl();
				mod.get().getAttributes().put(KEY_DELEGATE, del);
			}
			return del;
		}
		
		return new ResteasyProviderFactoryImpl();
	}
}
