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
package org.openntf.xsp.nosql.communication.driver.lsxbe.impl;


import static java.util.Objects.requireNonNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.text.MessageFormat;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.Vector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jnosql.communication.driver.attachment.EntityAttachment;
import org.eclipse.jnosql.mapping.reflection.ClassMapping;
import org.openntf.xsp.nosql.communication.driver.DominoConstants;
import org.openntf.xsp.nosql.communication.driver.impl.AbstractEntityConverter;
import org.openntf.xsp.nosql.communication.driver.lsxbe.DatabaseSupplier;
import org.openntf.xsp.nosql.communication.driver.lsxbe.util.DocumentCollectionIterator;
import org.openntf.xsp.nosql.communication.driver.lsxbe.util.DominoNoSQLUtil;
import org.openntf.xsp.nosql.communication.driver.lsxbe.util.LoaderObjectInputStream;
import org.openntf.xsp.nosql.communication.driver.lsxbe.util.ViewNavigatorIterator;
import org.openntf.xsp.nosql.mapping.extension.DXLExport;
import org.openntf.xsp.nosql.mapping.extension.EntryType;
import org.openntf.xsp.nosql.mapping.extension.ItemFlags;
import org.openntf.xsp.nosql.mapping.extension.ItemStorage;

import com.ibm.commons.util.StringUtil;

import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.nosql.ServiceLoaderProvider;
import jakarta.nosql.ValueWriter;
import jakarta.nosql.document.Document;
import jakarta.nosql.document.DocumentEntity;
import lotus.domino.Database;
import lotus.domino.DateTime;
import lotus.domino.DocumentCollection;
import lotus.domino.DxlExporter;
import lotus.domino.EmbeddedObject;
import lotus.domino.Item;
import lotus.domino.MIMEEntity;
import lotus.domino.MIMEHeader;
import lotus.domino.NotesException;
import lotus.domino.RichTextItem;
import lotus.domino.Session;
import lotus.domino.View;
import lotus.domino.ViewColumn;
import lotus.domino.ViewEntry;
import lotus.domino.ViewEntryCollection;
import lotus.domino.ViewNavigator;

/**
 * Utility methods for converting between Domino and NoSQL entities.
 * 
 * @author Jesse Gallagher
 * @since 2.3.0
 */
public class LSXBEEntityConverter extends AbstractEntityConverter {
	
	private final DatabaseSupplier databaseSupplier;
	private final Jsonb jsonb;
	
	public LSXBEEntityConverter(DatabaseSupplier databaseSupplier) {
		this.databaseSupplier = databaseSupplier;
		this.jsonb = JsonbBuilder.create();
	}
	
	/**
	 * Converts the provided QRP view to NoSQL document entities.
	 * 
	 * @param database the database containing the actual documents
	 * @param docs the QRP generated view
	 * @param classMapping the {@link ClassMapping} instance for the target entity; may be {@code null}
	 * @return a {@link Stream} of NoSQL {@link DocumentEntity} objects
	 * @throws NotesException if there is a problem reading the view or documents
	 */
	public Stream<DocumentEntity> convertQRPViewDocuments(Database database, View docs, ClassMapping classMapping) throws NotesException {
		ViewNavigator nav = docs.createViewNav();
		ViewNavigatorIterator iter = new ViewNavigatorIterator(nav, false, false);
		return iter.stream()
			.map(entry -> {
				try {
					Vector<?> columnValues = entry.getColumnValues();
					try {
						// The last column is the note ID in format "NT00000000"
						String noteId = (String)columnValues.get(columnValues.size()-1);
						lotus.domino.Document doc = database.getDocumentByID(noteId.substring(2));
						if(DominoNoSQLUtil.isValid(doc)) {
							List<Document> documents = convertDominoDocument(doc, classMapping);
							String name = doc.getItemValueString(DominoConstants.FIELD_NAME);
							return DocumentEntity.of(name, documents);
						} else {
							return null;
						}
					} finally {
						entry.recycle(columnValues);
					}
				} catch(NotesException e) {
					throw new RuntimeException(e);
				}
			})
			.filter(Objects::nonNull);
	}
	
	/**
	 * Converts the entries in the provided {@link ViewNavigator} into NoSQL document entities
	 * based on their column values.
	 * 
	 * @param entityName the name of the target entity
	 * @param nav the {@link ViewNavigator} to traverse
	 * @param didSkip whether previous code called {@code skip(...)} on {@code nav}
	 * @param limit the maximum number of entries to read, or {@code 0} to read all entries
	 * @param docsOnly whether to restrict processing to document entries only
	 * @param classMapping the {@link ClassMapping} instance for the target entity; may be {@code null}
	 * @return a {@link Stream} of NoSQL {@link DocumentEntity} objects
	 * @throws NotesException if there is a problem reading the view
	 */
	public Stream<DocumentEntity> convertViewEntries(String entityName, ViewNavigator nav, boolean didSkip, long limit, boolean docsOnly, ClassMapping classMapping) throws NotesException {
		nav.setEntryOptions(ViewNavigator.VN_ENTRYOPT_NOCOUNTDATA);
		
		// Read in the column names
		View view = nav.getParentView();
		@SuppressWarnings("unchecked")
		Vector<ViewColumn> columns = view.getColumns();
		List<String> columnNames = new ArrayList<>();
		List<String> columnFormulas = new ArrayList<>();
		for(ViewColumn col : columns) {
			if(col.getColumnValuesIndex() != ViewColumn.VC_NOT_PRESENT) {
				columnNames.add(col.getItemName());
				columnFormulas.add(col.getFormula());
			}
		}
		view.recycle(columns);
		
		Map<String, Class<?>> itemTypes = classMapping == null ? null : classMapping.getFields()
			.stream()
			.collect(Collectors.toMap(
				f -> f.getName(),
				f -> f.getNativeField().getType()
			));
		
		ViewNavigatorIterator iter = new ViewNavigatorIterator(nav, docsOnly, didSkip);
		Stream<DocumentEntity> result = iter.stream()
			.map(entry -> {
				try {
					return convertViewEntryInner(view.getParent(), entry, columnNames, columnFormulas, entityName, itemTypes);
				} catch(NotesException e) {
					throw new RuntimeException(e);
				}
			});
		if(limit > 0) {
			result = result.limit(limit);
		}
		return result;
	}
	
	/**
	 * Converts the document entries in the provided {@link ViewNavigator} to NoSQL documents
	 * based on their backing {@link Document} objects.
	 * 
	 * @param entityName the name of the target entity
	 * @param nav the {@link ViewNavigator} to traverse
	 * @param didSkip whether previous code called {@code skip(...)} on {@code nav}
	 * @param limit the maximum number of entries to read, or {@code 0} to read all entries
	 * @param classMapping the {@link ClassMapping} instance for the target entity; may be {@code null}
	 * @return a {@link Stream} of NoSQL {@link DocumentEntity} objects
	 * @throws NotesException if there is a problem reading the view or documents
	 */
	public Stream<DocumentEntity> convertViewDocuments(String entityName, ViewNavigator nav, boolean didSkip, long limit, ClassMapping classMapping) throws NotesException {
		nav.setEntryOptions(ViewNavigator.VN_ENTRYOPT_NOCOLUMNVALUES | ViewNavigator.VN_ENTRYOPT_NOCOUNTDATA);
		
		ViewNavigatorIterator iter = new ViewNavigatorIterator(nav, true, didSkip);
		Stream<DocumentEntity> result = iter.stream()
			.map(entry -> {
				try {
					lotus.domino.Document doc = entry.getDocument();
					List<Document> documents = convertDominoDocument(doc, classMapping);
					return DocumentEntity.of(entityName, documents);
				} catch (NotesException e) {
					throw new RuntimeException(e);
				}
			});
		if(limit > 0) {
			result = result.limit(limit);
		}
		return result;
	}

	/**
	 * Converts the documents in the provided {@link DocumentCollection} to NoSQL documents.
	 * 
	 * @param docs the {@link DocumentCollection} to process
	 * @param classMapping the {@link ClassMapping} instance for the target entity; may be {@code null}
	 * @return a {@link Stream} of NoSQL {@link DocumentEntity} objects
	 * @throws NotesException if there is a problem reading the documents
	 */
	public Stream<DocumentEntity> convertDocuments(DocumentCollection docs, ClassMapping classMapping) throws NotesException {
		DocumentCollectionIterator iter = new DocumentCollectionIterator(docs);
		return iter.stream()
			.filter(DominoNoSQLUtil::isValid)
			.map(doc -> {
				try {
					List<Document> documents = convertDominoDocument(doc, classMapping);
					String name = doc.getItemValueString(DominoConstants.FIELD_NAME);
					return DocumentEntity.of(name, documents);
				} catch(NotesException e) {
					throw new RuntimeException(e);
				}
			});
	}
	
	public DocumentEntity convertViewEntry(String entityName, ViewEntry viewEntry, ClassMapping classMapping) throws NotesException {
		Object parent = viewEntry.getParent();
		View view;
		if(parent instanceof View) {
			view = ((View)parent);
		} else if(parent instanceof ViewEntryCollection) {
			view = ((ViewEntryCollection)parent).getParent();
		} else if(parent instanceof ViewNavigator) {
			view = ((ViewNavigator)parent).getParentView();
		} else {
			throw new RuntimeException("Unable to locate parent view from " + viewEntry);
		}
		
		@SuppressWarnings("unchecked")
		Vector<ViewColumn> columns = view.getColumns();
		List<String> columnNames = new ArrayList<>();
		List<String> columnFormulas = new ArrayList<>();
		for(ViewColumn col : columns) {
			if(col.getColumnValuesIndex() != ViewColumn.VC_NOT_PRESENT) {
				columnNames.add(col.getItemName());
				columnFormulas.add(col.getFormula());
			}
		}
		view.recycle(columns);
		
		Map<String, Class<?>> itemTypes = classMapping == null ? null : classMapping.getFields()
			.stream()
			.collect(Collectors.toMap(
				f -> f.getName(),
				f -> f.getNativeField().getType()
			));
		
		return convertViewEntryInner(view.getParent(), viewEntry, columnNames, columnFormulas, entityName, itemTypes);
	}
	
	private DocumentEntity convertViewEntryInner(Database context, ViewEntry entry, List<String> columnNames, List<String> columnFormulas, String entityName, Map<String, Class<?>> itemTypes) throws NotesException {
		Vector<?> columnValues = entry.getColumnValues();
		try {
			List<Document> convertedEntry = new ArrayList<>(columnValues.size());

			String universalId = entry.getUniversalID();
			convertedEntry.add(Document.of(DominoConstants.FIELD_ID, universalId));
			convertedEntry.add(Document.of(DominoConstants.FIELD_POSITION, entry.getPosition('.')));
			convertedEntry.add(Document.of(DominoConstants.FIELD_READ, entry.getRead()));
			
			EntryType type;
			if(entry.isCategory()) {
				type = EntryType.CATEGORY;
			} else if(entry.isTotal()) {
				type = EntryType.TOTAL;
			} else {
				type = EntryType.DOCUMENT;
			}
			convertedEntry.add(Document.of(DominoConstants.FIELD_ENTRY_TYPE, type));
			
			for(int i = 0; i < columnValues.size(); i++) {
				String itemName = columnNames.get(i);
				Object value = columnValues.get(i);
				
				// Check to see if we have a matching time-based field and strip empty strings,
				//   since JNoSQL will otherwise try to parse them and will throw an exception
				if(itemTypes != null) {
					Class<?> itemType = itemTypes.get(itemName);
					if(itemType != null && TemporalAccessor.class.isAssignableFrom(itemType)) {
						if(value instanceof String && ((String)value).isEmpty()) {
							// Then skip the field
							continue;
						}
					}
				}
				
				// Check for known system formula equivalents
				switch(String.valueOf(columnFormulas.get(i))) {
				case "@DocLength": //$NON-NLS-1$
					itemName = DominoConstants.FIELD_SIZE;
					break;
				case "@Created": //$NON-NLS-1$
					itemName = DominoConstants.FIELD_CDATE;
					break;
				case "@Modified": //$NON-NLS-1$
					itemName = DominoConstants.FIELD_MDATE;
					break;
				case "@Accessed": //$NON-NLS-1$
					itemName = DominoConstants.FIELD_ADATE;
					break;
				case "@NoteID": //$NON-NLS-1$
					itemName = DominoConstants.FIELD_NOTEID;
					break;
				case "@AddedToThisFile": //$NON-NLS-1$
					itemName = DominoConstants.FIELD_ADDED;
					break;
				case "@ModifiedInThisFile": //$NON-NLS-1$
					itemName = DominoConstants.FIELD_MODIFIED_IN_THIS_FILE;
					break;
				case "@AttachmentNames": //$NON-NLS-1$
					// Very special handling for this
					itemName = DominoConstants.FIELD_ATTACHMENTS;
					if(value instanceof List) {
						@SuppressWarnings("unchecked")
						List<EntityAttachment> attachments = ((List<String>)value).stream()
							.map(attName -> new DominoDocumentAttachment(databaseSupplier, universalId, attName))
							.collect(Collectors.toList());
						convertedEntry.add(Document.of(itemName, attachments));
					} else if(value instanceof String && !((String)value).isEmpty()) {
						EntityAttachment attachment = new DominoDocumentAttachment(databaseSupplier, universalId, (String)value);
						convertedEntry.add(Document.of(itemName, Collections.singletonList(attachment)));
					} else {
						convertedEntry.add(Document.of(itemName, Collections.emptyList()));
					}
					continue; // Skip to the next column
				default:
					break;
				}
				
				convertedEntry.add(Document.of(itemName, DominoNoSQLUtil.toJavaFriendly(context, value)));
			}
			
			// If the entity requested an ETag and we happened to include the modified date, we can do that here
			Set<String> fieldNames = itemTypes.keySet();
			if(fieldNames.contains(DominoConstants.FIELD_ETAG)) {
				Optional<Temporal> modified = convertedEntry.stream()
					.filter(d -> DominoConstants.FIELD_MDATE.equals(d.getName()))
					.map(Document::get)
					.map(Temporal.class::cast)
					.findFirst();
				if(modified.isPresent()) {
					String etag = composeEtag(universalId, modified.get());
					convertedEntry.add(Document.of(DominoConstants.FIELD_ETAG, etag));
				}
			}
			
			if(fieldNames.contains(DominoConstants.FIELD_REPLICAID)) {
				convertedEntry.add(Document.of(DominoConstants.FIELD_REPLICAID, context.getReplicaID()));
			}
			if(fieldNames.contains(DominoConstants.FIELD_FILEPATH)) {
				convertedEntry.add(Document.of(DominoConstants.FIELD_FILEPATH, context.getFilePath()));
			}
			if(fieldNames.contains(DominoConstants.FIELD_SERVER)) {
				convertedEntry.add(Document.of(DominoConstants.FIELD_SERVER, context.getServer()));
			}
			
			return DocumentEntity.of(entityName, convertedEntry);
		} finally {
			entry.recycle(columnValues);
		}
	}

	@SuppressWarnings("unchecked")
	public List<Document> convertDominoDocument(lotus.domino.Document doc, ClassMapping classMapping) throws NotesException {
		Set<String> fieldNames = classMapping == null ? null : classMapping.getFieldsName()
			.stream()
			.filter(s -> !DominoConstants.FIELD_ID.equals(s))
			.collect(Collectors.toSet());

		Database database = doc.getParentDatabase();
		Session session = database.getParent();
		boolean convertMime = session.isConvertMime();
		try {
			session.setConvertMime(false);
			
			List<Document> result = new ArrayList<>();
			String unid = doc.getUniversalID();
			result.add(Document.of(DominoConstants.FIELD_ID, unid));
			
			// TODO when fieldNames is present, only loop over those names
			Map<String, Object> docMap = new LinkedHashMap<>();
			for(Item item : (List<Item>)doc.getItems()) {
				String itemName = item.getName();
				if(DominoConstants.SYSTEM_FIELDS.contains(itemName)) {
					continue;
				}
				
				// If we have field information, restrict to only those fields
				//   and match capitalization
				if(fieldNames != null) {
					String fItemName = itemName;
					itemName = fieldNames.stream()
						.filter(fieldName -> fieldName.equalsIgnoreCase(fItemName))
						.findFirst()
						.orElse(null);
					if(itemName == null) {
						continue;
					}
				}
				
				// Check if the item is expected to be stored specially, which may be handled down the line
				Optional<ItemStorage> optStorage = getFieldAnnotation(classMapping, itemName, ItemStorage.class);
				
				if(item instanceof RichTextItem) {
					// Special handling here for RT -> HTML
					String html = ((RichTextItem)item).convertToHTML(DominoConstants.HTML_CONVERSION_OPTIONS);
					docMap.put(itemName, html);
				} else if(item.getType() == Item.MIME_PART) {
					MIMEEntity entity = doc.getMIMEEntity(itemName);
					
					// See if this is expected to be MIMEBean
					if(optStorage.isPresent() && optStorage.get().type() == ItemStorage.Type.MIMEBean) {
						// If so, deserialize it

						byte[] serialized;
						lotus.domino.Stream outStream = session.createStream();
						try {
							entity.getContentAsBytes(outStream);
							try(ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
								outStream.getContents(baos);
								serialized = baos.toByteArray();
							} catch (IOException e) {
								throw new UncheckedIOException(e);
							}
						} finally {
							outStream.close();
							outStream.recycle();
						}
						
						String encoding = null;
						MIMEHeader encodingHeader = entity.getNthHeader("Content-Encoding"); //$NON-NLS-1$
						if(encodingHeader != null) {
							encoding = encodingHeader.getHeaderVal();
						}
						
						try(
							InputStream bais = new ByteArrayInputStream(serialized);
							InputStream is = DominoNoSQLUtil.wrapInputStream(bais, encoding);
							ObjectInputStream ois = new LoaderObjectInputStream(is)
						) {
							docMap.put(itemName, ois.readObject());
							continue;
						} catch (IOException e) {
							throw new UncheckedIOException(e);
						} catch (ClassNotFoundException e) {
							throw new RuntimeException(e);
						}
					}
					
					// TODO consider whether to pass this back as a Mail API MIME entity
					MIMEEntity html = findEntityForType(entity, "text", "html"); //$NON-NLS-1$ //$NON-NLS-2$
					if(html != null) {
						docMap.put(itemName, html.getContentAsText());
					} else {
						MIMEEntity text = findEntityForType(entity, "text", "plain"); //$NON-NLS-1$ //$NON-NLS-2$
						if(text != null) {
							docMap.put(itemName, text.getContentAsText());
						} else {
							docMap.put(itemName, entity.toString());
						}
					}
				} else {
					List<?> val = item.getValues();
					if(val == null || val.isEmpty()) {
						// Skip
					} else if(val.size() == 1) {
						// It may be stored as JSON
						if(val.get(0) != null) {
							if(optStorage.isPresent() && optStorage.get().type() == ItemStorage.Type.JSON) {
								Optional<Class<?>> targetType = getFieldType(classMapping, itemName);
								if(targetType.isPresent()) {
									if(String.class.equals(targetType.get())) {
										// Ignore when the target is a string
									} else {
										// Then try to deserialize it as the target type
										Object dest = AccessController.doPrivileged((PrivilegedAction<Object>)() -> {
											return jsonb.fromJson(val.get(0).toString(), targetType.get());
										});
										docMap.put(itemName, dest);
										continue;
									}
								}
							}
						}
						
						docMap.put(itemName, DominoNoSQLUtil.toJavaFriendly(database, val.get(0)));
					} else {
						docMap.put(itemName, DominoNoSQLUtil.toJavaFriendly(database, val));
					}
				}
			}
			
			docMap.forEach((key, value) -> result.add(Document.of(key, value)));
	
			if(fieldNames != null) {
				if(fieldNames.contains(DominoConstants.FIELD_CDATE)) {
					result.add(Document.of(DominoConstants.FIELD_CDATE, DominoNoSQLUtil.toTemporal(database, doc.getCreated())));
				}
				if(fieldNames.contains(DominoConstants.FIELD_MDATE)) {
					result.add(Document.of(DominoConstants.FIELD_MDATE, DominoNoSQLUtil.toTemporal(database, doc.getInitiallyModified())));
				}
				if(fieldNames.contains(DominoConstants.FIELD_READ)) {
					result.add(Document.of(DominoConstants.FIELD_READ, doc.getRead()));
				}
				if(fieldNames.contains(DominoConstants.FIELD_SIZE)) {
					result.add(Document.of(DominoConstants.FIELD_SIZE, doc.getSize()));
				}
				if(fieldNames.contains(DominoConstants.FIELD_ADATE)) {
					result.add(Document.of(DominoConstants.FIELD_ADATE, DominoNoSQLUtil.toTemporal(database, doc.getLastAccessed())));
				}
				if(fieldNames.contains(DominoConstants.FIELD_NOTEID)) {
					result.add(Document.of(DominoConstants.FIELD_NOTEID, doc.getNoteID()));
				}
				if(fieldNames.contains(DominoConstants.FIELD_ADDED)) {
					DateTime added = (DateTime)session.evaluate(" @AddedToThisFile ", doc).get(0); //$NON-NLS-1$
					result.add(Document.of(DominoConstants.FIELD_ADDED, DominoNoSQLUtil.toTemporal(database, added)));
				}
				if(fieldNames.contains(DominoConstants.FIELD_MODIFIED_IN_THIS_FILE)) {
					result.add(Document.of(DominoConstants.FIELD_MODIFIED_IN_THIS_FILE, DominoNoSQLUtil.toTemporal(database, doc.getLastModified())));
				}
				if(fieldNames.contains(DominoConstants.FIELD_ETAG)) {
					String etag = composeEtag(unid, DominoNoSQLUtil.toTemporal(database, doc.getInitiallyModified()));
					result.add(Document.of(DominoConstants.FIELD_ETAG, etag));
				}
				if(fieldNames.contains(DominoConstants.FIELD_REPLICAID)) {
					result.add(Document.of(DominoConstants.FIELD_REPLICAID, database.getReplicaID()));
				}
				if(fieldNames.contains(DominoConstants.FIELD_FILEPATH)) {
					result.add(Document.of(DominoConstants.FIELD_FILEPATH, database.getFilePath()));
				}
				if(fieldNames.contains(DominoConstants.FIELD_SERVER)) {
					result.add(Document.of(DominoConstants.FIELD_SERVER, database.getServer()));
				}
				
				if(fieldNames.contains(DominoConstants.FIELD_ATTACHMENTS)) {
					List<String> attachmentNames = session.evaluate(" @AttachmentNames ", doc); //$NON-NLS-1$
					List<EntityAttachment> attachments = attachmentNames.stream()
						.filter(StringUtil::isNotEmpty)
						.map(attachmentName -> new DominoDocumentAttachment(this.databaseSupplier, unid, attachmentName))
						.collect(Collectors.toList());
					result.add(Document.of(DominoConstants.FIELD_ATTACHMENTS, attachments));
				}
				
				if(fieldNames.contains(DominoConstants.FIELD_DXL)) {
					DxlExporter exporter = session.createDxlExporter();
					
					Optional<DXLExport> optSettings = getFieldAnnotation(classMapping, DominoConstants.FIELD_DXL, DXLExport.class);
					if(optSettings.isPresent()) {
						DXLExport settings = optSettings.get();
						
						if(StringUtil.isNotEmpty(settings.attachmentOmittedText())) {
							exporter.setAttachmentOmittedText(settings.attachmentOmittedText());
						}
						exporter.setConvertNotesBitmapsToGIF(settings.convertNotesBitmapsToGIF());
						if(StringUtil.isNotEmpty(settings.doctypeSYSTEM())) {
							exporter.setDoctypeSYSTEM(settings.doctypeSYSTEM());
						}
						exporter.setExitOnFirstFatalError(settings.exitOnFirstFatalError());
						exporter.setForceNoteFormat(settings.forceNoteFormat());
						if(settings.encapsulateMime()) {
							exporter.setMIMEOption(DxlExporter.DXLMIMEOPTION_DXL);
						}
						if(StringUtil.isNotEmpty(settings.oleObjectOmittedText())) {
							exporter.setOLEObjectOmittedText(settings.oleObjectOmittedText());
						}
						if(settings.omitItemNames() != null && settings.omitItemNames().length > 0) {
							exporter.setOmitItemNames(new Vector<>(Arrays.asList(settings.omitItemNames())));
						}
						exporter.setOmitMiscFileObjects(settings.omitMiscFileObjects());
						exporter.setOmitOLEObjects(settings.omitOleObjects());
						exporter.setOmitRichtextAttachments(settings.omitRichTextAttachments());
						exporter.setOmitRichtextPictures(settings.omitRichTextPictures());
						exporter.setOutputDOCTYPE(settings.outputDOCTYPE());
						if(StringUtil.isNotEmpty(settings.pictureOmittedText())) {
							exporter.setPictureOmittedText(settings.pictureOmittedText());
						}
						if(settings.restrictToItemNames() != null && settings.restrictToItemNames().length > 0) {
							exporter.setRestrictToItemNames(new Vector<>(Arrays.asList(settings.restrictToItemNames())));
						}
						if(!settings.encapsulateRichText()) {
							exporter.setRichTextOption(DxlExporter.DXLRICHTEXTOPTION_RAW);
						}
					}
					
					String dxl = exporter.exportDxl(doc);
					result.add(Document.of(DominoConstants.FIELD_DXL, dxl));
				}
			}
			
			return result;
		} finally {
			session.setConvertMime(convertMime);
		}
	}

	/**
	 * Converts the provided {@link DocumentEntity} instance into a Domino
	 * JSON object.
	 * 
	 * @param entity the entity instance to convert
	 * @param retainId whether or not to remove the {@link #FIELD_ID} field during conversion
	 * @param target the target Domino Document to store in
	 */
	public void convertNoSQLEntity(DocumentEntity entity, boolean retainId, lotus.domino.Document target, ClassMapping classMapping) throws NotesException {
		requireNonNull(entity, "entity is required"); //$NON-NLS-1$
		try {
			@SuppressWarnings("unchecked")
			List<ValueWriter<Object, Object>> writers = ServiceLoaderProvider.getSupplierStream(ValueWriter.class)
				.map(w -> (ValueWriter<Object, Object>)w)
				.collect(Collectors.toList());
	
			for(Document doc : entity.getDocuments()) {
				if(DominoConstants.FIELD_ATTACHMENTS.equals(doc.getName())) {
					@SuppressWarnings("unchecked")
					List<EntityAttachment> incoming = (List<EntityAttachment>)doc.get();
					Set<String> retain = incoming.stream()
						.filter(DominoDocumentAttachment.class::isInstance)
						.map(EntityAttachment::getName)
						.collect(Collectors.toSet());
					
					// TODO change to account for specific rich text fields
					// Remove any attachments no longer in the entity
					@SuppressWarnings("unchecked")
					List<String> existing = target.getParentDatabase().getParent()
						.evaluate(" @AttachmentNames ", target); //$NON-NLS-1$;
					for(String attName : existing) {
						if(StringUtil.isNotEmpty(attName) && !retain.contains(attName)) {
							EmbeddedObject eo = target.getAttachment(attName);
							if(eo != null) {
								eo.remove();
								eo.recycle();
							}
						}
					}
					
					// Now attach any incoming that aren't currently in the doc
					List<EntityAttachment> newAttachments = incoming.stream()
						.filter(att -> !(att instanceof DominoDocumentAttachment))
						.collect(Collectors.toList());
					if(!newAttachments.isEmpty()) {
						RichTextItem body = (RichTextItem)target.getFirstItem(DominoConstants.FIELD_ATTACHMENTS);
						if(body == null) {
							body = target.createRichTextItem(DominoConstants.FIELD_ATTACHMENTS);
						}
						for(EntityAttachment att : newAttachments) {
							// TODO check for if this field already exists
							try {
								Path tempDir = Files.createTempDirectory(DominoNoSQLUtil.getTempDirectory(), getClass().getSimpleName());
								try {
									// TODO consider options for when the name can't be stored on the filesystem
									Path tempFile = tempDir.resolve(att.getName());
									try(InputStream is = att.getData()) {
										Files.copy(is, tempFile, StandardCopyOption.REPLACE_EXISTING);
									}
									body.embedObject(EmbeddedObject.EMBED_ATTACHMENT, "", tempFile.toString(), null); //$NON-NLS-1$
								} finally {
									Files.list(tempDir).forEach(t -> {
										try {
											Files.deleteIfExists(t);
										} catch (IOException e) {
											throw new UncheckedIOException(e);
										}
									});
									Files.deleteIfExists(tempDir);
								}
							} catch (IOException e) {
								throw new UncheckedIOException(e);
							}
						}
					}
				} else if(!DominoConstants.SKIP_WRITING_FIELDS.contains(doc.getName())) {
					Optional<ItemStorage> optStorage = getFieldAnnotation(classMapping, doc.getName(), ItemStorage.class);
					// Check if we should skip processing
					if(optStorage.isPresent()) {
						ItemStorage storage = optStorage.get();
						if(!storage.insertable() && target.isNewNote()) {
							continue;
						} else if(!storage.updatable() && !target.isNewNote()) {
							continue;
						}
					}
					
					Object value = doc.get();
					if(value == null) {
						target.removeItem(doc.getName());
					} else {
						Object val = value;
						for(ValueWriter<Object, Object> w : writers) {
							if(w.test(value.getClass())) {
								val = w.write(value);
								break;
							}
						}
						
						Item item;
						
						// Check if the item is expected to be stored specially, which may be handled down the line
						if(optStorage.isPresent() && optStorage.get().type() != ItemStorage.Type.Default) {
							ItemStorage storage = optStorage.get();
							switch(storage.type()) {
							case JSON:
								Object fVal = val;
								String json = AccessController.doPrivileged((PrivilegedAction<String>)() -> jsonb.toJson(fVal));
								item = target.replaceItemValue(doc.getName(), json);
								item.setSummary(false);
								break;
							case MIME: {
								target.removeItem(doc.getName());
								MIMEEntity mimeEntity = target.createMIMEEntity(doc.getName());
								lotus.domino.Stream mimeStream = target.getParentDatabase().getParent().createStream();
								try {
									mimeStream.writeText(val.toString());
									mimeEntity.setContentFromText(mimeStream, storage.mimeType(), MIMEEntity.ENC_NONE);
								} finally {
									mimeStream.close();
									mimeStream.recycle();
								}
								continue;
							}
							case MIMEBean:
								byte[] serialized;
								try(
									ByteArrayOutputStream baos = new ByteArrayOutputStream();
									ObjectOutputStream oos = new ObjectOutputStream(baos);
								) {
									oos.writeObject(val);
									oos.flush();
									serialized = baos.toByteArray();
								} catch(IOException e) {
									throw new UncheckedIOException(e);
								}
								target.removeItem(doc.getName());
								MIMEEntity mimeEntity = target.createMIMEEntity(doc.getName());
								mimeEntity.createHeader(DominoConstants.HEADER_JAVA_CLASS).setHeaderVal(val.getClass().getName());
								lotus.domino.Stream mimeStream = target.getParentDatabase().getParent().createStream();
								try {
									mimeStream.write(serialized);
									mimeStream.setPosition(0);
									mimeEntity.setContentFromBytes(mimeStream, DominoConstants.MIME_TYPE_SERIALIZED_OBJECT, MIMEEntity.ENC_NONE);
								} finally {
									mimeStream.close();
									mimeStream.recycle();
								}
								
								continue;
							case Default:
							default:
								// Shouldn't get here
								throw new UnsupportedOperationException(MessageFormat.format("Unable to handle storage type {0}", storage.type()));
							}
						} else {
							Object dominoVal = DominoNoSQLUtil.toDominoFriendly(target.getParentDatabase().getParent(), val);
							
							// Set number precision if applicable
							if(optStorage.isPresent()) {
								int precision = optStorage.get().precision();
								if(precision > 0) {
									dominoVal = DominoNoSQLUtil.applyPrecision(dominoVal, precision);
								}
							}
							
							item = target.replaceItemValue(doc.getName(), dominoVal);
						}
						
						// Check for a @ItemFlags annotation
						Optional<ItemFlags> itemFlagsOpt = getFieldAnnotation(classMapping, doc.getName(), ItemFlags.class);
						if(itemFlagsOpt.isPresent()) {
							ItemFlags itemFlags = itemFlagsOpt.get();
							item.setAuthors(itemFlags.authors());
							item.setReaders(itemFlags.readers());
							if(itemFlags.authors() || itemFlags.readers() || itemFlags.names()) {
								item.setNames(true);
							} else {
								item.setNames(false);
							}
							item.setEncrypted(itemFlags.encrypted());
							item.setProtected(itemFlags.protectedItem());
							item.setSigned(itemFlags.signed());
							if(!(item instanceof RichTextItem) && item.getType() != Item.MIME_PART) {
								item.setSummary(itemFlags.summary());
							}
							item.setSaveToDisk(itemFlags.saveToDisk());
						}
						
						item.recycle();
					}
				}
			}
			
			target.replaceItemValue(DominoConstants.FIELD_NAME, entity.getName());
			
			target.closeMIMEEntities(true);
		} catch(Exception e) {
			throw e;
		}
	}
	
	// *******************************************************************************
	// * Utility methods
	// *******************************************************************************
	
	private static MIMEEntity findEntityForType(MIMEEntity entity, String targetType, String targetSubtype) throws NotesException {
		String type = entity.getContentType();
		String subtype = entity.getContentSubType();
		if(targetType.equals(type) && targetSubtype.equals(subtype)) {
			return entity;
		} else if("multipart".equals(type)) { //$NON-NLS-1$
			MIMEEntity child = entity.getFirstChildEntity();
			while(child != null) {
				MIMEEntity result = findEntityForType(child, targetType, targetSubtype);
				if(result != null) {
					return result;
				}
				
				child = child.getNextSibling();
			}
			return null;
		} else {
			return null;
		}
	}
}
