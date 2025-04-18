package org.openntf.xsp.jakartaee.module.nsf.io;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;

import com.ibm.commons.util.NotImplementedException;
import com.ibm.designer.domino.napi.NotesAPIException;
import com.ibm.designer.domino.napi.NotesDatabase;
import com.ibm.designer.domino.napi.NotesNote;
import com.ibm.designer.domino.napi.NotesSession;
import com.ibm.designer.domino.napi.design.FileAccess;

/**
 * @since 3.4.0
 */
public enum NSFAccess {
	;
	
	public static URLConnection openConnection(String nsfPath, String res) {
		throw new NotImplementedException("TODO implement openConnection");
	}
	
	public static InputStream openStream(String nsfPath, String res) throws IOException {
		try {
			NotesSession session = new NotesSession();
			NotesDatabase db = session.getDatabase(nsfPath);
			NotesNote note = FileAccess.getFileByPath(db, res);
			InputStream is = FileAccess.readFileContentAsInputStream(note);
			return new NSFInputStream(session, is);
		} catch (NotesAPIException e) {
			throw new IOException(e);
		}
		
	}
}
