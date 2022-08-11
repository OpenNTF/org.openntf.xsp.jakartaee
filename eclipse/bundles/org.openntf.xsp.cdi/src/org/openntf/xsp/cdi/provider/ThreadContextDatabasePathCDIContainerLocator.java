package org.openntf.xsp.cdi.provider;

import org.openntf.xsp.cdi.ext.CDIContainerLocator;
import org.openntf.xsp.cdi.ext.CDIContainerUtility;
import org.openntf.xsp.jakartaee.util.LibraryUtil;

import jakarta.annotation.Priority;

/**
 * This {@link CDIContainerLocator} looks for a thread-context database path,
 * which may be specified as an overried by user applications.
 * 
 * @author Jesse Gallagher
 * @since 2.8.0
 */
@Priority(3)
public class ThreadContextDatabasePathCDIContainerLocator implements CDIContainerLocator {

	@Override
	public String getNsfPath() {
		CDIContainerUtility util = LibraryUtil.findRequiredExtension(CDIContainerUtility.class);
		return util.getThreadContextDatabasePath();
	}

}
