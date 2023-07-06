package org.openntf.xsp.cdi.extlib;

import java.util.Collection;
import java.util.Collections;

import org.openntf.xsp.cdi.discovery.WeldBeanClassContributor;
import org.openntf.xsp.jakartaee.util.LibraryUtil;

/**
 * Contributes known XPages Extension Library beans when the ExtLib
 * is active for the current application.
 * 
 * @since 2.13.0
 */
public class ExtLibBeanContributor implements WeldBeanClassContributor {

	@Override
	public Collection<Class<?>> getBeanClasses() {
		if(LibraryUtil.isLibraryActive("com.ibm.xsp.extlib.library")) { //$NON-NLS-1$
			return Collections.singleton(ExtLibBeanProvider.class);
		} else {
			return Collections.emptySet();
		}
	}

}
