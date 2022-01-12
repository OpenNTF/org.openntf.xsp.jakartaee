package org.openntf.xsp.mvc.jaxrs;

import java.util.Collection;
import java.util.Collections;

import org.openntf.xsp.jaxrs.JAXRSClassContributor;

public class MvcJaxrsClassContributor implements JAXRSClassContributor {

	
	@Override
	public Collection<Class<?>> getClasses() {
		return Collections.singleton(MvcGenericThrowableMapper.class);
	}
}
