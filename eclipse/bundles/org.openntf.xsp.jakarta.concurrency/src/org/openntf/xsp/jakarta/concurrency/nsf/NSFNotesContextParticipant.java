package org.openntf.xsp.jakarta.concurrency.nsf;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

import org.glassfish.enterprise.concurrent.spi.ContextHandle;
import org.openntf.xsp.jakarta.concurrency.AttributedContextHandle;
import org.openntf.xsp.jakarta.concurrency.ContextSetupParticipant;

import com.ibm.designer.runtime.domino.adapter.ComponentModule;
import com.ibm.domino.xsp.module.nsf.NSFComponentModule;
import com.ibm.domino.xsp.module.nsf.NotesContext;
import com.ibm.domino.xsp.module.nsf.SessionCloner;

import jakarta.annotation.Priority;

/**
 * This {@link ContextSetupParticipant} will initialize and terminate an NSF-specific
 * {@link NotesContext} for the running thread.
 * 
 * @author Jesse Gallagher
 * @since 2.7.0
 */
@Priority(100)
public class NSFNotesContextParticipant implements ContextSetupParticipant {
	public static final String ATTR_MODULE = NSFNotesContextParticipant.class.getName() + "_module"; //$NON-NLS-1$
	public static final String ATTR_CLASSLOADER = NSFNotesContextParticipant.class.getName() + "_classLoader"; //$NON-NLS-1$
	
	@Override
	public void saveContext(ContextHandle contextHandle) {
		if(contextHandle instanceof AttributedContextHandle) {
			NotesContext ctx = NotesContext.getCurrentUnchecked();
			if(ctx != null) {
				((AttributedContextHandle)contextHandle).setAttribute(ATTR_MODULE, ctx.getModule());
			}
		}
	}
	
	@Override
	public void saveContext(ContextHandle contextHandle, Map<String, String> contextObjectProperties) {
		saveContext(contextHandle);
	}
	
	@Override
	public void setup(ContextHandle contextHandle) throws IllegalStateException {
		if(!shouldSetup()) {
			return;
		}
		
		if(contextHandle instanceof AttributedContextHandle) {
			ComponentModule mod = ((AttributedContextHandle)contextHandle).getAttribute(ATTR_MODULE);
			if(mod instanceof NSFComponentModule) {
				mod.updateLastModuleAccess();
				
				NotesContext notesContext = new NotesContext((NSFComponentModule)mod);
				NotesContext.initThread(notesContext);
				
				ClassLoader cl = AccessController.doPrivileged((PrivilegedAction<ClassLoader>)() -> {
					ClassLoader tccc = Thread.currentThread().getContextClassLoader();
					Thread.currentThread().setContextClassLoader(mod.getModuleClassLoader());
					return tccc;
				});
				((AttributedContextHandle)contextHandle).setAttribute(ATTR_CLASSLOADER, cl);
			}
		}
	}
	
	@Override
	public void reset(ContextHandle contextHandle) {
		if(contextHandle instanceof AttributedContextHandle) {
			ComponentModule mod = ((AttributedContextHandle)contextHandle).getAttribute(ATTR_MODULE);
			if(mod instanceof NSFComponentModule) {
				ClassLoader tccc = ((AttributedContextHandle)contextHandle).getAttribute(ATTR_CLASSLOADER);
				if(tccc != null) {
					AccessController.doPrivileged((PrivilegedAction<Void>)() -> {
						Thread.currentThread().setContextClassLoader(tccc);
						return null;
					});
				}
				
				if(NotesContext.getCurrentUnchecked() != null) {
					NotesContext.termThread();
				}
			}	
		}
	}

	private boolean shouldSetup() {
		StackTraceElement[] stack = Thread.currentThread().getStackTrace();
		return Arrays.stream(stack)
			.anyMatch(el -> ThreadPoolExecutor.class.getName().equals(el.getClassName()));
	}
}
