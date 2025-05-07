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

import com.ibm.domino.napi.NException;
import com.ibm.domino.napi.c.BackendBridge;
import com.ibm.domino.napi.c.Os;

import lotus.domino.Database;
import lotus.domino.NotesException;
import lotus.domino.Session;

/**
 * Container for lotus.domino and similar entities related to a specific request.
 * 
 * @param session the user-specific session object
 * @param database the user-specific database object
 * @param sessionAsSigner a session as an elevated user
 * @param sessionAsSignerFullAccess a session as an elevated user and marked as "full access"
 * @param hSigner a handle to the names list for the signer sessions
 */
public record LSXBEHolder(Session session, Database database, Session sessionAsSigner, Session sessionAsSignerFullAccess, long hSigner) implements AutoCloseable {
	@Override
	public void close() {
		try {
			BackendBridge.setNoRecycle(session, database, false);
			database.recycle();
		} catch(NotesException e) { }
		
		try {
			BackendBridge.setNoRecycle(session, session, false);
			session.recycle();
		} catch(NotesException e) { }
		try {
			BackendBridge.setNoRecycle(sessionAsSigner, sessionAsSigner, false);
			sessionAsSigner.recycle();
		} catch(NotesException e) { }
		try {
			BackendBridge.setNoRecycle(sessionAsSignerFullAccess, sessionAsSignerFullAccess, false);
			sessionAsSignerFullAccess.recycle();
		} catch(NotesException e) { }
		
		try {
			Os.OSMemFree(hSigner);
		} catch(NException e) { }
	}
}
