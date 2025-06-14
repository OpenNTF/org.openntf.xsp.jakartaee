package org.openntf.xsp.jakarta.mvc.rest;

import java.util.Collection;
import java.util.Set;

import org.openntf.xsp.jakarta.rest.RestClassContributor;

/**
 * @since 3.5.0
 */
public class MvcRestClassContributor implements RestClassContributor {

	@Override
	public Collection<Class<?>> getClasses() {
		return Set.of(DominoCsrfExceptionMapper.class);
	}

}
