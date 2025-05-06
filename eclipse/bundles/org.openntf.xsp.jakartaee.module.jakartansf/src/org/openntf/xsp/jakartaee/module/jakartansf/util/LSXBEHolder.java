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
