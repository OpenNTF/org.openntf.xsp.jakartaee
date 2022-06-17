package org.openntf.xsp.nosql.communication.driver.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.eclipse.jnosql.communication.driver.attachment.EntityAttachment;
import org.openntf.xsp.nosql.communication.driver.DatabaseSupplier;

import com.ibm.commons.util.StringUtil;
import jakarta.activation.MimetypesFileTypeMap;
import lotus.domino.Database;
import lotus.domino.Document;
import lotus.domino.EmbeddedObject;
import lotus.domino.NotesException;

public class DominoDocumentAttachment implements EntityAttachment {
	private final DatabaseSupplier databaseSupplier;
	private final String unid;
	private final String attachmentName;
	private Long lastModified;
	private String contentType;
	private Long length;
	
	public DominoDocumentAttachment(DatabaseSupplier databaseSupplier, String unid, String attachmentName) {
		this.databaseSupplier = databaseSupplier;
		this.unid = unid;
		this.attachmentName = attachmentName;
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
		cacheMeta();
		
		String contentType = URLConnection.guessContentTypeFromName(this.attachmentName);
		if(StringUtil.isNotEmpty(contentType)) {
			return contentType;
		}
		
		MimetypesFileTypeMap fileTypeMap = new MimetypesFileTypeMap();
	    contentType = fileTypeMap.getContentType(this.attachmentName);
		if(StringUtil.isNotEmpty(contentType)) {
			return contentType;
		}
		
		return "application/octet-stream"; //$NON-NLS-1$
	}

	@Override
	public InputStream getData() throws IOException {
		// TODO handle recycling on this
		try {
			return getEmbeddedObject().getInputStream();
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
				// TODO try to guess this
				this.contentType = "application/octet-stream"; //$NON-NLS-1$
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
}
