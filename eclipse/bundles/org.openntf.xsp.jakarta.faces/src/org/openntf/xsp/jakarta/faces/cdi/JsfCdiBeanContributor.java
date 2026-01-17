/**
 * Copyright (c) 2018-2026 Contributors to the XPages Jakarta EE Support Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openntf.xsp.jakarta.faces.cdi;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.openntf.xsp.jakarta.cdi.discovery.CDIClassContributor;
import org.openntf.xsp.jakartaee.util.LibraryUtil;

import jakarta.enterprise.inject.spi.Extension;

/**
 * @author Jesse Gallagher
 * @since 2.4.0
 */
public class JsfCdiBeanContributor implements CDIClassContributor {
	@Override
	public Collection<Class<?>> getBeanClasses() {
		if(LibraryUtil.isLibraryActive(LibraryUtil.LIBRARY_UI)) {
			return Collections.singleton(FacesConfigBean.class);
		}
		return null;
	}

	@Override
	public Collection<Extension> getExtensions() {
		if(LibraryUtil.isLibraryActive(LibraryUtil.LIBRARY_UI)) {
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
				new org.apache.myfaces.push.cdi.PushCDIExtension(),
				new org.apache.myfaces.flow.cdi.FlowScopeExtension(),
				new org.apache.myfaces.cdi.clientwindow.ClientWindowScopeExtension()
			);
		} else {
			return null;
		}
	}

}
