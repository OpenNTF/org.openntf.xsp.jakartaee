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
package org.openntf.xsp.jakarta.cdi.bean;

import java.util.Collection;
import java.util.List;

import org.openntf.xsp.jakarta.cdi.discovery.CDIClassContributor;

/**
 * Provides implicit beans from this bundle to the new Weld containers.
 *
 * @author Jesse Gallagher
 * @since 2.1.0
 */
public class BuiltinBeanClassContributor implements CDIClassContributor {

	@Override
	public Collection<Class<?>> getBeanClasses() {
		return List.of(
			HttpContextBean.class,
			DominoImplicitObjectProvider.class
		);
	}

}
