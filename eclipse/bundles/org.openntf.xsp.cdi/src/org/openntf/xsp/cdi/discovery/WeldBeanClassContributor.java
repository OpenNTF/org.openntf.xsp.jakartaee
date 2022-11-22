/**
 * Copyright © 2018-2022 Contributors to the XPages Jakarta EE Support Project
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
package org.openntf.xsp.cdi.discovery;

import java.util.Collection;
import java.util.Collections;

import jakarta.enterprise.inject.spi.Extension;

public interface WeldBeanClassContributor {
	public static final String EXTENSION_POINT = WeldBeanClassContributor.class.getName();
	
	Collection<Class<?>> getBeanClasses();
	
	default Collection<Extension> getExtensions() {
		return Collections.emptyList();
	}
	
	/**
	 * @since 2.5.0
	 */
	default Collection<Class<? extends Extension>> getExtensionClasses() {
		return Collections.emptyList();
	}
}
