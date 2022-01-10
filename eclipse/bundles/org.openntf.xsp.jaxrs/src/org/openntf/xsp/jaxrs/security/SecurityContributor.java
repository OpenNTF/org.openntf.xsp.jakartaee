package org.openntf.xsp.jaxrs.security;

import java.util.Arrays;
import java.util.Collection;

import org.openntf.xsp.jaxrs.JAXRSClassContributor;

public class SecurityContributor implements JAXRSClassContributor {

	@Override
	public Collection<Class<?>> getClasses() {
		return Arrays.asList(
			SecurityRequestFilter.class,
			RolesAllowedFilter.class
		);
	}

}
