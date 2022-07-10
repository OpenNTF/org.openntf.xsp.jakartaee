package org.openntf.xsp.cdi.concurrency;

import org.glassfish.enterprise.concurrent.spi.ContextHandle;
import org.openntf.xsp.cdi.CDILibrary;
import org.openntf.xsp.jakarta.concurrency.AttributedContextHandle;
import org.openntf.xsp.jakarta.concurrency.ContextSetupParticipant;
import org.openntf.xsp.jakartaee.util.LibraryUtil;

import jakarta.enterprise.inject.spi.CDI;

/**
 * Provides CDI capabilities to managed executors.
 * 
 * @author Jesse Gallagher
 * @since 2.7.0
 */
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
			CDI<Object> cdi =((AttributedContextHandle)contextHandle).getAttribute(ATTR_CDI);
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
