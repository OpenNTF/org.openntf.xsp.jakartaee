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
package org.openntf.xsp.jakartaee.module.jakartansf;

import java.util.Collection;
import java.util.EventListener;
import java.util.Set;

import com.ibm.designer.runtime.domino.adapter.ComponentModule;

import org.openntf.xsp.jakartaee.module.ServletContainerInitializerProvider;
import org.openntf.xsp.jakartaee.module.jakartansf.concurrency.NSFJakartaModuleConcurrencyListener;

import jakarta.servlet.ServletContainerInitializer;

/**
 * @since 3.5.0
 */
public class NSFJakartaModuleListenerProvider implements ServletContainerInitializerProvider {

	@Override
	public Collection<ServletContainerInitializer> provide(ComponentModule module) {
		return null;
	}
	
	@Override
	public Collection<Class<? extends EventListener>> provideListeners(ComponentModule module) {
		return Set.of(
			NSFJakartaModuleConcurrencyListener.class
		);
	}

}
