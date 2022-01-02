/**
 * Copyright © 2018-2022 Jesse Gallagher
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
package org.openntf.xsp.mvc.impl;

import java.util.Set;

import org.eclipse.krazo.servlet.KrazoContainerInitializer;
import org.openntf.xsp.jakartaee.LibraryUtil;
import org.openntf.xsp.mvc.MvcLibrary;
import com.ibm.xsp.application.ApplicationEx;

import jakarta.servlet.ServletContext;
import jakarta.servlet.annotation.HandlesTypes;
import jakarta.ws.rs.Path;

@HandlesTypes({Path.class})
public class MvcContainerInitializer extends KrazoContainerInitializer {
	@Override
	public void onStartup(Set<Class<?>> classes, ServletContext servletContext) {
		ApplicationEx app = ApplicationEx.getInstance();
		if(app != null) {
			if(LibraryUtil.usesLibrary(MvcLibrary.LIBRARY_ID, app)) {
				super.onStartup(classes, servletContext);
			}
		}
	}
}
