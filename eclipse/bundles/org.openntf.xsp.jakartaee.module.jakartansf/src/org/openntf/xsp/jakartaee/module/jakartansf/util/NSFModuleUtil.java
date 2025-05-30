package org.openntf.xsp.jakartaee.module.jakartansf.util;

import java.util.Collection;
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

	@SuppressWarnings("unchecked")
	public static Collection<String> getNamesList(String userName) {
		try {
			// DominoServer is blocked by our normal ClassLoader, so use the root one reflectively
			Object server = Class.forName("lotus.notes.addins.DominoServer", true, ClassLoader.getSystemClassLoader()).getConstructor().newInstance(); //$NON-NLS-1$
			return (Collection<String>)server.getClass().getMethod("getNamesList", String.class).invoke(server, userName); //$NON-NLS-1$
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
}
