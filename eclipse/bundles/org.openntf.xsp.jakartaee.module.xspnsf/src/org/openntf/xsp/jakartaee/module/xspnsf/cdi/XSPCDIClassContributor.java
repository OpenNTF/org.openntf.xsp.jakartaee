package org.openntf.xsp.jakartaee.module.xspnsf.cdi;

import java.util.Collection;
import java.util.Collections;

import org.openntf.xsp.jakarta.cdi.discovery.CDIClassContributor;

/**
 * @since 3.4.0
 */
public class XSPCDIClassContributor implements CDIClassContributor {
	@Override
	public Collection<Class<?>> getBeanClasses() {
		return Collections.singleton(DominoFacesImplicitObjectProvider.class);
	}
}
