/**
 * Copyright (c) 2018-2025 Contributors to the XPages Jakarta EE Support Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
