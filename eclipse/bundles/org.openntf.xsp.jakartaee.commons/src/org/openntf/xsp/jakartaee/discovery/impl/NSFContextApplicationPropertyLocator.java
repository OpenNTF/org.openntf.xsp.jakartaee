package org.openntf.xsp.jakartaee.discovery.impl;

import org.openntf.xsp.jakartaee.discovery.ApplicationPropertyLocator;
import org.openntf.xsp.jakartaee.util.LibraryUtil;

import com.ibm.designer.runtime.domino.adapter.ComponentModule;
import com.ibm.domino.xsp.module.nsf.NotesContext;

import jakarta.annotation.Priority;

/**
 * Determines whether a given component is enabled based on its ID being
 * present in the enabled XPages Libraries in the current {@link NotesContent}
 * {@link NotesDatabase}.
 * 
 * @author Jesse Gallagher
 * @since 2.7.0
 */
@Priority(1)
public class NSFContextApplicationPropertyLocator implements ApplicationPropertyLocator {

	@Override
	public boolean isActive() {
		NotesContext ctx = NotesContext.getCurrentUnchecked();
		if(ctx != null) {
			ComponentModule module = ctx.getModule();
			if(module != null) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String getApplicationProperty(String prop, String defaultValue) {
		NotesContext ctx = NotesContext.getCurrentUnchecked();
		if(ctx != null) {
			ComponentModule module = ctx.getModule();
			if(module != null) {
				return LibraryUtil.getXspProperties(module).getProperty(prop, defaultValue);
			}
		}
		return defaultValue;
	}

}
