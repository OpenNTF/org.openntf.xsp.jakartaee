package org.openntf.xsp.jakarta.concurrency;

import java.util.Map;

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
		LibraryUtil.findExtensionsSorted(ContextSetupParticipant.class, false)
			.forEach(participant -> participant.setup(contextHandle));
		
		return contextHandle;
	}

	@Override
	public void reset(ContextHandle contextHandle) {
		LibraryUtil.findExtensionsSorted(ContextSetupParticipant.class, true)
			.forEach(participant -> participant.setup(contextHandle));
	}

}
