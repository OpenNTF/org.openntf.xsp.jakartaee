/**
 * Copyright (c) 2018-2026 Contributors to the XPages Jakarta EE Support Project
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

import java.util.Collection;
import java.util.Collections;
import java.util.EventListener;

import com.ibm.designer.runtime.domino.adapter.ComponentModule;

import jakarta.servlet.ServletContainerInitializer;

/**
 * Allows contribution of {@link ServletContainerInitializer} for compatible
 * modules.
 * 
 * @since 3.4.0
 */
public interface ServletContainerInitializerProvider {
	Collection<ServletContainerInitializer> provide(ComponentModule module);

	/**
	 * Allows contribution of Servlet listeners to the module.
	 * 
	 * @param module the module being processed
	 * @return a {@link Collection} of Servlet-compatible {@link EventListener} classes
	 * @since 3.5.0
	 */
	default Collection<Class<? extends EventListener>> provideListeners(ComponentModule module) {
		return Collections.emptySet();
	}
}
