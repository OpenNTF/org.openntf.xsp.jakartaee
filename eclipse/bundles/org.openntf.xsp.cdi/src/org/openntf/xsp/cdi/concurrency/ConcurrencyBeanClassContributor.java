package org.openntf.xsp.cdi.concurrency;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.openntf.xsp.cdi.discovery.WeldBeanClassContributor;

import jakarta.enterprise.inject.spi.Extension;

/**
 * @author Jesse Gallagher
 * @since 2.7.0
 */
public class ConcurrencyBeanClassContributor implements WeldBeanClassContributor {

	@Override
	public Collection<Class<?>> getBeanClasses() {
		return Arrays.asList(ConcurrencyBean.class);
	}

	@Override
	public Collection<Extension> getExtensions() {
		return Collections.emptyList();
	}

}
