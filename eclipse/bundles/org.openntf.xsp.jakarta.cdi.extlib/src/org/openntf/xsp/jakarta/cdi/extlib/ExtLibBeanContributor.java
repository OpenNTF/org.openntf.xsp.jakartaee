/**
 * Copyright (c) 2018-2025 Contributors to the XPages Jakarta EE Support Project
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
package org.openntf.xsp.jakarta.cdi.extlib;

import java.util.Collection;
import java.util.Collections;

import org.openntf.xsp.jakarta.cdi.discovery.CDIClassContributor;
import org.openntf.xsp.jakartaee.util.LibraryUtil;

/**
 * Contributes known XPages Extension Library beans when the ExtLib
 * is active for the current application.
 *
 * @since 2.13.0
 */
public class ExtLibBeanContributor implements CDIClassContributor {

	@Override
	public Collection<Class<?>> getBeanClasses() {
		if(LibraryUtil.isLibraryActive("com.ibm.xsp.extlib.library")) { //$NON-NLS-1$
			return Collections.singleton(ExtLibBeanProvider.class);
		} else {
			return Collections.emptySet();
		}
	}

}
