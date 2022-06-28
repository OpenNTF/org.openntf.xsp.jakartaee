package org.openntf.xsp.jakartaee.discovery.impl;

import org.openntf.xsp.jakartaee.discovery.ApplicationPropertyLocator;
import org.openntf.xsp.jakartaee.util.LibraryUtil;

import com.ibm.designer.domino.napi.NotesAPIException;
import com.ibm.designer.domino.napi.NotesDatabase;
import com.ibm.designer.runtime.domino.adapter.ComponentModule;
import com.ibm.domino.xsp.module.nsf.NotesContext;

import jakarta.annotation.Priority;

/**
 * Determines whether a given component is enabled based on its ID being
 * present in the enabled XPages Libraries in the current {@link NotesContent}
 * {@link ComponentModule}.
 * 
 * @author Jesse Gallagher
 * @since 2.7.0
 */
@Priority(2)
public class NSFModuleApplicationPropertyLocator implements ApplicationPropertyLocator {

	@Override
	public boolean isActive() {
		NotesContext ctx = NotesContext.getCurrentUnchecked();
		if(ctx != null) {
			try {
				NotesDatabase database = ctx.getNotesDatabase();
				if(database != null) {
					return true;
				}
			} catch(NotesAPIException e) {
				// Ignore
			}
		}
		return false;
	}

	@Override
	public String getApplicationProperty(String prop, String defaultValue) {
		NotesContext ctx = NotesContext.getCurrentUnchecked();
		try {
			NotesDatabase database = ctx.getNotesDatabase();
			if(database != null) {
				return LibraryUtil.getXspProperties(database).getProperty(prop, defaultValue);
			}
		} catch(NotesAPIException e) {
			throw new RuntimeException(e);
		}
		return defaultValue;
	}

}
