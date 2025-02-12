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
package org.openntf.xsp.jakarta.concurrency;

import java.security.AccessController;
import java.security.PrivilegedAction;

import org.glassfish.enterprise.concurrent.AbstractManagedThread;
import org.glassfish.enterprise.concurrent.ContextServiceImpl;
import org.glassfish.enterprise.concurrent.ManagedThreadFactoryImpl;
import org.glassfish.enterprise.concurrent.spi.ContextHandle;

import lotus.domino.NotesThread;

public class NotesManagedThreadFactory extends ManagedThreadFactoryImpl {

	public NotesManagedThreadFactory(final String name) {
		super(name);
	}

	public NotesManagedThreadFactory(final String name, final ContextServiceImpl contextService) {
		super(name, contextService);
	}

	public NotesManagedThreadFactory(final String name, final ContextServiceImpl contextService, final int priority) {
		super(name, contextService, priority);
	}

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected AbstractManagedThread createThread(final Runnable r, final ContextHandle contextHandleForSetup) {
        if (System.getSecurityManager() == null) {
            return new ManagedNotesThread(r, contextHandleForSetup);
        } else {
            return (ManagedThread) AccessController.doPrivileged((PrivilegedAction) () -> new ManagedNotesThread(r, contextHandleForSetup));
        }
    }

	@Override
	public void stop() {
		AccessController.doPrivileged((PrivilegedAction<Void>)() -> {
			super.stop();
			return null;
		});
	}

	@Override
	public Thread newThread(final Runnable r) {
		Thread t = super.newThread(r);
		t.setDaemon(false);
		return t;
	}

	public class ManagedNotesThread extends ManagedThread {

		public ManagedNotesThread(final Runnable target, final ContextHandle contextHandleForSetup) {
			super(target, contextHandleForSetup);
		}

		@Override
		public void run() {
			NotesThread.sinitThread();
			try {
				super.run();
			} finally {
				NotesThread.stermThread();
			}
		}
	}
}
