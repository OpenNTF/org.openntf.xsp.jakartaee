package org.openntf.xsp.cdi.provider;

import org.openntf.xsp.cdi.ext.CDIContainerLocator;
import org.openntf.xsp.cdi.ext.CDIContainerUtility;
import org.openntf.xsp.jakartaee.util.LibraryUtil;

import com.ibm.xsp.application.ApplicationEx;

import jakarta.annotation.Priority;

/**
 * This {@link CDIContainerUtility} implementation attempts to locate a CDI
 * container based on a contextual {@link ApplicationEx} instance.
 * 
 * @author Jesse Gallagher
 * @since 2.8.0
 */
@Priority(2)
public class ApplicationExCDIContainerLocator implements CDIContainerLocator {
	@Override
	public Object getContainer() {
		CDIContainerUtility util = LibraryUtil.findRequiredExtension(CDIContainerUtility.class);
		
		ApplicationEx application = ApplicationEx.getInstance();
		if(application != null) {
			return util.getContainer(application);
		} else {
			return null;
		}
	}
}
