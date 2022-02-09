package org.openntf.xsp.jsf.cdi;

import java.util.Arrays;
import java.util.Collection;

import org.openntf.xsp.cdi.discovery.WeldBeanClassContributor;
import org.openntf.xsp.jakartaee.util.LibraryUtil;
import org.openntf.xsp.jsf.JsfLibrary;

import jakarta.enterprise.inject.spi.Extension;

public class JsfCdiBeanContributor implements WeldBeanClassContributor {
	@Override
	public Collection<Class<?>> getBeanClasses() {
		return null;
	}

	@Override
	public Collection<Extension> getExtensions() {
		if(LibraryUtil.isLibraryActive(JsfLibrary.LIBRARY_ID)) {
			return Arrays.asList(
				new org.apache.myfaces.cdi.FacesScopeExtension(),
				new org.apache.myfaces.cdi.FacesArtifactProducerExtension(),
				new org.apache.myfaces.cdi.FacesApplicationArtifactHolderExtension(),
				new org.apache.myfaces.cdi.config.FacesConfigExtension(),
				new org.apache.myfaces.cdi.managedproperty.ManagedPropertyExtension(),
				new org.apache.myfaces.cdi.model.FacesDataModelExtension(),
				new org.apache.myfaces.cdi.view.ViewScopeExtension(),
				new org.apache.myfaces.cdi.view.ViewTransientScopeExtension(),
				new org.apache.myfaces.config.annotation.CdiAnnotationProviderExtension(),
				new org.apache.myfaces.push.cdi.PushContextCDIExtension(),
				new org.apache.myfaces.flow.cdi.FlowScopeExtension(),
				new org.apache.myfaces.cdi.clientwindow.ClientWindowScopeExtension()
			);
		} else {
			return null;
		}
	}

}
