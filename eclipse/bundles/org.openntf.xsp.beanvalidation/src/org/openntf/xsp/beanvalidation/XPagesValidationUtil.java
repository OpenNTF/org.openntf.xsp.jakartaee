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
	
	public static Validator constructXPagesValidator() {
		return Validation.byDefaultProvider()
				.providerResolver(() -> Arrays.asList(new HibernateValidator()))
				.configure()
				.messageInterpolator(new XSPLocaleResourceBundleMessageInterpolator())
				.buildValidatorFactory().getValidator();
	}
	
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
