package org.openntf.xsp.nosql.communication.driver.impl;

public interface DominoConstants {
	/**
	 * The field used to store the UNID of the document during NoSQL
	 * conversion, currently {@value #FIELD_ID}
	 */
	String FIELD_ID = "_id"; //$NON-NLS-1$
	/**
	 * The expected field containing the collection name of the document in
	 * Domino, currently {@value #FIELD_NAME}
	 */
	String FIELD_NAME = "Form"; //$NON-NLS-1$
	
	/**
	 * The field used to store the creation date of the document during
	 * NoSQL conversion, currently {@value #FIELD_CDATE}
	 */
	String FIELD_CDATE = "_cdate"; //$NON-NLS-1$
	/**
	 * The field used to store the last modification date of the document during
	 * NoSQL conversion, currently {@value #FIELD_MDATE}
	 */
	String FIELD_MDATE = "_mdate"; //$NON-NLS-1$
	/**
	 * The field used to store document attachments during NoSQL conversion,
	 * currently {@value #FIELD_ATTACHMENTS}
	 */
	String FIELD_ATTACHMENTS = "_attachments"; //$NON-NLS-1$
}
