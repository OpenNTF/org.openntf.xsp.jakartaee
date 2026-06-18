package org.openntf.xsp.jakarta.validation.jndi;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.spi.ObjectFactory;

import org.openntf.xsp.jakarta.validation.XPagesValidationUtil;

/**
 * @since 3.7.0
 */
public class JndiValidatorFactory implements ObjectFactory {

	@Override
	public Object getObjectInstance(Object obj, Name name, Context nameCtx, Hashtable<?, ?> environment)
			throws Exception {
		switch(String.valueOf(name)) {
		case XPagesValidationUtil.JNDI_VALIDATORFACTORY:
			return XPagesValidationUtil.constructXPagesValidatorFactory();
		case XPagesValidationUtil.JNDI_VALIDATOR:
			return DelegatingValidator.INSTANCE;
		default:
			return null;
		}
	}

}
