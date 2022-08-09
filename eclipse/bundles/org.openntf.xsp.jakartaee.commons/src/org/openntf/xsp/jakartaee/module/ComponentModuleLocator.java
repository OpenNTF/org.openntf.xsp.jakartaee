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
package org.openntf.xsp.jakartaee.module;

import java.util.Optional;

import org.openntf.xsp.jakartaee.util.LibraryUtil;

import com.ibm.designer.runtime.domino.adapter.ComponentModule;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;

/**
 * This extension interface can be used to define a service that
 * can attempt to locate the active {@link ComponentModule} and
 * {@link ServletContext} instances.
 * 
 * @author Jesse Gallagher
 * @since 2.8.0
 */
public interface ComponentModuleLocator {
	static Optional<ComponentModuleLocator> getDefault() {
		return LibraryUtil.findExtensionsSorted(ComponentModuleLocator.class, false)
			.stream()
			.filter(ComponentModuleLocator::isActive)
			.findFirst();
	}
	
	/**
	 * Determines whether this locator is active in the current context.
	 * It is possible for more than one locator to be active at a time.
	 * 
	 * @return {@code true} if this locator can find its elements;
	 *         {@code false} otherwise
	 */
	boolean isActive();
	
	/**
	 * Finds the active {@link ComponentModule} when {@link #isActive()}
	 * is {@code true}
	 * 
	 * @return the current {@link ComponentModule} instance, or {@code null}
	 *         if this locator is not active
	 */
	ComponentModule getActiveModule();
	
	/**
	 * Attempts to find the active {@link ServletContext}, returning
	 * an empty value when the active module does not supply a ServletContext.
	 * 
	 * @return an {@link Optional} describing the current
	 *         {@link ServletContext}, or an empty one if this
	 *         locator does not apply
	 */
	Optional<ServletContext> getServletContext();
	
	/**
	 * Attempts to find the active {@link HttpServletRequest}, returning
	 * an empty value when the active module does not supply a Servlet request.
	 * 
	 * @return an {@link Optional} describing the current
	 *         {@link HttpServletRequest}, or an empty one if this
	 *         locator does not apply
	 */
	Optional<HttpServletRequest> getServletRequest();
}
