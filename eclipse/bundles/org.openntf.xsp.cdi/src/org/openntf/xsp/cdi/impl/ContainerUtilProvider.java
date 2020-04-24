package org.openntf.xsp.cdi.impl;

import java.io.IOException;

import org.openntf.xsp.cdi.ext.CDIContainerUtility;
import org.openntf.xsp.cdi.util.ContainerUtil;
import org.osgi.framework.Bundle;

import com.ibm.designer.domino.napi.NotesAPIException;
import com.ibm.designer.domino.napi.NotesDatabase;
import com.ibm.xsp.application.ApplicationEx;

/**
 * 
 * @author Jesse Gallagher
 * @since 1.2.0
 */
public class ContainerUtilProvider implements CDIContainerUtility {

	@Override
	public Object getContainer(NotesDatabase database) throws NotesAPIException, IOException {
		return ContainerUtil.getContainer(database);
	}

	@Override
	public Object getContainer(ApplicationEx app) {
		return ContainerUtil.getContainer(app);
	}

	@Override
	public Object getContainer(Bundle bundle) {
		return ContainerUtil.getContainer(bundle);
	}

	@Override
	public String getThreadContextDatabasePath() {
		return ContainerUtil.getThreadContextDatabasePath();
	}
}
