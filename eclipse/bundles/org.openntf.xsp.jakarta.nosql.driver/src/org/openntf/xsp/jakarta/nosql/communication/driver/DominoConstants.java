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
package org.openntf.xsp.jakarta.nosql.communication.driver;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import org.openntf.xsp.jakarta.nosql.mapping.extension.ViewEntries;

/**
 * Contains constant values used by the Domino NoSQL driver for mapping
 * and querying.
 *
 * @author Jesse Gallagher
 * @since 2.6.0
 */
public enum DominoConstants {
	;

	/**
	 * The field used to store the UNID of the document during NoSQL
	 * conversion, currently {@value #FIELD_ID}.
	 *
	 * <p>This value is shared with the default value for the
	 * {@link jakarta.nosql.mapping.Id @Id} annotation from Jakarta NoSQL.</p>
	 */
	public static final String FIELD_ID = "_id"; //$NON-NLS-1$
	/**
	 * The expected field containing the collection name of the document in
	 * Domino, currently {@value #FIELD_NAME}
	 */
	public static final String FIELD_NAME = "Form"; //$NON-NLS-1$

	/**
	 * The field used to store the creation date of the document during
	 * NoSQL conversion, currently {@value #FIELD_CDATE}
	 */
	public static final String FIELD_CDATE = "@cdate"; //$NON-NLS-1$

	/**
	 * The field used to store the last modification date of the document during
	 * NoSQL conversion, currently {@value #FIELD_MDATE}
	 */
	public static final String FIELD_MDATE = "@mdate"; //$NON-NLS-1$

	/**
	 * The field used to store document attachments during NoSQL conversion,
	 * currently {@value #FIELD_ATTACHMENTS}
	 */
	public static final String FIELD_ATTACHMENTS = "@attachments"; //$NON-NLS-1$

	/**
	 * The field used to request storage of a DXL representation of the document during
	 * NoSQL conversion, currently {@value #FIELD_DXL}
	 */
	public static final String FIELD_DXL = "@dxl"; //$NON-NLS-1$
	/**
	 * The field used to store the position tumbler when using the {@link ViewEntries @ViewEntries}
	 * annotation, currently {@value #FIELD_POSITION}
	 */
	public static final String FIELD_POSITION = "@position"; //$NON-NLS-1$
	/**
	 * The field used to store the type of entry when using the {@link ViewEntries @ViewEntries}
	 * annotation, currently {@value #FIELD_ENTRY_TYPE}
	 */
	public static final String FIELD_ENTRY_TYPE = "@entrytype"; //$NON-NLS-1$
	/**
	 * The field used to request storage of the unread status of a document or view entry
	 * during NoSQL conversion, currently {@value #FIELD_READ}
	 */
	public static final String FIELD_READ = "@read"; //$NON-NLS-1$
	/**
	 * The field used to request storage of the size in bytes of a document during NoSQL
	 * conversion, currently {@value #FIELD_SIZE}
	 */
	public static final String FIELD_SIZE = "@size"; //$NON-NLS-1$
	/**
	 * The field used to request storage of a document's note ID, currently {@value #FIELD_NOTEID}
	 * @since 2.8.0
	 */
	public static final String FIELD_NOTEID = "@noteid"; //$NON-NLS-1$
	/**
	 * The field used to request storage of a document's last-accessed date, currently
	 * {@value #FIELD_ADATE}
	 * @since 2.8.0
	 */
	public static final String FIELD_ADATE = "@adate"; //$NON-NLS-1$
	/**
	 * The field used to request storage of a document's last-modified-in-this-file date,
	 * currently {@value #FIELD_MODIFIED_IN_THIS_FILE}
	 * @since 2.8.0
	 */
	public static final String FIELD_MODIFIED_IN_THIS_FILE = "@modifiedinthisfile"; //$NON-NLS-1$
	/**
	 * The field used to request storage of a document's added-to-this-file date, currently
	 * {@value #FIELD_ADDED}
	 * @since 2.8.0
	 */
	public static final String FIELD_ADDED = "@added"; //$NON-NLS-1$
	/**
	 * The field used to request storage of an ETag-compatible value for the document, which
	 * will represent the document's ID and last modification time. The value is currently
	 * {@value #FIELD_ETAG}
	 * @since 2.8.0
	 */
	public static final String FIELD_ETAG = "@etag"; //$NON-NLS-1$
	/**
	 * The field used to request storage of the file path of the database housing the document,
	 * currently {@value #FIELD_FILEPATH}
	 * @since 2.8.0
	 */
	public static final String FIELD_FILEPATH = "@filepath"; //$NON-NLS-1$
	/**
	 * The field used to request storage of the server hosting the database housing the document,
	 * currently {@value #FIELD_SERVER}
	 * @since 2.8.0
	 */
	public static final String FIELD_SERVER = "@server"; //$NON-NLS-1$
	/**
	 * The field used to request storage of the replica ID of the database housing the document,
	 * currently {@value #FIELD_REPLICAID}
	 * @since 2.8.0
	 */
	public static final String FIELD_REPLICAID = "@replicaid"; //$NON-NLS-1$
	/**
	 * The field used to request storage of sibling entry count when reading view entries,
	 * currently {@value #FIELD_SIBLINGCOUNT}
	 * @since 2.9.0
	 */
	public static final String FIELD_SIBLINGCOUNT = "@siblingcount"; //$NON-NLS-1$
	/**
	 * The field used to request storage of child entry count when reading view entries,
	 * currently {@value #FIELD_CHILDCOUNT}
	 * @since 2.9.0
	 */
	public static final String FIELD_CHILDCOUNT = "@childcount"; //$NON-NLS-1$
	/**
	 * The field used to request storage of descendant entry count when reading view entries,
	 * currently {@value #FIELD_DESCENDANTCOUNT}
	 * @since 2.9.0
	 */
	public static final String FIELD_DESCENDANTCOUNT = "@descendantcount"; //$NON-NLS-1$
	/**
	 * The field used to request storage of the entry column indent level when reading
	 * view entries, currently {@value #FIELD_COLUMNINDENTLEVEL}
	 * @since 2.9.0
	 */
	public static final String FIELD_COLUMNINDENTLEVEL = "@columnindentlevel"; //$NON-NLS-1$
	/**
	 * The field used to request storage of the entry indent level when reading
	 * view entries, currently {@value #FIELD_INDENTLEVEL}
	 * @since 2.9.0
	 */
	public static final String FIELD_INDENTLEVEL = "@indentlevel"; //$NON-NLS-1$
	/**
	 * The field used to request storage of the full-text search score,
	 * currently {@value #FIELD_FTSEARCHSCORE}
	 * @since 2.9.0
	 */
	public static final String FIELD_FTSEARCHSCORE = "@ftsearchscore"; //$NON-NLS-1$
	/**
	 * The field used to request storage of the name of the note, currently
	 * {@value #FIELD_NOTENAME}
	 * @since 2.13.0
	 */
	public static final String FIELD_NOTENAME = "@notename"; //$NON-NLS-1$
	/**
	 * The field used to request storage of the profile name of the note, currently
	 * {@value #FIELD_PROFILENAME}
	 * @since 2.13.0
	 */
	public static final String FIELD_PROFILENAME = "@profilename"; //$NON-NLS-1$
	/**
	 * The field used to request storage of the qualifying user name of a named or
	 * document, currently {@value #FIELD_USERNAME}
	 * @since 2.13.0
	 */
	public static final String FIELD_USERNAME = "@username"; //$NON-NLS-1$
	/**
	 * The field used to request storage of the qualifying user name of a profile
	 * document, currently {@value #FIELD_USERNAME}
	 * @since 2.13.0
	 */
	public static final String FIELD_PROFILEKEY = "@profilekey"; //$NON-NLS-1$
	/**
	 * The field used to request storage of the parent-doc UNID as a string,
	 * marking the document as a response document, currently
	 * {@value #FIELD_PARENTUNID}.
	 * @since 3.3.0
	 */
	public static final String FIELD_PARENTUNID = "$REF"; //$NON-NLS-1$

	/**
	 * Options used when converting composite data to HTML. This list is based
	 * on the options used by XPages.
	 */
	public static final Vector<String> HTML_CONVERSION_OPTIONS = new Vector<>(Arrays.asList(
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
	public static final String HEADER_JAVA_CLASS = "X-Java-Class"; //$NON-NLS-1$
	/**
	 * This MIME type is used to indicate a serialized Java object for "MIMEBean"-type storage.
	 * @since 2.6.0
	 */
	public static final String MIME_TYPE_SERIALIZED_OBJECT = "application/x-java-serialized-object"; //$NON-NLS-1$
	
	/**
	 * Local constant for Item.MIME_PART to avoid problems with different compilers
	 * 
	 * @since 3.4.0
	 */
	public static final int TYPE_MIME_PART = 0x19;

	public static final Collection<String> SYSTEM_FIELDS;
	public static final Collection<String> SKIP_WRITING_FIELDS;
	static {
		Set<String> systemFields = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
		systemFields.addAll(Arrays.asList(
			FIELD_ID,
			FIELD_CDATE,
			FIELD_MDATE,
			FIELD_ATTACHMENTS,
			FIELD_DXL,
			FIELD_POSITION,
			FIELD_ENTRY_TYPE,
			FIELD_READ,
			FIELD_NOTEID,
			FIELD_ADATE,
			FIELD_ADDED,
			FIELD_MODIFIED_IN_THIS_FILE,
			FIELD_ETAG,
			FIELD_REPLICAID,
			FIELD_SERVER,
			FIELD_FILEPATH,
			FIELD_SIBLINGCOUNT,
			FIELD_CHILDCOUNT,
			FIELD_DESCENDANTCOUNT,
			FIELD_COLUMNINDENTLEVEL,
			FIELD_INDENTLEVEL,
			FIELD_FTSEARCHSCORE,
			FIELD_NOTENAME,
			FIELD_PROFILENAME,
			FIELD_USERNAME,
			FIELD_PROFILEKEY
		));
		SYSTEM_FIELDS = Collections.unmodifiableSet(systemFields);

		Set<String> skipWritingFields = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
		skipWritingFields.add("$FILE"); //$NON-NLS-1$
		skipWritingFields.addAll(SYSTEM_FIELDS);
		SKIP_WRITING_FIELDS = Collections.unmodifiableSet(skipWritingFields);
	}
}
