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
package org.openntf.xsp.jakarta.mvc.impl;

import java.util.Set;

import org.eclipse.krazo.servlet.KrazoContainerInitializer;
import org.openntf.xsp.jakartaee.util.LibraryUtil;

import jakarta.servlet.ServletContext;
import jakarta.servlet.annotation.HandlesTypes;
import jakarta.ws.rs.Path;

@HandlesTypes({Path.class})
public class MvcContainerInitializer extends KrazoContainerInitializer {
	@Override
	public void onStartup(final Set<Class<?>> classes, final ServletContext servletContext) {
		if(LibraryUtil.isLibraryActive(LibraryUtil.LIBRARY_UI)) {
			super.onStartup(classes, servletContext);
		}
	}
}
