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
package org.openntf.xsp.microprofile.health;

import java.util.Collection;
import java.util.Collections;

import org.openntf.xsp.jakartaee.util.LibraryUtil;
import org.openntf.xsp.jaxrs.JAXRSClassContributor;

public class HealthResourceContributor implements JAXRSClassContributor {

	@Override
	public Collection<Class<?>> getClasses() {
		if(LibraryUtil.isLibraryActive(HealthLibrary.LIBRARY_ID)) {
			return Collections.singleton(HealthResource.class);
		} else {
			return Collections.emptyList();
		}
	}

}
