package org.openntf.xsp.jakartaee.module.jakartansf;

import org.openntf.xsp.jakartaee.events.JakartaHttpInitListener;
import org.openntf.xsp.jakartaee.module.jakartansf.util.ModuleTracker;

/**
 * @since 3.4.0
 */
public class NSFJakartaHttpInitListener implements JakartaHttpInitListener {
	@Override
	public void httpInit() throws Exception {
		ModuleTracker.INSTANCE.populateModules(NSFJakartaModuleService.getInstance(null));
	}
	
	@Override
	public void postInit() throws Exception {
		ModuleTracker.INSTANCE.initializeModules();
	}
}
