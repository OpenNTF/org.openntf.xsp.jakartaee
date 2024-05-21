package org.openntf.xsp.jakartaee.core.library;

import org.openntf.xsp.jakartaee.util.LibraryUtil;
import org.osgi.framework.FrameworkUtil;

import com.ibm.xsp.library.AbstractXspLibrary;
import com.ibm.xsp.library.CoreLibrary;

public class JakartaCoreLibrary extends AbstractXspLibrary {

	@Override
	public String getLibraryId() {
		return LibraryUtil.LIBRARY_CORE;
	}
	
	@Override
	public String getPluginId() {
		return FrameworkUtil.getBundle(getClass()).getSymbolicName();
	}
	
	@Override
	public String[] getDependencies() {
		return new String[] {
			CoreLibrary.LIBRARY_ID
		};
	}
	
	@Override
	public boolean isGlobalScope() {
		return false;
	}

	@Override
	public String[] getFacesConfigFiles() {
		return new String[] {
			"/cdi-jsf.xml" //$NON-NLS-1$
		};
	}
}
