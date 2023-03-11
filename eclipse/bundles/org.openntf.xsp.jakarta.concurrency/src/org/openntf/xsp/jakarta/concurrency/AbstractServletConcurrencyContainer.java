/**
 * Copyright (c) 2018-2023 Contributors to the XPages Jakarta EE Support Project
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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.enterprise.concurrent.AbstractManagedExecutorService.RejectPolicy;
import org.glassfish.enterprise.concurrent.ContextServiceImpl;
import org.glassfish.enterprise.concurrent.ManagedExecutorServiceImpl;
import org.glassfish.enterprise.concurrent.ManagedScheduledExecutorServiceImpl;
import org.glassfish.enterprise.concurrent.spi.ContextSetupProvider;
import org.openntf.xsp.jakarta.concurrency.servlet.ConcurrencyRequestListener;

import jakarta.enterprise.concurrent.ManagedExecutorService;
import jakarta.enterprise.concurrent.ManagedScheduledExecutorService;
import jakarta.servlet.ServletContext;

/**
 * Provides common behavior for Concurrency containers that make
 * a {@link ServletContext} available.
 * 
 * @author Jesse Gallagher
 * @since 2.7.0
 */
public abstract class AbstractServletConcurrencyContainer {
	private static final Logger log = Logger.getLogger(AbstractServletConcurrencyContainer.class.getPackage().getName());

	public static final String ATTR_THREADFACTORY = AbstractServletConcurrencyContainer.class.getName() + "_threadFactory"; //$NON-NLS-1$
	
	public static final String PROP_PREFIX = "concurrency"; //$NON-NLS-1$
	public static final String PROP_HUNGTASKTHRESHOLD = PROP_PREFIX + ".hungTaskThreshold"; //$NON-NLS-1$
	public static final String PROP_LONGRUNNINGTASKS = PROP_PREFIX + ".longRunningTasks"; //$NON-NLS-1$
	public static final String PROP_COREPOOLSIZE = PROP_PREFIX + ".corePoolSize"; //$NON-NLS-1$
	public static final String PROP_MAXPOOLSIZE = PROP_PREFIX + ".maxPoolSize"; //$NON-NLS-1$
	public static final String PROP_KEEPALIVESECONDS = PROP_PREFIX + ".keepAliveSeconds"; //$NON-NLS-1$
	public static final String PROP_THREADLIFETIMESECONDS = PROP_PREFIX + ".threadLifetimeSeconds"; //$NON-NLS-1$
	public static final String PROP_QUEUECAPACITY = PROP_PREFIX + ".queueCapacity"; //$NON-NLS-1$
	public static final String PROP_REJECTPOLICY = PROP_PREFIX + ".rejectPolicy"; //$NON-NLS-1$
	
	protected static final List<String> CONFIG_PROPS = Collections.unmodifiableList(Arrays.asList(
		PROP_HUNGTASKTHRESHOLD,
		PROP_LONGRUNNINGTASKS,
		PROP_COREPOOLSIZE,
		PROP_MAXPOOLSIZE,
		PROP_KEEPALIVESECONDS,
		PROP_THREADLIFETIMESECONDS,
		PROP_QUEUECAPACITY,
		PROP_REJECTPOLICY
	));
	
	protected abstract Optional<ServletContext> getServletContext();
	
	/**
	 * Initializes the concurrency container for the current context.
	 * 
	 * @param configFetcher a {@link BiFunction} that can take a property name
	 *        and default value, and return a context-specific config value
	 */
	public void initializeConcurrencyContainer(BiFunction<String, String, String> configFetcher) {
		getServletContext().ifPresent(ctx -> {
			ctx.addListener(new ConcurrencyRequestListener());
			
			ContextSetupProvider provider = new DominoContextSetupProvider();
			
			String name = ctx.getServletContextName();
			if(name == null || name.isEmpty()) {
				name = String.valueOf(System.identityHashCode(ctx));
			}
			
			int hungTaskThreshold = Integer.parseInt(configFetcher.apply(PROP_HUNGTASKTHRESHOLD, "0")); //$NON-NLS-1$
			boolean longRunningTasks = Boolean.parseBoolean(configFetcher.apply(PROP_LONGRUNNINGTASKS, "true")); //$NON-NLS-1$
			int corePoolSize = Integer.parseInt(configFetcher.apply(PROP_COREPOOLSIZE, "5")); //$NON-NLS-1$
			int maxPoolSize = Integer.parseInt(configFetcher.apply(PROP_MAXPOOLSIZE, "10")); //$NON-NLS-1$
			int keepAliveTime = Integer.parseInt(configFetcher.apply(PROP_KEEPALIVESECONDS, "1800")); //$NON-NLS-1$
			int threadLifeTime = Integer.parseInt(configFetcher.apply(PROP_THREADLIFETIMESECONDS, "1800")); //$NON-NLS-1$
			int queueCapacity = Integer.parseInt(configFetcher.apply(PROP_QUEUECAPACITY, "0")); //$NON-NLS-1$
			RejectPolicy rejectPolicy = RejectPolicy.valueOf(configFetcher.apply(PROP_REJECTPOLICY, RejectPolicy.ABORT.name()));
			
			ContextServiceImpl contextService = new ContextServiceImpl("contextService-" + name, provider); //$NON-NLS-1$
			NotesManagedThreadFactory factory = new NotesManagedThreadFactory("threadFactory-" + name, contextService); //$NON-NLS-1$
			ctx.setAttribute(ATTR_THREADFACTORY, factory);
			
			ManagedExecutorService exec = new ManagedExecutorServiceImpl(
				"executor-" + name, //$NON-NLS-1$
				factory,
				hungTaskThreshold,
				longRunningTasks,
				corePoolSize,
				maxPoolSize,
				keepAliveTime,
				TimeUnit.SECONDS,
				threadLifeTime,
				queueCapacity,
				contextService,
				rejectPolicy
			);
			ctx.setAttribute(ConcurrencyRequestListener.ATTR_EXECUTORSERVICE, exec);
			ExecutorHolder.INSTANCE.register(exec);
			
			ManagedScheduledExecutorService scheduledExec = new ManagedScheduledExecutorServiceImpl(
				"scheduledExecutor-" + name, //$NON-NLS-1$
				factory,
				hungTaskThreshold,
				longRunningTasks,
				corePoolSize,
				keepAliveTime,
				TimeUnit.SECONDS,
				threadLifeTime,
				contextService,
				rejectPolicy
			);
			ctx.setAttribute(ConcurrencyRequestListener.ATTR_SCHEDULEDEXECUTORSERVICE, scheduledExec);
			ExecutorHolder.INSTANCE.register(scheduledExec);
		});
	}
	
	public void terminateConcurrencyContainer() {
		getServletContext().ifPresent(ctx -> {
			ManagedExecutorService exec = (ManagedExecutorService)ctx.getAttribute(ConcurrencyRequestListener.ATTR_EXECUTORSERVICE);
			if(exec != null) {
				try {
					exec.shutdownNow();
					exec.awaitTermination(5, TimeUnit.MINUTES);
				} catch (Exception e) {
					if(log.isLoggable(Level.SEVERE)) {
						log.log(Level.SEVERE, "Encountered exception terminating executor service", e);
					}
				}
				ExecutorHolder.INSTANCE.unregister(exec);
			}
			
			ManagedScheduledExecutorService scheduledExec = (ManagedScheduledExecutorService)ctx.getAttribute(ConcurrencyRequestListener.ATTR_SCHEDULEDEXECUTORSERVICE);
			if(scheduledExec != null) {
				try {
					scheduledExec.shutdownNow();
					scheduledExec.awaitTermination(5, TimeUnit.MINUTES);
				} catch (Exception e) {
					if(log.isLoggable(Level.SEVERE)) {
						log.log(Level.SEVERE, "Encountered exception terminating scheduled executor service", e);
					}
				}
				ExecutorHolder.INSTANCE.unregister(scheduledExec);
			}
			
			NotesManagedThreadFactory fac = (NotesManagedThreadFactory)ctx.getAttribute(ATTR_THREADFACTORY);
			if(fac != null) {
				fac.stop();
			}
		});
	}
}
