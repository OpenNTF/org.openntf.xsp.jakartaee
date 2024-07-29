package org.openntf.xsp.jakarta.el.impl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.faces.el.EvaluationException;
import javax.faces.el.PropertyNotFoundException;
import javax.faces.el.PropertyResolver;

import com.ibm.xsp.el.PropertyResolverImpl;

/**
 * @since 3.1.0
 */
public class RecordPropertyResolver extends PropertyResolver {
	public static final RecordPropertyResolver INSTANCE = new RecordPropertyResolver();
	private final PropertyResolverImpl delegate = new PropertyResolverImpl();

	@SuppressWarnings({"removal", "deprecation"})
	@Override
	public Object getValue(Object base, Object property)
			throws EvaluationException, PropertyNotFoundException {
		if(base == null) {
			return null;
		}
		if(!base.getClass().isRecord()) {
			return null;
		}
		
		Objects.requireNonNull(property);
		AtomicBoolean resolved = new AtomicBoolean(false);
		Object result = AccessController.doPrivileged((PrivilegedAction<Object>)() -> {
			try {
				Method m = base.getClass().getMethod(property.toString());
				resolved.set(true);
				return m.invoke(base);
			} catch (NoSuchMethodException e) {
				return null;
			} catch (SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				throw new RuntimeException(e);
			}
		});
		
		// Handle custom methods like normal beans
		if(!resolved.get()) {
			return delegate.getValue(base, property);
		} else {
			return result;
		}
	}

	@Override
	public Object getValue(Object base, int index) throws EvaluationException, PropertyNotFoundException {
		throw new PropertyNotFoundException();
	}

	@Override
	public void setValue(Object base, Object property, Object value)
			throws EvaluationException, PropertyNotFoundException {
		throw new EvaluationException("Cannot set values on records");
	}

	@Override
	public void setValue(Object base, int index, Object value)
			throws EvaluationException, PropertyNotFoundException {
		throw new PropertyNotFoundException();
	}

	@Override
	public boolean isReadOnly(Object base, Object property)
			throws EvaluationException, PropertyNotFoundException {
		return true;
	}

	@Override
	public boolean isReadOnly(Object base, int index) throws EvaluationException, PropertyNotFoundException {
		return true;
	}

	@SuppressWarnings({"removal", "deprecation", "rawtypes"})
	@Override
	public Class getType(Object base, Object property)
			throws EvaluationException, PropertyNotFoundException {
		Objects.requireNonNull(base);
		if(!base.getClass().isRecord()) {
			return null;
		}
		
		Objects.requireNonNull(property);
		AtomicBoolean resolved = new AtomicBoolean(false);
		Class<?> result = AccessController.doPrivileged((PrivilegedAction<Class<?>>)() -> {
			try {
				Method m = base.getClass().getMethod(property.toString());
				resolved.set(true);
				return m.getReturnType();
			} catch (NoSuchMethodException e) {
				return null;
			} catch (SecurityException e) {
				throw new RuntimeException(e);
			}
		});
		
		// Handle custom methods like normal beans
		if(!resolved.get()) {
			return delegate.getType(base, property);
		} else {
			return result;
		}
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Class getType(Object base, int index) throws EvaluationException, PropertyNotFoundException {
		throw new PropertyNotFoundException();
	}

}
