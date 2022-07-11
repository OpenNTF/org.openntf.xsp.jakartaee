package org.openntf.xsp.jakarta.concurrency;

import java.security.AccessController;
import java.security.PrivilegedAction;

import org.glassfish.enterprise.concurrent.AbstractManagedThread;
import org.glassfish.enterprise.concurrent.ContextServiceImpl;
import org.glassfish.enterprise.concurrent.ManagedThreadFactoryImpl;
import org.glassfish.enterprise.concurrent.spi.ContextHandle;

import lotus.domino.NotesThread;

public class NotesManagedThreadFactory extends ManagedThreadFactoryImpl {

	public NotesManagedThreadFactory(String name) {
		super(name);
	}

	public NotesManagedThreadFactory(String name, ContextServiceImpl contextService) {
		super(name, contextService);
	}

	public NotesManagedThreadFactory(String name, ContextServiceImpl contextService, int priority) {
		super(name, contextService, priority);
	}
	
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
	public Thread newThread(Runnable r) {
		Thread t = super.newThread(r);
		t.setDaemon(false);
		return t;
	}
	
	public class ManagedNotesThread extends ManagedThread {

		public ManagedNotesThread(Runnable target, ContextHandle contextHandleForSetup) {
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
