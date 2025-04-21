package org.openntf.xsp.jakartaee.module.nsf.io;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Optional;

import com.ibm.commons.util.NotImplementedException;
import com.ibm.commons.util.io.ByteStreamCache;
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
	
	public static Optional<URL> getUrl(String nsfPath, String res) {
		try {
			NotesSession session = new NotesSession();
			NotesDatabase db = session.getDatabase(nsfPath);
			db.open();
			try {
				NotesNote note = FileAccess.getFileByPath(db, res);
				if(note != null) {
					Optional.ofNullable(NSFJakartaURL.of(nsfPath, res));
				} else {
					return Optional.empty();
				}
			} finally {
				session.recycle();
			}
		} catch (NotesAPIException e) {
			throw new RuntimeException(e);
		}
		return Optional.ofNullable(NSFJakartaURL.of(nsfPath, res));
	}
	
	public static URLConnection openConnection(String nsfPath, String res) {
		throw new NotImplementedException("TODO implement openConnection");
	}
	
	public static InputStream openStream(String nsfPath, String res) throws IOException {
		try {
			NotesSession session = new NotesSession();
			NotesDatabase db = session.getDatabase(nsfPath);
			db.open();
			try {
				NotesNote note = FileAccess.getFileByPath(db, res);
				if(note != null) {
					ByteStreamCache cache = new ByteStreamCache();
					FileAccess.readFileContent(note, cache.getOutputStream());
					return cache.getInputStream();
				} else {
					return null;
				}
			} finally {
				session.recycle();
			}
		} catch (NotesAPIException e) {
			throw new IOException(e);
		}
		
	}
}
