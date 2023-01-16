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
package org.openntf.xsp.cdi.concurrency;

import java.security.AccessController;
import java.security.PrivilegedAction;

import org.glassfish.enterprise.concurrent.spi.ContextHandle;
import org.openntf.xsp.jakarta.concurrency.AttributedContextHandle;
import org.openntf.xsp.jakarta.concurrency.ContextSetupParticipant;

import com.ibm.domino.xsp.module.nsf.NotesContext;
import com.ibm.domino.xsp.module.nsf.SessionCloner;

import jakarta.annotation.Priority;
import lotus.domino.NotesException;
import lotus.domino.NotesFactory;
import lotus.domino.Session;

/**
 * Provides cloned sessions to managed executors.
 * 
 * @author Jesse Gallagher
 * @since 2.7.0
 */
@Priority(1)
public class NSFSessionClonerSetupParticipant implements ContextSetupParticipant {
	public static final ThreadLocal<Session> THREAD_SESSION = new ThreadLocal<>();
	public static final ThreadLocal<Session> THREAD_SESSIONASSIGNER = new ThreadLocal<>();
	
	private static final String ATTR_CLONER = NSFSessionClonerSetupParticipant.class.getName() + "_cloner"; //$NON-NLS-1$

	@Override
	public void saveContext(ContextHandle contextHandle) {
		if(contextHandle instanceof AttributedContextHandle) {
			if(NotesContext.getCurrentUnchecked() != null) {
				((AttributedContextHandle)contextHandle).setAttribute(ATTR_CLONER, SessionCloner.getSessionCloner());
			}
		}
	}

	@Override
	public void setup(ContextHandle contextHandle) throws IllegalStateException {
		if(contextHandle instanceof AttributedContextHandle) {
			SessionCloner cloner = ((AttributedContextHandle)contextHandle).getAttribute(ATTR_CLONER);
			if(cloner != null) {
				try {
					THREAD_SESSION.set(cloner.getSession());
				} catch (NotesException e) {
					throw new RuntimeException(e);
				}
				
				Session sessionAsSigner = AccessController.doPrivileged((PrivilegedAction<Session>)() -> {
					ClassLoader cl = Thread.currentThread().getContextClassLoader();
					SecurityManager sm = System.getSecurityManager();
					try {
						Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
						System.setSecurityManager(null);
						// TODO pull in session as signer name
						return NotesFactory.createSession();
					} catch (NotesException e) {
						throw new RuntimeException(e);
					} finally {
						System.setSecurityManager(sm);
						Thread.currentThread().setContextClassLoader(cl);
					}
				});
				THREAD_SESSIONASSIGNER.set(sessionAsSigner);
			}
		}
	}

	@Override
	public void reset(ContextHandle contextHandle) {
		Session session = THREAD_SESSION.get();
		if(session != null) {
			try {
				session.recycle();
			} catch (NotesException e) {
			}
			THREAD_SESSION.remove();
		}
		Session sessionAsSigner = THREAD_SESSIONASSIGNER.get();
		if(sessionAsSigner != null) {
			try {
				sessionAsSigner.recycle();
			} catch(NotesException e) {
			}
			THREAD_SESSIONASSIGNER.remove();
		}
	}

}
