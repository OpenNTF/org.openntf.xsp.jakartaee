package org.openntf.xsp.nosql.communication.driver;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.jnosql.communication.driver.attachment.EntityAttachment;

import lotus.domino.DateTime;
import lotus.domino.Document;
import lotus.domino.EmbeddedObject;
import lotus.domino.NotesException;

/**
 * 
 * @author Jesse Gallagher
 * @since 1.4.0
 */
public class DominoDocumentAttachment implements EntityAttachment {
	private final DatabaseSupplier databaseSupplier;
	private final String unid;
	private final String name;
	
	public DominoDocumentAttachment(DatabaseSupplier databaseSupplier, String unid, EmbeddedObject obj) throws NotesException {
		this.databaseSupplier = databaseSupplier;
		this.unid = unid;
		this.name = obj.getSource();
	}

	@Override
	public String getContentType() {
		// TODO Auto-generated method stub
		return "application/octet-stream";
	}

	@Override
	public InputStream getData() throws IOException {
		try {
			Document doc = getDocument();
			try {
				EmbeddedObject eo = doc.getAttachment(name);
				return new EmbeddedObjectInputStream(eo, eo.getInputStream());
			} finally {
				doc.recycle();
			}
		} catch(NotesException e) {
			throw new IOException(e);
		}
	}

	@Override
	public long getLastModified() {
		try {
			Document doc = getDocument();
			try {
				EmbeddedObject eo = doc.getAttachment(name);
				try {
					DateTime dt = eo.getFileModified();
					try {
						return dt.toJavaDate().getTime();
					} finally {
						dt.recycle();
					}
				} finally {
					eo.recycle();
				}
			} finally {
				doc.recycle();
			}
		} catch(NotesException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public long getLength() {
		try {
			Document doc = getDocument();
			try {
				EmbeddedObject eo = doc.getAttachment(name);
				try {
					return eo.getFileSize();
				} finally {
					eo.recycle();
				}
			} finally {
				doc.recycle();
			}
		} catch(NotesException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String getName() {
		return name;
	}
	
	// *******************************************************************************
	// * Internal utilities
	// *******************************************************************************
	
	private static class EmbeddedObjectInputStream extends InputStream {
		private final EmbeddedObject eo;
		private final InputStream is;
		
		public EmbeddedObjectInputStream(EmbeddedObject eo, InputStream is) {
			this.eo = eo;
			this.is = is;
		}

		public int read() throws IOException {
			return is.read();
		}

		public int read(byte[] b) throws IOException {
			return is.read(b);
		}

		public boolean equals(Object obj) {
			return is.equals(obj);
		}

		public int read(byte[] b, int off, int len) throws IOException {
			return is.read(b, off, len);
		}

		public long skip(long n) throws IOException {
			return is.skip(n);
		}

		public int available() throws IOException {
			return is.available();
		}

		public void close() throws IOException {
			is.close();
			try {
				eo.recycle();
			} catch (NotesException e) {
				// Ignore
			}
		}

		public void mark(int readlimit) {
			is.mark(readlimit);
		}

		public void reset() throws IOException {
			is.reset();
		}

		public boolean markSupported() {
			return is.markSupported();
		}
		
		
	}

	private Document getDocument() throws NotesException {
		return databaseSupplier.get().getDocumentByUNID(unid);
	}
}
