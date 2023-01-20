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

import java.util.Optional;
import java.util.Properties;

import org.openntf.xsp.jakartaee.discovery.ApplicationPropertyLocator;

import com.ibm.xsp.application.ApplicationEx;

import jakarta.annotation.Priority;

/**
 * Retrieves the named property from an active XPages application.
 * 
 * @author Jesse Gallagher
 * @since 2.7.0
 */
@Priority(3)
public class XPagesApplicationPropertyLocator implements ApplicationPropertyLocator {

	@Override
	public boolean isActive() {
		return ApplicationEx.getInstance() != null;
	}

	@Override
	public String getApplicationProperty(String prop, String defaultValue) {
		ApplicationEx app = ApplicationEx.getInstance();
		if(app != null) {
			return app.getApplicationProperty(prop, defaultValue);
		}
		return defaultValue;
	}
	
	@Override
	public Optional<Properties> getApplicationProperties() {
		return Optional.empty();
	}
}
