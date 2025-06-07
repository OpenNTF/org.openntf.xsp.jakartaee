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
package org.openntf.xsp.jakartaee.module.xspnsf.concurrency;

import java.util.Optional;

import com.ibm.xsp.application.ApplicationEx;

import org.openntf.xsp.jakarta.concurrency.AbstractServletConcurrencyContainer;
import org.openntf.xsp.jakartaee.events.JakartaApplicationListener;
import org.openntf.xsp.jakartaee.module.ComponentModuleLocator;

import jakarta.annotation.Priority;
import jakarta.servlet.ServletContext;

/**
 * Listens for XPages application initialization - which should be roughly
 * comparable to ServletContext lifecycle - to attach a listener to init/term
 * JNDI configuration for Concurrency.
 *
 * @author Jesse Gallagher
 * @since 2.7.0
 */
@Priority(1)
public class ConcurrencyApplicationListener extends AbstractServletConcurrencyContainer implements JakartaApplicationListener {
	@Override
	public void applicationCreated(final ApplicationEx app) {
		initializeConcurrencyContainer(app::getProperty);
	}

	@Override
	public void applicationDestroyed(final ApplicationEx app) {
		terminateConcurrencyContainer();
	}

	@Override
	protected Optional<ServletContext> getServletContext() {
		return ComponentModuleLocator.getDefault()
			.flatMap(ComponentModuleLocator::getServletContext);
	}
}
