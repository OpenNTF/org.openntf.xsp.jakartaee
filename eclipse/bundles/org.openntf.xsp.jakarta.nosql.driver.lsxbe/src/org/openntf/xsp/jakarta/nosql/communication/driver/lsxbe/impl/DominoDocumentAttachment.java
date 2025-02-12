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
package org.openntf.xsp.jakarta.nosql.communication.driver.lsxbe.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.util.function.Supplier;

import com.ibm.commons.util.StringUtil;

import org.eclipse.jnosql.communication.driver.attachment.EntityAttachment;

import jakarta.activation.MimetypesFileTypeMap;
import lotus.domino.Database;
import lotus.domino.Document;
import lotus.domino.EmbeddedObject;
import lotus.domino.NotesException;

public class DominoDocumentAttachment implements EntityAttachment {
	private final Supplier<Database> databaseSupplier;
	private final String unid;
	private final String attachmentName;
	private Long lastModified;
	private String contentType;
	private Long length;

	public DominoDocumentAttachment(final Supplier<Database> databaseSupplier, final String unid, final String attachmentName) {
		this.databaseSupplier = databaseSupplier;
		this.unid = unid;
		this.attachmentName = attachmentName;
		this.contentType = guessContentType(attachmentName);
	}

	@Override
	public String getName() {
		return this.attachmentName;
	}

	@Override
	public long getLastModified() {
		cacheMeta();
		return this.lastModified;
	}

	@Override
	public String getContentType() {
		return this.contentType;
	}

	@Override
	public InputStream getData() throws IOException {
		try {
			return new EmbeddedObjectInputStream(getEmbeddedObject());
		} catch (NotesException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public long getLength() {
		cacheMeta();
		return this.length;
	}

	@Override
	public String toString() {
		return String.format( "DominoDocumentAttachment [unid=%s, attachmentName=%s]", unid, attachmentName); //$NON-NLS-1$
	}

	// *******************************************************************************
	// * Internal utility methods
	// *******************************************************************************


	private synchronized void cacheMeta() {
		if(this.lastModified == null) {
			try {
				EmbeddedObject eo = getEmbeddedObject();
				this.lastModified = eo.getFileModified().toJavaDate().getTime();
				this.length = (long)eo.getFileSize();
				eo.recycle();
			} catch(NotesException ne) {
				throw new RuntimeException(ne);
			}
		}
	}

	private EmbeddedObject getEmbeddedObject() {
		try {
			Database database = databaseSupplier.get();
			Document doc = database.getDocumentByUNID(unid);
			return doc.getAttachment(this.attachmentName);
		} catch(NotesException ne) {
			throw new RuntimeException(ne);
		}
	}

	private static String guessContentType(final String fileName) {
		String contentType = URLConnection.guessContentTypeFromName(fileName);
		if(StringUtil.isNotEmpty(contentType)) {
			return contentType;
		}

		MimetypesFileTypeMap fileTypeMap = new MimetypesFileTypeMap();
	    contentType = fileTypeMap.getContentType(fileName);
		if(StringUtil.isNotEmpty(contentType)) {
			return contentType;
		}

		return "application/octet-stream"; //$NON-NLS-1$
	}

	private static class EmbeddedObjectInputStream extends InputStream {
		private final EmbeddedObject eo;
		private final InputStream delegate;

		public EmbeddedObjectInputStream(final EmbeddedObject eo) throws NotesException {
			this.eo = eo;
			this.delegate = eo.getInputStream();
		}

		@Override
		public int read() throws IOException {
			return delegate.read();
		}

		@Override
		public int read(final byte[] b) throws IOException {
			return delegate.read(b);
		}

		@Override
		public int read(final byte[] b, final int off, final int len) throws IOException {
			return delegate.read(b, off, len);
		}

		@Override
		public long skip(final long n) throws IOException {
			return delegate.skip(n);
		}

		@Override
		public int available() throws IOException {
			return delegate.available();
		}

		@Override
		public void close() throws IOException {
			super.close();
			delegate.close();
			try {
				eo.recycle();
			} catch (NotesException e) {

			}
		}

		@Override
		public synchronized void mark(final int readlimit) {
			delegate.mark(readlimit);
		}

		@Override
		public synchronized void reset() throws IOException {
			delegate.reset();
		}

		@Override
		public boolean markSupported() {
			return delegate.markSupported();
		}
	}
}
