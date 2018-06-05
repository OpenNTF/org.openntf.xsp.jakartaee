package org.openntf.xsp.jaxrs.weld;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;

import org.glassfish.jersey.inject.cdi.se.CdiSeInjectionManager;
import org.glassfish.jersey.inject.cdi.se.injector.ContextInjectionResolverImpl;
import org.glassfish.jersey.internal.inject.AbstractBinder;
import org.glassfish.jersey.internal.inject.Binder;
import org.glassfish.jersey.internal.inject.Binding;
import org.glassfish.jersey.internal.inject.Bindings;
import org.glassfish.jersey.internal.inject.ForeignDescriptor;
import org.glassfish.jersey.internal.inject.InjectionManager;
import org.glassfish.jersey.internal.inject.ServiceHolder;
import org.jboss.weld.environment.se.WeldContainer;
import org.openntf.xsp.cdi.util.ContainerUtil;

import com.ibm.xsp.application.ApplicationEx;

public class WeldContextInjectionManager extends CdiSeInjectionManager {
	
	public static final WeldContextInjectionManager instance = new WeldContextInjectionManager();

	@Override
	public void completeRegistration() throws IllegalStateException {
		ensureContainerInit();
	}
	
	@Override
	public <T> T getInstance(Class<T> contractOrImpl) {
		ensureContainerInit();
		return super.getInstance(contractOrImpl);
	}
	@Override
	public <T> T getInstance(Class<T> contractOrImpl, Annotation... qualifiers) {
		ensureContainerInit();
		return super.getInstance(contractOrImpl, qualifiers);
	}
	@Override
	public <T> T getInstance(Class<T> contractOrImpl, String classAnalyzer) {
		ensureContainerInit();
		return super.getInstance(contractOrImpl, classAnalyzer);
	}
	@Override
	public Object getInstance(ForeignDescriptor foreignDescriptor) {
		ensureContainerInit();
		return super.getInstance(foreignDescriptor);
	}
	@Override
	public <T> T getInstance(Type contractOrImpl) {
		ensureContainerInit();
		return super.getInstance(contractOrImpl);
	}
	@Override
	public <T> List<T> getAllInstances(Type contractOrImpl) {
		ensureContainerInit();
		return super.getAllInstances(contractOrImpl);
	}
	
	@Override
	public <T> T createAndInitialize(Class<T> createMe) {
		ensureContainerInit();
		return super.createAndInitialize(createMe);
	}
	
//	@Override
//	public boolean isRegistrable(Class<?> clazz) {
//		ensureContainerInit();
//		return true;
//	}
	
	@Override
	public void inject(Object injectMe, String classAnalyzer) {
		ensureContainerInit();
		super.inject(injectMe, classAnalyzer);
	}
	@Override
	public void inject(Object instance) {
		ensureContainerInit();
		super.inject(instance);
	}
	
//	@Override
//	public void register(Object provider) throws IllegalArgumentException {
//		ensureContainerInit();
//		getBindings().bind(provider);
//	}
	
	@Override
	public ForeignDescriptor createForeignDescriptor(Binding binding) {
		ensureContainerInit();
		return super.createForeignDescriptor(binding);
	}
	
	@Override
	public <T> List<ServiceHolder<T>> getAllServiceHolders(Class<T> contractOrImpl, Annotation... qualifiers) {
		ensureContainerInit();
		return super.getAllServiceHolders(contractOrImpl, qualifiers);
	}
	
	@Override
	public void register(Binder binder) {
		ensureContainerInit();
		super.register(binder);
	}
	@Override
	public void register(Binding binding) {
		ensureContainerInit();
		super.register(binding);
	}
	
	@Override
	public void preDestroy(Object preDestroyMe) {
		ensureContainerInit();
		super.preDestroy(preDestroyMe);
	}
	
	@Override
	public void register(Iterable<Binding> bindings) {
		ensureContainerInit();
		super.register(bindings);
	}
	
	@Override
	public void shutdown() {
		ensureContainerInit();
		super.shutdown();
	}
	
	@Override
	public AbstractBinder getBindings() {
		return super.getBindings();
	}
	
	private void ensureContainerInit() {
		if(getContainer() == null) {
			getBindings().bind(Bindings.service(this).to(InjectionManager.class));
			getBindings().install(new ContextInjectionResolverImpl.Binder(this::getBeanManager));
			
			ApplicationEx application = ApplicationEx.getInstance();
			WeldContainer container = ContainerUtil.getContainer(application);
			
	        setContainer(container);
	        setBeanManager(container.getBeanManager());
		}
	}
	
}
