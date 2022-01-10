package org.openntf.xsp.jsonapi.jaxrs.exceptions;

import java.util.Arrays;
import java.util.Collection;

import org.openntf.xsp.jaxrs.JAXRSClassContributor;

/**
 * @author Jesse Gallagher
 * @since 2.2.0
 */
public class ExceptionMapperContributor implements JAXRSClassContributor {

	@Override
	public Collection<Class<?>> getClasses() {
		return Arrays.asList(
			GenericThrowableMapper.class,
			NotFoundMapper.class
		);
	}

}
