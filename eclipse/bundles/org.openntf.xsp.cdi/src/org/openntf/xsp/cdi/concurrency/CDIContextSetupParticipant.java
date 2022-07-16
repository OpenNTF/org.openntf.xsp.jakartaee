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
package org.openntf.xsp.cdi.concurrency;

import org.glassfish.enterprise.concurrent.spi.ContextHandle;
import org.openntf.xsp.cdi.CDILibrary;
import org.openntf.xsp.jakarta.concurrency.AttributedContextHandle;
import org.openntf.xsp.jakarta.concurrency.ContextSetupParticipant;
import org.openntf.xsp.jakartaee.util.LibraryUtil;

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
	public void saveContext(ContextHandle contextHandle) {
		if(contextHandle instanceof AttributedContextHandle) {
			if(LibraryUtil.isLibraryActive(CDILibrary.LIBRARY_ID)) {
				((AttributedContextHandle)contextHandle).setAttribute(ATTR_CDI, CDI.current());
			}
		}
	}
	
	@Override
	public void setup(ContextHandle contextHandle) throws IllegalStateException {
		if(contextHandle instanceof AttributedContextHandle) {
			CDI<Object> cdi = ((AttributedContextHandle)contextHandle).getAttribute(ATTR_CDI);
			ConcurrencyCDIContainerLocator.setCdi(cdi);
		}
	}

	@Override
	public void reset(ContextHandle contextHandle) {
		if(contextHandle instanceof AttributedContextHandle) {
			ConcurrencyCDIContainerLocator.setCdi(null);
		}
	}

}
