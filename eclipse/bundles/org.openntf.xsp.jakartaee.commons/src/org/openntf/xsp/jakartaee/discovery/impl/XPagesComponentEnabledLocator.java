package org.openntf.xsp.jakartaee.discovery.impl;

import org.openntf.xsp.jakartaee.discovery.ComponentEnabledLocator;
import org.openntf.xsp.jakartaee.util.LibraryUtil;

import com.ibm.xsp.application.ApplicationEx;

/**
 * Determines whether a given component is enabled based on its ID being
 * present in the enabled XPages Libraries in a context XPages Application.
 * 
 * @author Jesse Gallagher
 * @since 2.7.0
 */
public class XPagesComponentEnabledLocator implements ComponentEnabledLocator {

	@Override
	public boolean isActive() {
		return ApplicationEx.getInstance() != null;
	}

	@Override
	public boolean isComponentEnabled(String componentId) {
		ApplicationEx app = ApplicationEx.getInstance();
		if(app != null) {
			return LibraryUtil.usesLibrary(componentId, app);
		}
		return false;
	}

}
