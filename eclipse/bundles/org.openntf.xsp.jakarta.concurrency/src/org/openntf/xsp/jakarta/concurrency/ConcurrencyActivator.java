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
package org.openntf.xsp.jakarta.concurrency;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * This activator is used to try to ensure that all spawned executors
 * are terminated at HTTP stop, even if they were violently flushed out
 * of context by design changes or if incoming HTTP requests are still
 * held up by blocked threads.
 *
 * <p>This class works reflectively to access
 * {@code lotus.notes.internal.MessageQueue} because Domino's OSGi stack
 * may prevent normal access to it.</p>
 *
 * @author Jesse Gallagher
 * @since 2.10.0
 */
public class ConcurrencyActivator implements BundleActivator {

	public static final String ATTR_SCHEDULEDEXECUTORSERVICE = ConcurrencyActivator.class.getPackage().getName() + "_scheduledExec"; //$NON-NLS-1$

	public static final String ATTR_EXECUTORSERVICE = ConcurrencyActivator.class.getPackage().getName() + "_exec"; //$NON-NLS-1$

	public static final String JNDI_SCHEDULEDEXECUTORSERVICE = "java:comp/DefaultManagedScheduledExecutorService"; //$NON-NLS-1$

	public static final String JNDI_EXECUTORSERVICE = "java:comp/DefaultManagedExecutorService"; //$NON-NLS-1$

	@Override
	public void start(final BundleContext bundleContext) throws Exception {
		
	}

	@Override
	public void stop(final BundleContext bundleContext) throws Exception {
		ExecutorHolder.INSTANCE.termAll();
	}
}
