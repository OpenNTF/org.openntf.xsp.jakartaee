package org.openntf.xsp.microprofile.openapi;

import java.util.Collection;
import java.util.Collections;

import org.openntf.xsp.jaxrs.JAXRSClassContributor;

public class OpenAPIResourceContributor implements JAXRSClassContributor {

	@Override
	public Collection<Class<?>> getClasses() {
		return Collections.singleton(OpenAPIResource.class);
	}

}
