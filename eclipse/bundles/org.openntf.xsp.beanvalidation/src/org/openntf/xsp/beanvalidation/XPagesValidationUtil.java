/**
 * Copyright © 2018-2020 Jesse Gallagher
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
package org.openntf.xsp.beanvalidation;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Arrays;
import java.util.Locale;
import java.util.Set;

import javax.faces.context.FacesContext;
import javax.validation.ConstraintViolation;
import javax.validation.MessageInterpolator;
import javax.validation.Validation;
import javax.validation.Validator;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.messageinterpolation.ResourceBundleMessageInterpolator;

import com.ibm.xsp.designer.context.XSPContext;

public enum XPagesValidationUtil {
	;
	
	/**
	 * Constructs a new {@link Validator} instance that uses the locate settings from
	 * the current XPages environment.
	 * 
	 * @return a new {@link Validator} instance for XPages use
	 */
	public static Validator constructXPagesValidator() {
		return AccessController.doPrivileged((PrivilegedAction<Validator>)() ->
			Validation.byDefaultProvider()
				.providerResolver(() -> Arrays.asList(new HibernateValidator()))
				.configure()
				.messageInterpolator(new XSPLocaleResourceBundleMessageInterpolator())
				.buildValidatorFactory().getValidator()
		);
	}
	
	/**
	 * Constructs a new {@link Validator} instance that uses default locale settings.
	 * 
	 * @return a new {@link Validator} instance for generic use
	 * @since 1.2.0
	 */
	public static Validator constructGenericValidator() {
		return AccessController.doPrivileged((PrivilegedAction<Validator>)() ->
			Validation.byDefaultProvider()
				.providerResolver(() -> Arrays.asList(new HibernateValidator()))
				.configure()
				.buildValidatorFactory().getValidator()
		);
	}
	
	/**
	 * Validates a bean using the default validator for the current XPages environment.
	 * 
	 * @param <T> the class of the bean to validate
	 * @param bean the bean object to validate
	 * @return a {@link Set} of {@link ConstraintViolation} objects for any validation failures
	 * @since 1.2.0
	 */
	public static <T> Set<ConstraintViolation<T>> validateBean(T bean) {
		return validateBean(bean, constructXPagesValidator());
	}
	
	/**
	 * Validates a bean using the provided {@link Validator}.
	 * 
	 * @param <T> the class of the bean to validate
	 * @param bean the bean object to validate
	 * @param validator the {@link Validator} instance to use when validating
	 * @return a {@link Set} of {@link ConstraintViolation} objects for any validation failures
	 */
	public static <T> Set<ConstraintViolation<T>> validateBean(T bean, Validator validator) {
		return AccessController.doPrivileged((PrivilegedAction<Set<ConstraintViolation<T>>>) () -> {
			// Juggling the ClassLoader avoids a problem where the XPages ClassLoader can't
			// find the com.sun.el classes privately in this plugin
			ClassLoader loader = Thread.currentThread().getContextClassLoader();
			Thread.currentThread().setContextClassLoader(XPagesValidationUtil.class.getClassLoader());
			try {
				return validator.validate(bean);
			} finally {
				Thread.currentThread().setContextClassLoader(loader);
			}
		});
	}
	
	private static class XSPLocaleResourceBundleMessageInterpolator extends ResourceBundleMessageInterpolator {
		@Override
		public String interpolate(final String message, final MessageInterpolator.Context context) {
			XSPContext xspContext = XSPContext.getXSPContext(FacesContext.getCurrentInstance());
			Locale locale = xspContext.getLocale();
			return interpolate(message, context, locale);
		}
	}
}
