package org.openntf.xsp.jakarta.security.cdi;

import java.util.Collection;
import java.util.Set;

import org.openntf.xsp.jakarta.cdi.discovery.CDIClassContributor;
import org.openntf.xsp.jakarta.security.jasapi.cdi.JavaSapiSecurityContext;

public class SecurityClassContributor implements CDIClassContributor {

	@Override
	public Collection<Class<?>> getBeanClasses() {
		return Set.of(JavaSapiSecurityContext.class);
	}

}
