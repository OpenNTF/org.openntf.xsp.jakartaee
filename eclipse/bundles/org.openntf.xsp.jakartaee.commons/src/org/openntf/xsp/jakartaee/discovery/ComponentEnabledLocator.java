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
package org.openntf.xsp.jakartaee.discovery;

/**
 * This extension class allows contributions to the process of determining
 * whether a given XPages Jakarta EE component is enabled.
 * 
 * @author Jesse Gallagher
 * @since 2.7.0
 */
public interface ComponentEnabledLocator {
	/**
	 * @return {@code true} if the locator is in a context where it can run
	 */
	boolean isActive();
	
	/**
	 * @param componentId the ID of the component to query
	 * @return {@code true} if the given component is enabled
	 */
	boolean isComponentEnabled(String componentId);
}
