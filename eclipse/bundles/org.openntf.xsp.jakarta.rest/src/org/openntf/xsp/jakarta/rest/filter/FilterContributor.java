package org.openntf.xsp.jakarta.rest.filter;

import java.util.Collection;
import java.util.Set;

import org.jboss.resteasy.plugins.interceptors.GZIPEncodingInterceptor;
import org.openntf.xsp.jakarta.rest.RestClassContributor;

/**
 * @since 3.7.0
 */
public class FilterContributor implements RestClassContributor {

	@Override
	public Collection<Class<?>> getClasses() {
		return Set.of(
			CompressorResponseFilter.class,
			GZIPEncodingInterceptor.class
		);
	}

}
