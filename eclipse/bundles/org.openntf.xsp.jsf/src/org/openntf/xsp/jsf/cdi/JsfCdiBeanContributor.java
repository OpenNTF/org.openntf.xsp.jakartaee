package org.openntf.xsp.jsf.cdi;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

import org.openntf.xsp.cdi.discovery.WeldBeanClassContributor;
import org.openntf.xsp.jakartaee.util.LibraryUtil;
import org.openntf.xsp.jsf.JsfLibrary;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

import jakarta.enterprise.inject.spi.Extension;
import jakarta.faces.context.FacesContext;

public class JsfCdiBeanContributor implements WeldBeanClassContributor {
	private static final String[] EXTENSIONS = {
		"com.sun.faces.application.view.ViewScopeExtension", //$NON-NLS-1$
		"com.sun.faces.flow.FlowCDIExtension", //$NON-NLS-1$
		"com.sun.faces.flow.FlowDiscoveryCDIExtension", //$NON-NLS-1$
		"com.sun.faces.cdi.CdiExtension" //$NON-NLS-1$
	};

	@Override
	public Collection<Class<?>> getBeanClasses() {
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Collection<Extension> getExtensions() {
		if(LibraryUtil.isLibraryActive(JsfLibrary.LIBRARY_ID)) {
			Bundle b = FrameworkUtil.getBundle(FacesContext.class);
			return Arrays.stream(EXTENSIONS)
				.map(t -> {
					try {
						return (Class<Extension>)b.loadClass(t);
					} catch (ClassNotFoundException e) {
						throw new RuntimeException(e);
					}
				})
				.map(t -> {
					try {
						return t.newInstance();
					} catch (InstantiationException | IllegalAccessException e) {
						throw new RuntimeException(e);
					}
				})
				.collect(Collectors.toList());
		} else {
			return null;
		}
	}

}
