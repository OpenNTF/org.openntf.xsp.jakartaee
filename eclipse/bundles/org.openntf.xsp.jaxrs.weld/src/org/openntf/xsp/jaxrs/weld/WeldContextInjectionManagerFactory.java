package org.openntf.xsp.jaxrs.weld;

import org.glassfish.jersey.internal.inject.InjectionManager;
import org.glassfish.jersey.internal.inject.InjectionManagerFactory;

public class WeldContextInjectionManagerFactory implements InjectionManagerFactory {

	@Override
	public InjectionManager create(Object parent) {
		return WeldContextInjectionManager.instance;
	}

}
