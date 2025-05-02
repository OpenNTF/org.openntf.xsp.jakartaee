package org.openntf.xsp.jakartaee.module.jakartansf.util;

import com.ibm.domino.napi.NException;
import com.ibm.domino.napi.c.Os;
import lotus.domino.Database;
import lotus.domino.NotesException;
import lotus.domino.Session;

public record LSXBEHolder(Session session, Database database, Session sessionAsSigner, Session sessionAsSignerFullAccess, long hSigner) implements AutoCloseable {
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
	}
}
