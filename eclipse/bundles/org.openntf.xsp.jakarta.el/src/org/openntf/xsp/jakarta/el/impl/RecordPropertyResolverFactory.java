package org.openntf.xsp.jakarta.el.impl;

import javax.faces.el.PropertyResolver;

import com.ibm.xsp.el.PropertyResolverFactory;

/**
 * @since 3.1.0
 */
public class RecordPropertyResolverFactory implements PropertyResolverFactory {
	public static final RecordPropertyResolverFactory INSTANCE = new RecordPropertyResolverFactory();

	@Override
	public PropertyResolver getPropertyResolver(final Object base) {
		if(base != null && base.getClass().isRecord()) {
			return RecordPropertyResolver.INSTANCE;
		}
		return null;
	}

}
