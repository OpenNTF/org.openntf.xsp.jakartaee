package org.openntf.xsp.jakarta.faces;

import com.ibm.domino.xsp.module.nsf.NSFService;

import org.openntf.xsp.jakartaee.events.JakartaHttpInitListener;

/**
 * @since 3.4.0
 */
public class FacesHttpInitListener implements JakartaHttpInitListener {
	@Override
	public void httpInit() throws Exception {
		// Register ".xhtml" with the NSF service, which will then pass along to FacesServletFactory
		NSFService.addHandledExtensions(".xhtml"); //$NON-NLS-1$
		NSFService.addHandledExtensions(".jsf"); //$NON-NLS-1$
	}
}
