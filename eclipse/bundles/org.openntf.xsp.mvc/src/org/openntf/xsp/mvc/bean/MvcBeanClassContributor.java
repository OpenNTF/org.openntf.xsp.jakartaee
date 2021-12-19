package org.openntf.xsp.mvc.bean;

import java.util.Arrays;
import java.util.Collection;

import org.eclipse.krazo.cdi.KrazoCdiExtension;
import org.eclipse.krazo.jaxrs.PostMatchingRequestFilter;
import org.eclipse.krazo.jaxrs.PreMatchingRequestFilter;
import org.openntf.xsp.cdi.discovery.WeldBeanClassContributor;

import jakarta.enterprise.inject.spi.Extension;

public class MvcBeanClassContributor implements WeldBeanClassContributor {

	@Override
	public Collection<Class<?>> getBeanClasses() {
		return Arrays.asList(
			DominoHttpContextBean.class,
			
			PreMatchingRequestFilter.class,
			PostMatchingRequestFilter.class
		);
	}

	@Override
	public Collection<Extension> getExtensions() {
		return Arrays.asList(new KrazoCdiExtension());
	}

}
