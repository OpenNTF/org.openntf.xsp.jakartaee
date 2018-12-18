package org.openntf.xsp.jsonapi;

import com.ibm.xsp.library.AbstractXspLibrary;

public class JSONLibrary extends AbstractXspLibrary {

	public static final String LIBRARY_ID = JSONLibrary.class.getPackage().getName();

	@Override
	public String getLibraryId() {
		return LIBRARY_ID;
	}
	
	@Override
	public String getPluginId() {
		return Activator.getDefault().getBundle().getSymbolicName();
	}
	
	@Override
	public String[] getDependencies() {
		return new String[] {
			"com.ibm.xsp.core.library", //$NON-NLS-1$
			"com.ibm.xsp.extsn.library", //$NON-NLS-1$
			"com.ibm.xsp.designer.library" //$NON-NLS-1$
		};
	}
	
	@Override
	public boolean isGlobalScope() {
		return false;
	}

}
