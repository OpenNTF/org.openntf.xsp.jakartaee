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
package org.openntf.xsp.jakartaee.discovery.impl;

import com.ibm.xsp.application.ApplicationEx;

import org.openntf.xsp.jakartaee.discovery.ComponentEnabledLocator;
import org.openntf.xsp.jakartaee.util.LibraryUtil;

/**
 * Determines whether a given component is enabled based on its ID being
 * present in the enabled XPages Libraries in a context XPages Application.
 *
 * @author Jesse Gallagher
 * @since 2.7.0
 */
public class XPagesComponentEnabledLocator implements ComponentEnabledLocator {

	@Override
	public boolean isActive() {
		return ApplicationEx.getInstance() != null;
	}

	@Override
	public boolean isComponentEnabled(final String componentId) {
		ApplicationEx app = ApplicationEx.getInstance();
		if(app != null) {
			return LibraryUtil.usesLibrary(componentId, app);
		}
		return false;
	}

}
