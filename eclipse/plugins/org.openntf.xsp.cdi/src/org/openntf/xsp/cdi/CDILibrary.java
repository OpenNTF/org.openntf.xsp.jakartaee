package org.openntf.xsp.cdi;

import java.util.Arrays;

import com.ibm.xsp.application.ApplicationEx;
import com.ibm.xsp.library.AbstractXspLibrary;

public class CDILibrary extends AbstractXspLibrary {
	public static final String LIBRARY_ID = CDILibrary.class.getPackage().getName();

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
	
	@Override
	public String[] getFacesConfigFiles() {
		return new String[] {
			"/res/jsf.xml" //$NON-NLS-1$
		};
	}

	public static boolean usesLibrary(ApplicationEx app) {
		String prop = app.getProperty("xsp.library.depends", ""); //$NON-NLS-1$ //$NON-NLS-2$
		return Arrays.asList(prop.split(",")).contains(LIBRARY_ID); //$NON-NLS-1$
	}

}
