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
	public <T> Set<ConstraintViolation<T>> validate(T object, Class<?>... groups) {
		return delegate().validate(object, groups);
	}

	@Override
	public <T> Set<ConstraintViolation<T>> validateProperty(T object, String propertyName, Class<?>... groups) {
		return delegate().validateProperty(object, propertyName, groups);
	}

	@Override
	public <T> Set<ConstraintViolation<T>> validateValue(Class<T> beanType, String propertyName, Object value,
			Class<?>... groups) {
		return delegate().validateValue(beanType, propertyName, value, groups);
	}

	@Override
	public BeanDescriptor getConstraintsForClass(Class<?> clazz) {
		return delegate().getConstraintsForClass(clazz);
	}

	@Override
	public <T> T unwrap(Class<T> type) {
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
