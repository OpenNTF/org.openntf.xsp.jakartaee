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

import org.glassfish.enterprise.concurrent.spi.ContextHandle;
import org.openntf.xsp.jakarta.cdi.context.RequestScopeContext;
import org.openntf.xsp.jakarta.concurrency.AttributedContextHandle;
import org.openntf.xsp.jakarta.concurrency.ContextSetupParticipant;

import jakarta.annotation.Priority;
import jakarta.enterprise.inject.spi.CDI;

/**
 * Provides CDI capabilities to managed executors.
 *
 * @author Jesse Gallagher
 * @since 2.7.0
 */
@Priority(2)
public class CDIContextSetupParticipant implements ContextSetupParticipant {
	private static final String ATTR_CDI = CDIContextSetupParticipant.class.getName();

	@Override
	public void saveContext(final ContextHandle contextHandle) {
		if(contextHandle instanceof AttributedContextHandle) {
			try {
				((AttributedContextHandle)contextHandle).setAttribute(ATTR_CDI, CDI.current());
			} catch(IllegalStateException e) {
				// Oddly, this is sometimes called from an existing managed thread,
				//   which will not have a CDI available yet. If so, ignore
			}
		}
	}

	@Override
	public void setup(final ContextHandle contextHandle) throws IllegalStateException {
		if(contextHandle instanceof AttributedContextHandle) {
			RequestScopeContext.FORCE_ACTIVE.set(true);
			CDI<Object> cdi = ((AttributedContextHandle)contextHandle).getAttribute(ATTR_CDI);
			if(cdi != null) {
				ConcurrencyCDIContainerLocator.setCdi(cdi);
			}

			// TODO investigate propagating the same beans from here outside of XPages (Issue #286)
			// May be handled if we move to WeldWebModule
		}
	}

	@Override
	public void reset(final ContextHandle contextHandle) {
		if(contextHandle instanceof AttributedContextHandle) {
			ConcurrencyCDIContainerLocator.setCdi(null);
			RequestScopeContext.FORCE_ACTIVE.set(false);
		}
	}

}
