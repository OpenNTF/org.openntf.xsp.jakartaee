package org.openntf.xsp.jakarta.persistence;

import org.osgi.framework.FrameworkUtil;

import com.ibm.xsp.library.AbstractXspLibrary;

/**
 * 
 * @author Jesse Gallagher
 * @since 2.11.0
 */
public class PersistenceLibrary extends AbstractXspLibrary {

	public static final String LIBRARY_ID = PersistenceLibrary.class.getPackage().getName();

	@Override
	public String getLibraryId() {
		return LIBRARY_ID;
	}
	
	@Override
	public String getPluginId() {
		return FrameworkUtil.getBundle(getClass()).getSymbolicName();
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
