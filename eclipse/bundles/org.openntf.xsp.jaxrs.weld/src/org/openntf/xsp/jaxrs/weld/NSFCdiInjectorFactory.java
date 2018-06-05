package org.openntf.xsp.jaxrs.weld;

import javax.enterprise.inject.spi.BeanManager;

import org.jboss.resteasy.cdi.CdiInjectorFactory;
import org.openntf.xsp.cdi.util.ContainerUtil;

import com.ibm.xsp.application.ApplicationEx;

public class NSFCdiInjectorFactory extends CdiInjectorFactory {
	public NSFCdiInjectorFactory() {
		
	}
	
	@Override
	protected BeanManager lookupBeanManager() {
		ApplicationEx application = ApplicationEx.getInstance();
		if(application == null) {
			throw new IllegalStateException("Unable to locate ApplicationEx!");
		}
		return ContainerUtil.getBeanManager(application);
	}
}
