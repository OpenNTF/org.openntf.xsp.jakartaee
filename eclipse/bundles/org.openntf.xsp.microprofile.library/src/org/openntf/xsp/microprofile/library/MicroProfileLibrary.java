package org.openntf.xsp.microprofile.library;

import org.openntf.xsp.jakartaee.util.LibraryUtil;
import org.osgi.framework.FrameworkUtil;

import com.ibm.xsp.library.AbstractXspLibrary;
import com.ibm.xsp.library.CoreLibrary;

public class MicroProfileLibrary extends AbstractXspLibrary {

	@Override
	public String getLibraryId() {
		return LibraryUtil.LIBRARY_MICROPROFILE;
	}
	
	@Override
	public String getPluginId() {
		return FrameworkUtil.getBundle(getClass()).getSymbolicName();
	}
	
	@Override
	public String[] getDependencies() {
		return new String[] {
			CoreLibrary.LIBRARY_ID,
			LibraryUtil.LIBRARY_CORE
		};
	}
	
	@Override
	public boolean isGlobalScope() {
		return false;
	}

}
