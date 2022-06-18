package org.openntf.xsp.nosql.communication.driver.impl;

import java.util.Arrays;
import java.util.Vector;

/**
 * Contains constant values used by the Domino NoSQL driver for mapping
 * and querying.
 * 
 * @author Jesse Gallagher
 * @since 2.6.0
 */
public interface DominoConstants {
	/**
	 * The field used to store the UNID of the document during NoSQL
	 * conversion, currently {@value #FIELD_ID}
	 */
	String FIELD_ID = "@unid"; //$NON-NLS-1$
	/**
	 * The expected field containing the collection name of the document in
	 * Domino, currently {@value #FIELD_NAME}
	 */
	String FIELD_NAME = "Form"; //$NON-NLS-1$
	
	/**
	 * The field used to store the creation date of the document during
	 * NoSQL conversion, currently {@value #FIELD_CDATE}
	 */
	String FIELD_CDATE = "@cdate"; //$NON-NLS-1$
	/**
	 * The field used to store the last modification date of the document during
	 * NoSQL conversion, currently {@value #FIELD_MDATE}
	 */
	String FIELD_MDATE = "@mdate"; //$NON-NLS-1$
	/**
	 * The field used to store document attachments during NoSQL conversion,
	 * currently {@value #FIELD_ATTACHMENTS}
	 */
	String FIELD_ATTACHMENTS = "@attachments"; //$NON-NLS-1$
	/**
	 * Options used when converting composite data to HTML. This list is based
	 * on the options used by XPages.
	 */
	Vector<String> HTML_CONVERSION_OPTIONS = new Vector<>(Arrays.asList(
		"AutoClass=2", //$NON-NLS-1$
		"RowAtATimeTableAlt=2", //$NON-NLS-1$
		"SectionAlt=1", //$NON-NLS-1$
		"XMLCompatibleHTML=1", //$NON-NLS-1$
		"AttachmentLink=1", //$NON-NLS-1$
		"TableStyle=1", //$NON-NLS-1$
		"FontConversion=1", //$NON-NLS-1$
		"LinkHandling=1", //$NON-NLS-1$
		"ListFidelity=1", //$NON-NLS-1$
		"ParagraphIndent=2" //$NON-NLS-1$
	));
}
