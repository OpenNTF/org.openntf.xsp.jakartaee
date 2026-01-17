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
package org.openntf.xsp.jakartaee.module.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.ServiceLoader;

import org.openntf.xsp.jakartaee.module.ServletContainerInitializerProvider;

import com.ibm.designer.runtime.domino.adapter.ComponentModule;

import jakarta.servlet.ServletContainerInitializer;

/**
 * This implementation of {@link ServletContainerInitializerProvider} loads initializers
 * using the META-INF/services mechanism.
 */
public class ServiceLoaderServletContainerInitializerProvider implements ServletContainerInitializerProvider {

	@Override
	public Collection<ServletContainerInitializer> provide(ComponentModule module) {
		ClassLoader cl = module.getModuleClassLoader();
		if(cl != null) {
			return ServiceLoader.load(ServletContainerInitializer.class, cl).stream()
				.map(ServiceLoader.Provider::get)
				.toList();
		} else {
			return Collections.emptySet();
		}
	}

}
