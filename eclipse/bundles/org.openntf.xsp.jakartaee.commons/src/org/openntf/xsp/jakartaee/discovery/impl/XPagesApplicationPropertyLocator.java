package org.openntf.xsp.jakartaee.discovery.impl;

import org.openntf.xsp.jakartaee.discovery.ApplicationPropertyLocator;

import com.ibm.xsp.application.ApplicationEx;

import jakarta.annotation.Priority;

/**
 * Retrieves the named property from an active XPages application.
 * 
 * @author Jesse Gallagher
 * @since 2.7.0
 */
@Priority(3)
public class XPagesApplicationPropertyLocator implements ApplicationPropertyLocator {

	@Override
	public boolean isActive() {
		return ApplicationEx.getInstance() != null;
	}

	@Override
	public String getApplicationProperty(String prop, String defaultValue) {
		ApplicationEx app = ApplicationEx.getInstance();
		if(app != null) {
			return app.getApplicationProperty(prop, defaultValue);
		}
		return defaultValue;
	}
}
