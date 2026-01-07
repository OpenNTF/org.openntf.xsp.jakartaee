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
package org.openntf.xsp.microprofile.config;

import java.util.Collection;
import java.util.Collections;

import org.openntf.xsp.jakarta.cdi.discovery.CDIClassContributor;
import org.openntf.xsp.jakartaee.util.LibraryUtil;

import io.smallrye.config.inject.ConfigExtension;
import jakarta.enterprise.inject.spi.Extension;

public class ConfigCDIContributor implements CDIClassContributor {

	@Override
	public Collection<Class<?>> getBeanClasses() {
		return null;
	}

	@Override
	public Collection<Extension> getExtensions() {
		if(LibraryUtil.isLibraryActive(LibraryUtil.LIBRARY_MICROPROFILE)) {
			return Collections.singleton(new ConfigExtension());
		}
		return null;
	}

}
