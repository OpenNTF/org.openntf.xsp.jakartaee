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
package org.openntf.xsp.jakartaee.module.jakartansf.concurrency;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ThreadPoolExecutor;

import org.glassfish.concurro.spi.ContextHandle;
import org.openntf.xsp.jakarta.concurrency.AttributedContextHandle;
import org.openntf.xsp.jakarta.concurrency.ContextSetupParticipant;
import org.openntf.xsp.jakartaee.module.jakartansf.util.ActiveRequest;
import org.openntf.xsp.jakartaee.module.jakartansf.util.ActiveRequestCloner;

import jakarta.annotation.Priority;

/**
 * This {@link ContextSetupParticipant} will initialize and terminate an NSF-specific
 * {@link NotesContext} for the running thread.
 *
 * @author Jesse Gallagher
 * @since 2.7.0
 */
@Priority(100)
public class NSFJakartaModuleContextParticipant implements ContextSetupParticipant {
	public static final String ATTR_CLASSLOADER = NSFJakartaModuleContextParticipant.class.getName() + "_classLoader"; //$NON-NLS-1$
	public static final String ATTR_CLONER = NSFJakartaModuleContextParticipant.class.getName() + "_cloner"; //$NON-NLS-1$

	@Override
	public void saveContext(final ContextHandle contextHandle) {
		if(contextHandle instanceof AttributedContextHandle ach) {
			if(ach.getAttribute(ATTR_CLONER) == null) {
				ActiveRequest.get().ifPresent(req -> {
					ach.setAttribute(ATTR_CLONER, req.createCloner());
				});
			}
		}
	}

	@Override
	public void saveContext(final ContextHandle contextHandle, final Map<String, String> contextObjectProperties) {
		saveContext(contextHandle);
	}

	@Override
	public void setup(final ContextHandle contextHandle) throws IllegalStateException {
		if(!shouldSetup()) {
			return;
		}

		if(contextHandle instanceof AttributedContextHandle ach) {
			ActiveRequestCloner cloner = ach.getAttribute(ATTR_CLONER);
			if(cloner != null) {
				ActiveRequest req = cloner.cloneRequest();
				ActiveRequest.set(req);
				
				req.module().updateLastModuleAccess();
				
				ClassLoader cl = AccessController.doPrivileged((PrivilegedAction<ClassLoader>)() -> {
					ClassLoader tccc = Thread.currentThread().getContextClassLoader();
					Thread.currentThread().setContextClassLoader(req.module().getModuleClassLoader());
					return tccc;
				});
				ach.setAttribute(ATTR_CLASSLOADER, cl);
			}
		}
	}

	@Override
	public void reset(final ContextHandle contextHandle) {
		if(contextHandle instanceof AttributedContextHandle ach) {
			// Only operate on Jakarta requests
			Optional<ActiveRequest> req = ActiveRequest.get();
			if(req.isPresent()) {
				ClassLoader tccc = ach.getAttribute(ATTR_CLASSLOADER);
				if(tccc != null) {
					AccessController.doPrivileged((PrivilegedAction<Void>)() -> {
						Thread.currentThread().setContextClassLoader(tccc);
						return null;
					});
				}

				req.get().lsxbe().close();
				ActiveRequest.set(null);
			}
		}
	}

	private boolean shouldSetup() {
		StackTraceElement[] stack = Thread.currentThread().getStackTrace();
		return Arrays.stream(stack)
			.anyMatch(el -> ThreadPoolExecutor.class.getName().equals(el.getClassName()));
	}
}
