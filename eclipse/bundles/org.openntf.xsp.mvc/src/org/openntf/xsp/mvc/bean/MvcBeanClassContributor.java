/**
 * Copyright (c) 2018-2024 Contributors to the XPages Jakarta EE Support Project
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
package org.openntf.xsp.mvc.bean;

import java.util.Arrays;
import java.util.Collection;

import org.eclipse.krazo.cdi.KrazoCdiExtension;
import org.openntf.xsp.cdi.discovery.WeldBeanClassContributor;
import org.openntf.xsp.jakartaee.util.LibraryUtil;
import org.openntf.xsp.mvc.MvcLibrary;
import org.openntf.xsp.mvc.impl.DelegatingExceptionViewEngine;

import jakarta.enterprise.inject.spi.Extension;

public class MvcBeanClassContributor implements WeldBeanClassContributor {

	@Override
	public Collection<Class<?>> getBeanClasses() {
		if(LibraryUtil.isLibraryActive(MvcLibrary.LIBRARY_ID)) {
			return Arrays.asList(
				DelegatingExceptionViewEngine.class
			);
		} else {
			return null;
		}
	}

	@Override
	public Collection<Extension> getExtensions() {
		if(LibraryUtil.isLibraryActive(MvcLibrary.LIBRARY_ID)) {
			return Arrays.asList(new KrazoCdiExtension());
		} else {
			return null;
		}
	}
}
