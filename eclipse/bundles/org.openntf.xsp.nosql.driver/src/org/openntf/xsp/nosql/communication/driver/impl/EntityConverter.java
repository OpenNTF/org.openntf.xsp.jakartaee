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
package org.openntf.xsp.nosql.communication.driver.impl;


import static java.util.Objects.requireNonNull;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.eclipse.jnosql.mapping.reflection.ClassMapping;

import jakarta.nosql.ServiceLoaderProvider;
import jakarta.nosql.ValueWriter;
import jakarta.nosql.document.Document;
import jakarta.nosql.document.DocumentEntity;
import lotus.domino.Database;
import lotus.domino.DateRange;
import lotus.domino.DateTime;
import lotus.domino.DocumentCollection;
import lotus.domino.Item;
import lotus.domino.MIMEEntity;
import lotus.domino.NotesException;
import lotus.domino.RichTextItem;
import lotus.domino.Session;
import lotus.domino.View;
import lotus.domino.ViewColumn;
import lotus.domino.ViewEntry;
import lotus.domino.ViewNavigator;

/**
 * Utility methods for converting between Domino and NoSQL entities.
 * 
 * @author Jesse Gallagher
 * @since 2.3.0
 */
public enum EntityConverter {
	;
	/**
	 * The field used to store the UNID of the document during JSON
	 * serialization, currently {@value #ID_FIELD}
	 */
	public static final String ID_FIELD = "_id"; //$NON-NLS-1$
	/**
	 * The expected field containing the collection name of the document in
	 * Domino, currently {@value #NAME_FIELD}
	 */
	public static final String NAME_FIELD = "Form"; //$NON-NLS-1$
	
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
	
	static Stream<DocumentEntity> convert(Database database, View docs, ClassMapping classMapping) throws NotesException {
		// TODO stream this better
		// TODO create a lazy-loading list?
		List<DocumentEntity> result = new ArrayList<>();
		ViewNavigator nav = docs.createViewNav();
		try {
			ViewEntry entry = nav.getFirst();
			while(entry != null) {
				Vector<?> columnValues = entry.getColumnValues();
				// The last column is the note ID in format "NT00000000"
				String noteId = (String)columnValues.get(columnValues.size()-1);
				lotus.domino.Document doc = database.getDocumentByID(noteId.substring(2));
				if(isValid(doc)) {
					List<Document> documents = toDocuments(doc, classMapping);
					String name = doc.getItemValueString(NAME_FIELD);
					result.add(DocumentEntity.of(name, documents));
				}
				
				entry.recycle(columnValues);
				ViewEntry tempEntry = entry;
				entry = nav.getNext(entry);
				tempEntry.recycle();
			}
		} finally {
			nav.recycle();
		}
		return result.stream();
	}
	
	static Stream<DocumentEntity> convertViewEntries(String entityName, ViewNavigator nav, long limit, ClassMapping classMapping) throws NotesException {
		// Read in the column names
		View view = nav.getParentView();
		@SuppressWarnings("unchecked")
		Vector<ViewColumn> columns = view.getColumns();
		List<String> columnNames = new ArrayList<>();
		for(ViewColumn col : columns) {
			if(col.getColumnValuesIndex() != ViewColumn.VC_NOT_PRESENT) {
				columnNames.add(col.getItemName());
			}
		}
		view.recycle(columns);
		
		List<DocumentEntity> result = new ArrayList<>();
		ViewEntry entry = nav.getFirst();
		while(entry != null) {
			Vector<?> columnValues = entry.getColumnValues();
			
			List<Document> convertedEntry = new ArrayList<>(columnValues.size());

			convertedEntry.add(Document.of(ID_FIELD, entry.getUniversalID()));
			
			for(int i = 0; i < columnValues.size(); i++) {
				String itemName = columnNames.get(i);
				Object value = columnValues.get(i);
				convertedEntry.add(Document.of(itemName, toJavaFriendly(view.getParent(), value)));
			}
			result.add(DocumentEntity.of(entityName, convertedEntry));
			
			if(limit > 0 && result.size() >= limit) {
				break;
			}

			entry.recycle(columnValues);
			ViewEntry tempEntry = entry;
			entry = nav.getNext(entry);
			tempEntry.recycle();
		}
		
		return result.stream();
	}
	
	static Stream<DocumentEntity> convertViewDocuments(String entityName, ViewNavigator nav, long limit, ClassMapping classMapping) throws NotesException {
		// Read in the column names
		View view = nav.getParentView();
		@SuppressWarnings("unchecked")
		Vector<ViewColumn> columns = view.getColumns();
		List<String> columnNames = new ArrayList<>();
		for(ViewColumn col : columns) {
			if(col.getColumnValuesIndex() != ViewColumn.VC_NOT_PRESENT) {
				columnNames.add(col.getItemName());
			}
		}
		view.recycle(columns);
		
		List<DocumentEntity> result = new ArrayList<>();
		ViewEntry entry = nav.getFirst();
		while(entry != null) {
			if(entry.isDocument()) {
				lotus.domino.Document doc = entry.getDocument();
				List<Document> documents = toDocuments(doc, classMapping);
				String name = doc.getItemValueString(NAME_FIELD);
				result.add(DocumentEntity.of(name, documents));
				
				if(limit > 0 && result.size() >= limit) {
					break;
				}
			}
			
			ViewEntry tempEntry = entry;
			entry = nav.getNext(entry);
			tempEntry.recycle();
		}
		
		return result.stream();
	}

	static Stream<DocumentEntity> convert(DocumentCollection docs, ClassMapping classMapping) throws NotesException {
		// TODO stream this better
		// TODO create a lazy-loading list?
		List<DocumentEntity> result = new ArrayList<>();
		lotus.domino.Document doc = docs.getFirstDocument();
		while(doc != null) {
			if(isValid(doc)) {
				List<Document> documents = toDocuments(doc, classMapping);
				String name = doc.getItemValueString(NAME_FIELD);
				result.add(DocumentEntity.of(name, documents));
			}
			
			lotus.domino.Document tempDoc = doc;
			doc = docs.getNextDocument();
			tempDoc.recycle();
		}
		return result.stream();
	}

	@SuppressWarnings("unchecked")
	public static List<Document> toDocuments(lotus.domino.Document doc, ClassMapping classMapping) throws NotesException {
		Set<String> fieldNames = classMapping == null ? null : classMapping.getFieldsName()
			.stream()
			.filter(s -> !ID_FIELD.equals(s))
			.collect(Collectors.toSet());
		
		Session session = doc.getParentDatabase().getParent();
		boolean convertMime = session.isConvertMime();
		try {
			session.setConvertMime(false);
			
			List<Document> result = new ArrayList<>();
			result.add(Document.of(ID_FIELD, doc.getUniversalID()));
			
			Map<String, Object> docMap = new LinkedHashMap<>();
			for(Item item : (List<Item>)doc.getItems()) {
				String itemName = item.getName();
				if(NAME_FIELD.equalsIgnoreCase(itemName)) {
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
				
				if(item instanceof RichTextItem) {
					// Special handling here for RT -> HTML
					String html = ((RichTextItem)item).convertToHTML(HTML_CONVERSION_OPTIONS);
					docMap.put(itemName, html);
				} else if(item.getType() == Item.MIME_PART) {
					// TODO consider whether to pass this back as a Mail API MIME entity
					MIMEEntity entity = doc.getMIMEEntity(itemName);
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
						docMap.put(itemName, toJavaFriendly(doc.getParentDatabase(), val.get(0)));
					} else {
						docMap.put(itemName, toJavaFriendly(doc.getParentDatabase(), val));
					}
				}
			}
			
			docMap.forEach((key, value) -> result.add(Document.of(key, value)));
	
			result.add(Document.of("_cdate", doc.getCreated().toJavaDate().toInstant())); //$NON-NLS-1$
			result.add(Document.of("_mdate", doc.getCreated().toJavaDate().toInstant())); //$NON-NLS-1$
			
			// TODO attachments support
	//		result.add(Document.of(ATTACHMENT_FIELD,
	//			Stream.of(doc.getAttachments())
	//				.map(t -> {
	//					try {
	//						return new DominoDocumentAttachment(t);
	//					} catch (NotesException e) {
	//						throw new RuntimeException(e);
	//					}
	//				})
	//				.collect(Collectors.toList())
	//		));
			
			return result;
		} finally {
			session.setConvertMime(convertMime);
		}
	}
	
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

	/**
	 * Converts the provided {@link DocumentEntity} instance into a Domino
	 * JSON object.
	 * 
	 * <p>This is equivalent to calling {@link #convert(DocumentEntity, boolean)} with
	 * <code>false</code> as the second parameter.</p>
	 * 
	 * @param entity the entity instance to convert
	 * @param target the target Domino Document to store in
	 */
	public static void convert(DocumentEntity entity, lotus.domino.Document target) throws NotesException {
		convert(entity, false, target);
	}
	
	/**
	 * Converts the provided {@link DocumentEntity} instance into a Domino
	 * JSON object.
	 * 
	 * @param entity the entity instance to convert
	 * @param retainId whether or not to remove the {@link #ID_FIELD} field during conversion
	 * @param target the target Domino Document to store in
	 */
	public static void convert(DocumentEntity entity, boolean retainId, lotus.domino.Document target) throws NotesException {
		requireNonNull(entity, "entity is required"); //$NON-NLS-1$

		// NB: JNoSQL doesn't currently use ValueWriters, so gather them here
		@SuppressWarnings("unchecked")
		List<ValueWriter<Object, Object>> writers = ServiceLoaderProvider.getSupplierStream(ValueWriter.class)
			.map(w -> (ValueWriter<Object, Object>)w)
			.collect(Collectors.toList());

		for(Document doc : entity.getDocuments()) {
			if(!"$FILE".equalsIgnoreCase(doc.getName()) && !ID_FIELD.equalsIgnoreCase(doc.getName())) { //$NON-NLS-1$
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
					
					target.replaceItemValue(doc.getName(), toDominoFriendly(target.getParentDatabase().getParent(), val)).recycle();
				}
			}
		}
		
		target.replaceItemValue(NAME_FIELD, entity.getName());
	}
	
	private static Object toDominoFriendly(Session session, Object value) throws NotesException {
		if(value instanceof Iterable) {
			Vector<Object> result = new Vector<Object>();
			for(Object val : (Iterable<?>)value) {
				result.add(toDominoFriendly(session, val));
			}
			return result;
		} else if(value instanceof Date) {
			return session.createDateTime((Date)value);
		} else if(value instanceof Calendar) {
			return session.createDateTime((Calendar)value);
		} else if(value instanceof Number) {
			return ((Number)value).doubleValue();
		} else if(value instanceof Boolean) {
			// TODO figure out if this can be customized, perhaps from the Settings element
			return (Boolean)value ? "Y": "N"; //$NON-NLS-1$ //$NON-NLS-2$
		} else if(value instanceof LocalDate) {
			// TODO fix these Temporals when the API improves
			Instant inst = ZonedDateTime.of((LocalDate)value, LocalTime.of(12, 0), ZoneId.systemDefault()).toInstant();
			DateTime dt = session.createDateTime(Date.from(inst));
			dt.setAnyTime();
			return dt;
		} else if(value instanceof LocalTime) {
			Instant inst = ZonedDateTime.of(LocalDate.now(), (LocalTime)value, ZoneId.systemDefault()).toInstant();
			DateTime dt = session.createDateTime(Date.from(inst));
			dt.setAnyDate();
			return dt;
		} else if(value instanceof TemporalAccessor) {
			Instant inst = Instant.from((TemporalAccessor)value);
			DateTime dt = session.createDateTime(Date.from(inst));
			return dt;
		} else {
			// TODO support other types above
			return value.toString();
		}
	}
	
	private static boolean isValid(lotus.domino.Document doc) {
		try {
			return doc != null && doc.isValid() && !doc.isDeleted() && doc.getCreated() != null;
		} catch (NotesException e) {
			throw new RuntimeException(e);
		}
	}
	
	private static final String ITEM_TEMPTIME = "$$TempTime"; //$NON-NLS-1$
	@SuppressWarnings("nls")
	private static final String FORMULA_TOISODATE = "m := @Month($$TempTime);\n"
		+ "d := @Day($$TempTime);\n"
		+ "@Text(@Year($$TempTime)) + \"-\" + @If(m < 10; \"0\"; \"\") + @Text(m) + \"-\" + @If(d < 10; \"0\"; \"\") + @Text(d)";
	@SuppressWarnings("nls")
	private static final String FORMULA_TOISOTIME = "h := @Hour($$TempTime);\n"
		+ "m := @Minute($$TempTime);\n"
		+ "s := @Second($$TempTime);\n"
		+ "@If(h < 10; \"0\"; \"\") + @Text(h) + \":\" + @If(m < 10; \"0\"; \"\") + @Text(m) + \":\" + @If(s < 10; \"0\"; \"\") + @Text(s)";
	
	/**
	 * Converts the provided value read from Domino to a stock JDK type, if necessary.
	 * 
	 * @param value the value to convert
	 * @return a stock-JDK object representing the value
	 */
	private static Object toJavaFriendly(lotus.domino.Database context, Object value) {
		if(value instanceof Iterable) {
			return StreamSupport.stream(((Iterable<?>)value).spliterator(), false)
				.map(val -> toJavaFriendly(context, val))
				.collect(Collectors.toList());
		} else if(value instanceof DateTime) {
			// TODO improve with a better API
			try {
				DateTime dt = (DateTime)value;
				String datePart = dt.getDateOnly();
				String timePart = dt.getTimeOnly();
				if(datePart == null || datePart.isEmpty()) {
					lotus.domino.Document tempDoc = context.createDocument();
					tempDoc.replaceItemValue(ITEM_TEMPTIME, dt);
					String iso = (String)dt.getParent().evaluate(FORMULA_TOISOTIME, tempDoc).get(0);
					Instant inst = dt.toJavaDate().toInstant();
					int nano = inst.getNano();
					iso += "." + nano; //$NON-NLS-1$
					return LocalTime.from(DateTimeFormatter.ISO_LOCAL_TIME.parse(iso));
				} else if(timePart == null || timePart.isEmpty()) {
					lotus.domino.Document tempDoc = context.createDocument();
					tempDoc.replaceItemValue(ITEM_TEMPTIME, dt);
					String iso = (String)dt.getParent().evaluate(FORMULA_TOISODATE, tempDoc).get(0);
					return LocalDate.from(DateTimeFormatter.ISO_LOCAL_DATE.parse(iso));
				} else {
					return dt.toJavaDate().toInstant();
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		} else if(value instanceof DateRange) {
			try {
				DateRange dr = (DateRange)value;
				Temporal start = (Temporal)toDominoFriendly(context.getParent(), dr.getStartDateTime());
				Temporal end = (Temporal)toDominoFriendly(context.getParent(), dr.getEndDateTime());
				return Arrays.asList(start, end);
			} catch (NotesException e) {
				throw new RuntimeException(e);
			}
		} else {
			// String, Double
			return value;
		}
	}
	
	
}
