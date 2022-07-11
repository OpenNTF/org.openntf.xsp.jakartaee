package org.openntf.xsp.jakarta.concurrency;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

import org.glassfish.enterprise.concurrent.spi.ContextHandle;
import org.glassfish.enterprise.concurrent.spi.ContextSetupProvider;
import org.openntf.xsp.jakartaee.util.LibraryUtil;

import jakarta.enterprise.concurrent.ContextService;

/**
 * This implementation of {@link ContextSetupProvider} uses the {@link ContextSetupParticipant}
 * extension point to delegate handling of concurrency events.
 *  
 * @author Jesse Gallagher
 * @since 2.7.0
 */
public class DominoContextSetupProvider implements ContextSetupProvider {
	private static final long serialVersionUID = 1L;
	
	public DominoContextSetupProvider() {
	}

	@Override
	public ContextHandle saveContext(ContextService contextService) {
		ContextHandle handle = new AttributedContextHandle();
		LibraryUtil.findExtensionsSorted(ContextSetupParticipant.class, false)
			.forEach(participant -> participant.saveContext(handle));
		
		return handle;
	}

	@Override
	public ContextHandle saveContext(ContextService contextService, Map<String, String> contextObjectProperties) {
		ContextHandle handle = new AttributedContextHandle();
		LibraryUtil.findExtensionsSorted(ContextSetupParticipant.class, false)
			.forEach(participant -> participant.saveContext(handle, contextObjectProperties));
		
		return handle;
	}

	@Override
	public ContextHandle setup(ContextHandle contextHandle) throws IllegalStateException {
		if(shouldSetup()) {
			LibraryUtil.findExtensionsSorted(ContextSetupParticipant.class, false)
				.forEach(participant -> participant.setup(contextHandle));
		}
		
		return contextHandle;
	}

	@Override
	public void reset(ContextHandle contextHandle) {
		LibraryUtil.findExtensionsSorted(ContextSetupParticipant.class, true)
			.forEach(participant -> participant.reset(contextHandle));
	}

	/**
	 * Setup is called twice with the same thread object, but we only want to actually call setup participants
	 * when run from the internal executor service.
	 * 
	 * @return {@code true} if participants should be called to set up; {@code false} otherwise
	 */
	private boolean shouldSetup() {
		StackTraceElement[] stack = Thread.currentThread().getStackTrace();
		return Arrays.stream(stack)
			.anyMatch(el -> ThreadPoolExecutor.class.getName().equals(el.getClassName()));
	}
}
