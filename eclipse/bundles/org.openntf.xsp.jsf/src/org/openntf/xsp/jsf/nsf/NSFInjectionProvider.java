package org.openntf.xsp.jsf.nsf;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Arrays;
import java.util.stream.Stream;

import com.sun.faces.spi.DiscoverableInjectionProvider;
import com.sun.faces.spi.InjectionProviderException;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.inject.Inject;

public class NSFInjectionProvider extends DiscoverableInjectionProvider {

	@Override
	public void inject(Object managedBean) throws InjectionProviderException {
		if(managedBean != null) {
			AccessController.doPrivileged((PrivilegedAction<Void>)() -> {
				Class<?> c = managedBean.getClass();
				while(c != null) {
					Arrays.stream(c.getDeclaredFields())
						.filter(f -> f.isAnnotationPresent(Inject.class))
						.peek(f -> f.setAccessible(true))
						.forEach(f -> {
							Annotation[] annotations = Stream.of(f.getAnnotations())
								.filter(t -> !Inject.class.equals(t.annotationType()))
								.toArray(Annotation[]::new);
							Object val = CDI.current().select(f.getType(), annotations);
							try {
								f.set(managedBean, val);
							} catch (IllegalArgumentException | IllegalAccessException e) {
								throw new RuntimeException(e);
							}
						});
					
					c = c.getSuperclass();
				}
				
				return null;
			});
		}
	}

	@Override
	public void invokePostConstruct(Object managedBean) throws InjectionProviderException {
		if(managedBean != null) {
			AccessController.doPrivileged((PrivilegedAction<Void>)() -> {
				Class<?> c = managedBean.getClass();
				while(c != null) {
					Arrays.stream(c.getDeclaredMethods())
						.filter(m -> m.isAnnotationPresent(PostConstruct.class))
						.peek(m -> m.setAccessible(true))
						.peek(m -> System.out.println("Invoking postConstruct " + m))
						.forEach(m -> {
							try {
								m.invoke(managedBean);
							} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
								throw new RuntimeException(e);
							}
						});
					
					c = c.getSuperclass();
				}
				
				return null;
			});
		}
	}

	@Override
	public void invokePreDestroy(Object managedBean) throws InjectionProviderException {
		if(managedBean != null) {
			AccessController.doPrivileged((PrivilegedAction<Void>)() -> {
				Class<?> c = managedBean.getClass();
				while(c != null) {
					Arrays.stream(c.getDeclaredMethods())
						.filter(m -> m.isAnnotationPresent(PreDestroy.class))
						.peek(m -> m.setAccessible(true))
						.peek(m -> System.out.println("Invoking preDestroy " + m))
						.forEach(m -> {
							try {
								m.invoke(managedBean);
							} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
								throw new RuntimeException(e);
							}
						});
					
					c = c.getSuperclass();
				}
				
				return null;
			});
		}
	}

}
