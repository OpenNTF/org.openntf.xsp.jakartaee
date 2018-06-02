package org.openntf.xsp.cdi.impl;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Named;

import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.resources.spi.ResourceLoader;
import org.jboss.weld.util.ForwardingBeanManager;
import org.openntf.xsp.cdi.CDILibrary;
import com.ibm.domino.xsp.module.nsf.ModuleClassLoader;
import com.ibm.domino.xsp.module.nsf.NSFComponentModule;
import com.ibm.domino.xsp.module.nsf.NotesContext;
import com.ibm.xsp.application.ApplicationEx;
import com.ibm.xsp.application.events.ApplicationListener2;

/**
 * Manages the lifecycle of the app's associated Weld instance.
 * 
 * @author Jesse Gallagher
 * @since 1.0.0
 */
public class WeldApplicationListener implements ApplicationListener2 {
	
	public static WeldContainer getContainer(ApplicationEx application) {
		return WeldContainer.instance(application.getApplicationId());
	}
	public static BeanManagerImpl getBeanManager(ApplicationEx application) {
		WeldContainer container = getContainer(application);
		BeanManager manager = container.getBeanManager();
		if(manager instanceof BeanManagerImpl) {
			return (BeanManagerImpl)manager;
		} else if(manager instanceof ForwardingBeanManager) {
			return (BeanManagerImpl) ((ForwardingBeanManager)manager).delegate();
		} else {
			throw new IllegalStateException("Cannot find BeanManagerImpl in " + manager);
		}
	}

	private static final String PREFIX_CLASSES = "WEB-INF/classes/"; //$NON-NLS-1$
	private static final String SUFFIX_CLASS = ".class"; //$NON-NLS-1$
	private static final Pattern IGNORE_CLASSES = Pattern.compile("^(xsp|plugin)\\..*$"); //$NON-NLS-1$
	
	// *******************************************************************************
	// * ApplicationListener2 methods
	// *******************************************************************************

	@Override
	public void applicationCreated(ApplicationEx application) {
		if(CDILibrary.usesLibrary(application)) {
			Weld weld = new Weld()
				.disableDiscovery()
				.containerId(application.getApplicationId())
				// Disable concurrent deployment to avoid Notes thread init trouble
				.property("org.jboss.weld.bootstrap.concurrentDeployment", "false"); //$NON-NLS-1$ //$NON-NLS-2$

			NotesContext context = NotesContext.getCurrent();
			NSFComponentModule module = context.getModule();

			weld.setResourceLoader(new ModuleResourceLoader(module));
			
			ModuleClassLoader classLoader = (ModuleClassLoader)module.getModuleClassLoader();
			weld.setClassLoader(classLoader);
			
			// TODO see if the ResourceLoader can be configured to allow auto-discovery
			module.getRuntimeFileSystem().getAllResources().entrySet().stream()
				.map(Map.Entry::getKey)
				.filter(key -> key.startsWith(PREFIX_CLASSES) && key.endsWith(SUFFIX_CLASS))
				.map(key -> key.substring(PREFIX_CLASSES.length(), key.length()-SUFFIX_CLASS.length()))
				.map(key -> key.replace('/', '.'))
				.filter(className -> !IGNORE_CLASSES.matcher(className).matches())
				.map(className -> loadClass(classLoader, className))
				.filter(WeldApplicationListener::isBeanClass)
				.forEach(weld::addBeanClass);
			
			weld.initialize();
		}
	}

	@Override
	public void applicationDestroyed(ApplicationEx application) {
		if(CDILibrary.usesLibrary(application)) {
			getContainer(application).shutdown();
		}
	}

	@Override
	public void applicationRefreshed(ApplicationEx application) {
		if(CDILibrary.usesLibrary(application)) {
			applicationDestroyed(application);
			applicationCreated(application);
		}
	}

	// *******************************************************************************
	// * Internal utility methods
	// *******************************************************************************
	
	private static class ModuleResourceLoader implements ResourceLoader {
		private final NSFComponentModule module;
		
		public ModuleResourceLoader(NSFComponentModule module) {
			this.module = module;
		}

		@Override
		public Class<?> classForName(String name) {
			try {
				return module.getModuleClassLoader().loadClass(name);
			} catch (ClassNotFoundException e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public URL getResource(String name) {
			try {
				return module.getResource(name);
			} catch (MalformedURLException e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public Collection<URL> getResources(String name) {
			try {
				return Collections.list(module.getModuleClassLoader().getResources(name));
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		
		@Override
		public void cleanup() {
			// NOP
		}
		
	}
	
	private static Class<?> loadClass(ClassLoader cl, String className) {
		try {
			return cl.loadClass(className);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}
	
	private static final List<Class<? extends Annotation>> SCOPE_ANNOTATIONS = Arrays.asList(
		ApplicationScoped.class,
		SessionScoped.class,
		RequestScoped.class
	);
	
	private static boolean isBeanClass(Class<?> clazz) {
		if(!SCOPE_ANNOTATIONS.stream().anyMatch(a -> clazz.getAnnotation(a) != null)) {
			return false;
		}
		if(clazz.getAnnotation(Named.class) == null) {
			return false;
		}
		return true;
	}
}
