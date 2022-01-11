package org.openntf.xsp.microprofile.health;

import java.util.Collection;
import java.util.Collections;

import org.openntf.xsp.jakartaee.LibraryUtil;
import org.openntf.xsp.jaxrs.JAXRSClassContributor;

import com.ibm.xsp.application.ApplicationEx;

public class HealthResourceContributor implements JAXRSClassContributor {

	@Override
	public Collection<Class<?>> getClasses() {
		ApplicationEx app = ApplicationEx.getInstance();
		if(app != null && LibraryUtil.usesLibrary(HealthLibrary.LIBRARY_ID, app)) {
			return Collections.singleton(HealthResource.class);
		} else {
			return Collections.emptyList();
		}
	}

}
