package org.openntf.xsp.jakartaee.module.jakartansf.io;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Optional;

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
	
	public static Optional<URL> getUrl(NotesDatabase db, String res) {
		try {
			// TODO handle "\" in resource name as "/".
			//   Probably best to cache this in the ClassLoader instead of using FileAccess for this
			NotesNote note = FileAccess.getFileByPath(db, res);
			if(note != null) {
				try {
					return Optional.ofNullable(NSFJakartaURL.of(db.getDatabasePath(), res));
				} finally {
					note.recycle();
				}
			} else {
				return Optional.empty();
			}
		} catch (NotesAPIException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static URLConnection openConnection(URL u, String nsfPath, String res) {
		return new NSFJakartaURLConnection(u, nsfPath, res);
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
