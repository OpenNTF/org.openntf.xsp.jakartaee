package org.openntf.xsp.jakartaee.module.jakartansf.util;

import java.util.Objects;

import lotus.domino.Database;
import lotus.domino.NotesException;
import lotus.domino.Session;

/**
 * @since 3.4.0
 */
public enum NSFModuleUtil {
	;
	
	public static Database openDatabase(Session session, String path) throws NotesException {
		Objects.requireNonNull(session);
		Objects.requireNonNull(path, "path cannot be null"); //$NON-NLS-1$
		
		int bangIndex = path.indexOf("!!"); //$NON-NLS-1$
		String serverName, fileName;
		if(bangIndex < 0) {
			serverName = ""; //$NON-NLS-1$
			fileName = path;
		} else {
			serverName = path.substring(0, bangIndex);
			fileName = path.substring(bangIndex+2);
		}
		
		return session.getDatabase(serverName, fileName);
	}
}
