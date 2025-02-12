/**
 * Copyright (c) 2018-2025 Contributors to the XPages Jakarta EE Support Project
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
package org.openntf.xsp.jakarta.el.impl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.text.MessageFormat;
import java.util.Objects;

import jakarta.el.BeanELResolver;
import jakarta.el.ELContext;
import jakarta.el.ELResolver;
import jakarta.el.PropertyNotWritableException;

public class RecordPropertyELResolver extends ELResolver {

	private final BeanELResolver delegate = new BeanELResolver();

	@SuppressWarnings({"removal", "deprecation"})
	@Override
	public Object getValue(final ELContext context, final Object base, final Object property) {
		if(base == null) {
			return null;
		}
		if(!base.getClass().isRecord()) {
			return null;
		}

		Objects.requireNonNull(property);
		Object result = AccessController.doPrivileged((PrivilegedAction<Object>)() -> {
			try {
				Method m = base.getClass().getMethod(property.toString());
				context.setPropertyResolved(true);
				return m.invoke(base);
			} catch (NoSuchMethodException e) {
				return null;
			} catch (SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				throw new RuntimeException(e);
			}
		});

		// Handle custom methods like normal beans
		if(!context.isPropertyResolved()) {
			return delegate.getValue(context, base, property);
		} else {
			return result;
		}
	}

	@SuppressWarnings({"removal", "deprecation"})
	@Override
	public Class<?> getType(final ELContext context, final Object base, final Object property) {
		Objects.requireNonNull(base);
		if(!base.getClass().isRecord()) {
			return null;
		}

		Objects.requireNonNull(property);
		Class<?> result = AccessController.doPrivileged((PrivilegedAction<Class<?>>)() -> {
			try {
				Method m = base.getClass().getMethod(property.toString());
				context.setPropertyResolved(true);
				return m.getReturnType();
			} catch (NoSuchMethodException e) {
				return null;
			} catch (SecurityException e) {
				throw new RuntimeException(e);
			}
		});

		// Handle custom methods like normal beans
		if(!context.isPropertyResolved()) {
			return delegate.getType(context, base, property);
		} else {
			return result;
		}
	}

	@Override
	public void setValue(final ELContext context, final Object base, final Object property, final Object value) {
		if(value != null && value.getClass().isRecord()) {
			throw new PropertyNotWritableException(MessageFormat.format("Cannot write a property on a record (property={0}, type={1})", property, base.getClass().getName()));
		}
	}

	@Override
	public boolean isReadOnly(final ELContext context, final Object base, final Object property) {
		return true;
	}

	@Override
	public Class<?> getCommonPropertyType(final ELContext context, final Object base) {
		if (base == null) {
            return null;
        }

        return Object.class;
	}

}
