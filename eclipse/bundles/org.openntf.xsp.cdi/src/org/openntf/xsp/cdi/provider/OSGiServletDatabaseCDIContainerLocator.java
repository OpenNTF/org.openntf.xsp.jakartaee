package org.openntf.xsp.cdi.provider;

import org.openntf.xsp.cdi.ext.CDIContainerLocator;
import org.openntf.xsp.cdi.ext.CDIContainerUtility;
import org.openntf.xsp.jakartaee.util.LibraryUtil;

import com.ibm.designer.domino.napi.NotesDatabase;
import com.ibm.domino.osgi.core.context.ContextInfo;

import jakarta.annotation.Priority;

/**
 * This {@link CDIContainerLocator} looks for a contextual database
 * in an OSGi Servlet request for a CDI container.
 * 
 * @author Jesse Gallagher
 * @since 2.8.0
 */
@Priority(1)
public class OSGiServletDatabaseCDIContainerLocator implements CDIContainerLocator {

	@Override
	public Object getContainer() {
		CDIContainerUtility util = LibraryUtil.findRequiredExtension(CDIContainerUtility.class);
		
		try {
			NotesDatabase database = ContextInfo.getServerDatabase();
			if(database != null) {
				return util.getContainer(database);
			}
		} catch(Throwable t) {
			t.printStackTrace();
		}
		return null;
	}

}
