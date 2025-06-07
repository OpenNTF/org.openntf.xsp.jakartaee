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
package org.openntf.xsp.jakarta.cdi.concurrency;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.openntf.xsp.jakarta.concurrency.ConcurrencyActivator;

import jakarta.enterprise.concurrent.ManagedExecutorService;
import jakarta.enterprise.concurrent.ManagedScheduledExecutorService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Named;

/**
 * Provides {@link ManagedExecutorService} and {@link ManagedScheduledExecutorService}
 * instances to the running application via CDI.
 *
 * @author Jesse Gallagher
 * @since 2.7.0
 */
@ApplicationScoped
public class ConcurrencyBean {

	@Produces @Named(ConcurrencyActivator.JNDI_EXECUTORSERVICE)
	public ManagedExecutorService produceExecutorService() {
		try {
			return InitialContext.doLookup(ConcurrencyActivator.JNDI_EXECUTORSERVICE);
		} catch (NamingException e) {
			throw new RuntimeException("Encountered exception looking up ManagedExecutorService");
		}
	}

	@Produces @Named(ConcurrencyActivator.JNDI_SCHEDULEDEXECUTORSERVICE)
	public ManagedScheduledExecutorService produceScheduledExecutorService() {
		try {
			return InitialContext.doLookup(ConcurrencyActivator.JNDI_SCHEDULEDEXECUTORSERVICE);
		} catch (NamingException e) {
			throw new RuntimeException("Encountered exception looking up ManagedExecutorService");
		}
	}

}
