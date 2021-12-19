package org.openntf.xsp.mvc.bean;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.openntf.xsp.cdi.discovery.WeldBeanClassContributor;

import jakarta.enterprise.inject.spi.Extension;

public class MvcBeanClassContributor implements WeldBeanClassContributor {

	@Override
	public Collection<Class<?>> getBeanClasses() {
		return Arrays.asList(DominoHttpContextBean.class);
	}

	@Override
	public Collection<Extension> getExtensions() {
		return Collections.emptySet();
	}

}
