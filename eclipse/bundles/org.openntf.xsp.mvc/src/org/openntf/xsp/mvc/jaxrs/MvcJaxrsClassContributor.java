package org.openntf.xsp.mvc.jaxrs;

import java.util.Arrays;
import java.util.Collection;

import org.eclipse.krazo.binding.convert.MvcConverterProvider;
import org.eclipse.krazo.core.ViewResponseFilter;
import org.eclipse.krazo.core.ViewableWriter;
import org.eclipse.krazo.jaxrs.PostMatchingRequestFilter;
import org.eclipse.krazo.jaxrs.PreMatchingRequestFilter;
import org.openntf.xsp.jaxrs.JAXRSClassContributor;

public class MvcJaxrsClassContributor implements JAXRSClassContributor {

	@Override
	public Collection<Class<?>> getClasses() {
		return Arrays.asList(
			PreMatchingRequestFilter.class,
			PostMatchingRequestFilter.class,
			ViewableWriter.class,
			ViewResponseFilter.class,
			MvcConverterProvider.class
		);
	}

}
