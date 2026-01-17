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
package org.openntf.xsp.jakarta.validation.jndi;

import java.util.Set;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.validation.executable.ExecutableValidator;
import jakarta.validation.metadata.BeanDescriptor;

public class DelegatingValidator implements Validator {

	@Override
	public <T> Set<ConstraintViolation<T>> validate(final T object, final Class<?>... groups) {
		return delegate().validate(object, groups);
	}

	@Override
	public <T> Set<ConstraintViolation<T>> validateProperty(final T object, final String propertyName, final Class<?>... groups) {
		return delegate().validateProperty(object, propertyName, groups);
	}

	@Override
	public <T> Set<ConstraintViolation<T>> validateValue(final Class<T> beanType, final String propertyName, final Object value,
			final Class<?>... groups) {
		return delegate().validateValue(beanType, propertyName, value, groups);
	}

	@Override
	public BeanDescriptor getConstraintsForClass(final Class<?> clazz) {
		return delegate().getConstraintsForClass(clazz);
	}

	@Override
	public <T> T unwrap(final Class<T> type) {
		return delegate().unwrap(type);
	}

	@Override
	public ExecutableValidator forExecutables() {
		// TODO Auto-generated method stub
		return null;
	}

	private Validator delegate() {
		// TODO see if there's a good way to not re-resolve this every time
		try {
			InitialContext jndi = new InitialContext();
			return (Validator)jndi.lookup("java:comp/Validator"); //$NON-NLS-1$
		} catch (NamingException e) {
			throw new RuntimeException(e);
		}
	}
}
