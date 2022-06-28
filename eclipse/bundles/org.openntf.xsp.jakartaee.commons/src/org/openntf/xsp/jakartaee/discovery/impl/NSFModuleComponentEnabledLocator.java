package org.openntf.xsp.jakartaee.discovery.impl;

import org.openntf.xsp.jakartaee.discovery.ComponentEnabledLocator;
import org.openntf.xsp.jakartaee.util.LibraryUtil;

import com.ibm.designer.runtime.domino.adapter.ComponentModule;
import com.ibm.domino.xsp.module.nsf.NotesContext;

/**
 * Determines whether a given component is enabled based on its ID being
 * present in the enabled XPages Libraries in the current {@link NotesContent}
 * {@link ComponentModule}.
 * 
 * @author Jesse Gallagher
 * @since 2.7.0
 */
public class NSFModuleComponentEnabledLocator implements ComponentEnabledLocator {

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
	public boolean isComponentEnabled(String componentId) {
		NotesContext ctx = NotesContext.getCurrentUnchecked();
		if(ctx != null) {
			ComponentModule module = ctx.getModule();
			if(module != null) {
				return LibraryUtil.usesLibrary(componentId, module);
			}
		}
		return false;
	}

}
