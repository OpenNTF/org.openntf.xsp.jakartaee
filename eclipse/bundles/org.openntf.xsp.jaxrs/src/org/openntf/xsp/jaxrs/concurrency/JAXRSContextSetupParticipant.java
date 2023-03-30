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
package org.openntf.xsp.jaxrs.concurrency;

import org.glassfish.enterprise.concurrent.spi.ContextHandle;
import org.openntf.xsp.jakarta.concurrency.AttributedContextHandle;
import org.openntf.xsp.jakarta.concurrency.ContextSetupParticipant;
import org.openntf.xsp.jakartaee.util.LibraryUtil;
import org.openntf.xsp.jaxrs.JAXRSLibrary;

import jakarta.ws.rs.ext.RuntimeDelegate;

/**
 * This {@link ContextSetupParticipant} propagates the JAX-RS {@link RuntimeDelegate}
 * instance from the active environment.
 * 
 * @author Jesse Gallagher
 * @since 2.7.0
 */
@SuppressWarnings("unused")
public class JAXRSContextSetupParticipant implements ContextSetupParticipant {
	public static final String ATTR_DELEGATE = JAXRSContextSetupParticipant.class.getName() + "_delegate"; //$NON-NLS-1$

	@Override
	public void saveContext(ContextHandle contextHandle) {
//		if(LibraryUtil.isLibraryActive(JAXRSLibrary.LIBRARY_ID)) {
//			if(contextHandle instanceof AttributedContextHandle) {
//				((AttributedContextHandle)contextHandle).setAttribute(ATTR_DELEGATE, RuntimeDelegate.getInstance());
//			}
//		}
	}

	@Override
	public void setup(ContextHandle contextHandle) throws IllegalStateException {
//		if(contextHandle instanceof AttributedContextHandle) {
//			RuntimeDelegate delegate = ((AttributedContextHandle)contextHandle).getAttribute(ATTR_DELEGATE);
//			if(delegate != null) {
//				RuntimeDelegate.setInstance(delegate);
//			}
//		}
	}

	@Override
	public void reset(ContextHandle contextHandle) {
//		if(contextHandle instanceof AttributedContextHandle) {
//			RuntimeDelegate delegate = ((AttributedContextHandle)contextHandle).getAttribute(ATTR_DELEGATE);
//			if(delegate != null) {
//				RuntimeDelegate.setInstance(null);
//			}
//		}
	}

}
