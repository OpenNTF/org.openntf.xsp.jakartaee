/**
 * Copyright (c) 2018-2023 Contributors to the XPages Jakarta EE Support Project
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
package org.openntf.xsp.jakartaee.discovery.impl;

import org.openntf.xsp.jakartaee.discovery.ApplicationPropertyLocator;
import org.openntf.xsp.jakartaee.module.ComponentModuleLocator;
import org.openntf.xsp.jakartaee.util.LibraryUtil;

import com.ibm.designer.runtime.domino.adapter.ComponentModule;

import jakarta.annotation.Priority;

/**
 * Retrieves an application property from the currently-active {@link ComponentModule}.
 * 
 * @author Jesse Gallagher
 * @since 2.7.0
 */
@Priority(2)
public class ComponentModuleApplicationPropertyLocator implements ApplicationPropertyLocator {

	@Override
	public boolean isActive() {
		return ComponentModuleLocator.getDefault().isPresent();
	}

	@Override
	public String getApplicationProperty(String prop, String defaultValue) {
		return ComponentModuleLocator.getDefault()
			.map(ComponentModuleLocator::getActiveModule)
			.map(LibraryUtil::getXspProperties)
			.map(props -> props.getProperty(prop, defaultValue))
			.orElse(defaultValue);
	}

}
