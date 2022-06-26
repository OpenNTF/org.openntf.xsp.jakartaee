/**
 * Copyright © 2018-2022 Contributors to the XPages Jakarta EE Support Project
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
package org.openntf.xsp.nosql.communication.driver;

import java.util.Arrays;
import java.util.Vector;

import org.openntf.xsp.nosql.mapping.extension.ViewEntries;

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
	 * conversion, currently {@value #FIELD_ID}.
	 * 
	 * <p>This value is shared with the default value for the
	 * {@link jakarta.nosql.mapping.Id @Id} annotation from Jakarta NoSQL.</p>
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
	 * The field used to request storage of a DXL representation of the document during
	 * NoSQL conversion, currently {@value #FIELD_DXL}
	 */
	String FIELD_DXL = "@dxl"; //$NON-NLS-1$
	/**
	 * The field used to store the position tumbler when using the {@link ViewEntries @ViewEntries}
	 * annotation, currently {@value #FIELD_POSITION}
	 */
	String FIELD_POSITION = "@position"; //$NON-NLS-1$
	/**
	 * The field used to store the type of entry when using the {@link ViewEntries @ViewEntries}
	 * annotation, currently {@value #FIELD_ENTRY_TYPE}
	 */
	String FIELD_ENTRY_TYPE = "@entrytype"; //$NON-NLS-1$
	/**
	 * The field used to request storage of the unread status of a document or view entry
	 * during NoSQL conversion, currently {@value #FIELD_READ}
	 */
	String FIELD_READ = "@read"; //$NON-NLS-1$
	/**
	 * The field used to request storage of the size in bytes of a document during NoSQL
	 * conversion, currently {@value #FIELD_SIZE}
	 */
	String FIELD_SIZE = "@size"; //$NON-NLS-1$
	
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
	
	/**
	 * This header is used to denote the stored Java class for "MIMEBean"-type storage.
	 * @since 2.6.0
	 */
	String HEADER_JAVA_CLASS = "X-Java-Class"; //$NON-NLS-1$
	/**
	 * This MIME type is used to indicate a serialized Java object for "MIMEBean"-type storage.
	 * @since 2.6.0
	 */
	String MIME_TYPE_SERIALIZED_OBJECT = "application/x-java-serialized-object"; //$NON-NLS-1$
}
