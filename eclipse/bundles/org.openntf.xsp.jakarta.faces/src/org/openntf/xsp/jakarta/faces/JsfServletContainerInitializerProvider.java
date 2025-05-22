package org.openntf.xsp.jakarta.faces;

import java.util.Collection;
import java.util.Collections;

import com.ibm.designer.runtime.domino.adapter.ComponentModule;

import org.apache.myfaces.webapp.MyFacesContainerInitializer;
import org.openntf.xsp.jakartaee.module.ServletContainerInitializerProvider;
import org.openntf.xsp.jakartaee.util.LibraryUtil;

import jakarta.servlet.ServletContainerInitializer;

/**
 * @since 3.4.0
 */
public class JsfServletContainerInitializerProvider implements ServletContainerInitializerProvider {

	@Override
	public Collection<ServletContainerInitializer> provide(ComponentModule module) {
		if(LibraryUtil.usesLibrary(LibraryUtil.LIBRARY_UI, module)) {
			return Collections.singleton(new MyFacesContainerInitializer());
		} else {
			return Collections.emptyList();
		}
	}

}
