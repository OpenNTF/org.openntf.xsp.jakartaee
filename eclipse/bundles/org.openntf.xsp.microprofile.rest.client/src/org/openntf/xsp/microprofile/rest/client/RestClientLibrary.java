package org.openntf.xsp.microprofile.rest.client;

import org.osgi.framework.FrameworkUtil;

import com.ibm.xsp.library.AbstractXspLibrary;

/**
 * @since 2.2.0
 */
public class RestClientLibrary extends AbstractXspLibrary {
	public static final String LIBRARY_ID = RestClientLibrary.class.getPackage().getName();

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
