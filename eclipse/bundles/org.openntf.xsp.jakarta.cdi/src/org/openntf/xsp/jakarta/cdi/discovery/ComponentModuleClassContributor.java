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
package org.openntf.xsp.jakarta.cdi.discovery;

import java.util.Collection;
import java.util.stream.Collectors;

import org.openntf.xsp.jakartaee.module.ComponentModuleLocator;
import org.openntf.xsp.jakartaee.util.ModuleUtil;

/**
 * This class is responsible for locating and loading bean classes from the
 * context NSF when active.
 *
 * <p>Originally, this work was done by {@link StaticBeanArchiveHandler}, but
 * this mechanism avoids the trouble of handing off just string class names.</p>
 *
 * @author Jesse Gallagher
 * @since 2.9.0
 */
public class ComponentModuleClassContributor implements CDIClassContributor {

	@Override
	public Collection<Class<?>> getBeanClasses() {
		return ComponentModuleLocator.getDefault()
			.map(ComponentModuleLocator::getActiveModule)
			.map(ModuleUtil::getClasses)
			.map(c -> c.collect(Collectors.toSet()))
			.orElse(null);
	}

}
