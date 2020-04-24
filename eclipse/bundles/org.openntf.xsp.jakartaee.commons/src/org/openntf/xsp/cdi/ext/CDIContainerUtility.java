package org.openntf.xsp.cdi.ext;

import java.io.IOException;

import org.osgi.framework.Bundle;

import com.ibm.designer.domino.napi.NotesAPIException;
import com.ibm.designer.domino.napi.NotesDatabase;
import com.ibm.xsp.application.ApplicationEx;

/**
 * 
 * @author Jesse Gallagher
 * @since 1.2.0
 */
public interface CDIContainerUtility {
	Object getContainer(NotesDatabase database) throws NotesAPIException, IOException;
	Object getContainer(ApplicationEx app);
	Object getContainer(Bundle bundle);
	String getThreadContextDatabasePath();
}
