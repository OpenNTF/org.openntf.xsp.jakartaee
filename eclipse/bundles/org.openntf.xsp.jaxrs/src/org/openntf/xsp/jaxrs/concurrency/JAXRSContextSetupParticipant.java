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
public class JAXRSContextSetupParticipant implements ContextSetupParticipant {
	public static final String ATTR_DELEGATE = JAXRSContextSetupParticipant.class.getName() + "_delegate"; //$NON-NLS-1$

	@Override
	public void saveContext(ContextHandle contextHandle) {
		if(LibraryUtil.isLibraryActive(JAXRSLibrary.LIBRARY_ID)) {
			if(contextHandle instanceof AttributedContextHandle) {
				((AttributedContextHandle)contextHandle).setAttribute(ATTR_DELEGATE, RuntimeDelegate.getInstance());
			}
		}
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
