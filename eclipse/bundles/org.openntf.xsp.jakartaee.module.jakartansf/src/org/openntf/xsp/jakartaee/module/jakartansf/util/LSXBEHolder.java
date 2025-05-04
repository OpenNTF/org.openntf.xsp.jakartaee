package org.openntf.xsp.jakartaee.module.jakartansf.util;

import com.ibm.domino.napi.NException;
import com.ibm.domino.napi.c.NotesUtil;
import com.ibm.domino.napi.c.Os;
import com.ibm.domino.napi.c.xsp.XSPNative;

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
 * @param hUser a handle to a names list for the user session; may be {@code 0}
 */
public record LSXBEHolder(Session session, Database database, Session sessionAsSigner, Session sessionAsSignerFullAccess, long hSigner, long hUser) implements AutoCloseable {
	@Override
	public void close() {
		try {
			session.recycle();
		} catch(NotesException e) { }
		try {
			sessionAsSigner.recycle();
		} catch(NotesException e) { }
		try {
			sessionAsSignerFullAccess.recycle();
		} catch(NotesException e) { }
		
		try {
			Os.OSMemFree(hSigner);
		} catch(NException e) { }
		
		if(hUser != 0) {
			try {
				Os.OSMemFree(hUser);
			} catch(NException e) { }
		}
	}
	
	/**
	 * Creates a new holder based on cloning the objects contained in this holder.
	 * 
	 * @return a new holder instance, which must be closed separately
	 */
	public LSXBEHolder cloneSessions() {
		try {
			String name = this.session.getEffectiveUserName();
			long hUser = NotesUtil.createUserNameList(this.session.getEffectiveUserName());
			Session session = XSPNative.createXPageSession(name, hUser, true, false);
			Database database = session.getDatabase(this.database.getServer(), this.database.getFilePath());
			XSPNative.setContextDatabase(session, XSPNative.getDBHandle(database));
			
			String signerName = this.sessionAsSigner.getEffectiveUserName();
			long hSigner = NotesUtil.createUserNameList(signerName);
			Session sessionAsSigner = XSPNative.createXPageSessionExt(signerName, hSigner, false, false, false);
			Session sessAsSignerFullAccess = XSPNative.createXPageSessionExt(signerName, hSigner, false, false, false);
			
			return new LSXBEHolder(session, database, sessionAsSigner, sessAsSignerFullAccess, hSigner, hUser);
		} catch(NotesException | NException e) {
			throw new RuntimeException("Encountered exception while cloning Notes objects");
		}
	}
}
