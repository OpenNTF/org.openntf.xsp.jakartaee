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
import java.util.Vector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import jakarta.nosql.document.Document;
import jakarta.nosql.document.DocumentEntity;
import lotus.domino.Database;
import lotus.domino.DateRange;
import lotus.domino.DateTime;
import lotus.domino.DocumentCollection;
import lotus.domino.Item;
import lotus.domino.NotesException;
import lotus.domino.View;
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
	
	static Stream<DocumentEntity> convert(Database database, View docs) throws NotesException {
		// TODO stream this better
		// TODO create a lazy-loading list?
		List<DocumentEntity> result = new ArrayList<>();
		ViewNavigator nav = docs.createViewNav();
		try {
			ViewEntry entry = nav.getFirst();
			while(entry != null) {
				List<?> columnValues = entry.getColumnValues();
				// The last column is the note ID in format "NT00000000"
				String noteId = (String)columnValues.get(columnValues.size()-1);
				lotus.domino.Document doc = database.getDocumentByID(noteId.substring(2));
				if(isValid(doc)) {
					List<Document> documents = toDocuments(doc);
					String name = doc.getItemValueString(NAME_FIELD);
					result.add(DocumentEntity.of(name, documents));
				}
				
				ViewEntry tempEntry = entry;
				entry = nav.getNext(entry);
				tempEntry.recycle();
			}
		} finally {
			nav.recycle();
		}
		return result.stream();
	}

	static Stream<DocumentEntity> convert(DocumentCollection docs) throws NotesException {
		// TODO stream this better
		// TODO create a lazy-loading list?
		List<DocumentEntity> result = new ArrayList<>();
		lotus.domino.Document doc = docs.getFirstDocument();
		while(doc != null) {
			if(isValid(doc)) {
				List<Document> documents = toDocuments(doc);
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
	public static List<Document> toDocuments(lotus.domino.Document doc) throws NotesException {
		List<Document> result = new ArrayList<>();
		result.add(Document.of(ID_FIELD, doc.getUniversalID()));
		Map<String, Object> docMap = new LinkedHashMap<>();
		for(Item item : (List<Item>)doc.getItems()) {
			String itemName = item.getName();
			if(NAME_FIELD.equalsIgnoreCase(itemName)) {
				continue;
			}
			List<?> val = item.getValues();
			if(val == null || val.isEmpty()) {
				// Skip
			} else if(val.size() == 1) {
				docMap.put(item.getName(), toJavaFriendly(doc, val.get(0)));
			} else {
				docMap.put(item.getName(), toJavaFriendly(doc, val));
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

		for(Document doc : entity.getDocuments()) {
			if(!"$FILE".equalsIgnoreCase(doc.getName()) && !ID_FIELD.equalsIgnoreCase(doc.getName())) { //$NON-NLS-1$
				Object value = doc.get();
				if(value == null) {
					target.removeItem(doc.getName());
				} else {
					target.replaceItemValue(doc.getName(), toDominoFriendly(target, value)).recycle();
				}
			}
		}
		
		target.replaceItemValue(NAME_FIELD, entity.getName());
	}
	
	private static Object toDominoFriendly(lotus.domino.Document context, Object value) throws NotesException {
		if(value instanceof Iterable) {
			Vector<Object> result = new Vector<Object>();
			for(Object val : (Iterable<?>)value) {
				result.add(toDominoFriendly(context, val));
			}
			return result;
		} else if(value instanceof Date) {
			return context.getParentDatabase().getParent().createDateTime((Date)value);
		} else if(value instanceof Calendar) {
			return context.getParentDatabase().getParent().createDateTime((Calendar)value);
		} else if(value instanceof Number) {
			return ((Number)value).doubleValue();
		} else if(value instanceof Boolean) {
			// TODO figure out if this can be customized, perhaps from the Settings element
			return (Boolean)value ? "Y": "N"; //$NON-NLS-1$ //$NON-NLS-2$
		} else if(value instanceof LocalDate) {
			// TODO fix these Temporals when the API improves
			Instant inst = ZonedDateTime.of((LocalDate)value, LocalTime.of(12, 0), ZoneId.systemDefault()).toInstant();
			DateTime dt = context.getParentDatabase().getParent().createDateTime(Date.from(inst));
			dt.setAnyTime();
			return dt;
		} else if(value instanceof LocalTime) {
			Instant inst = ZonedDateTime.of(LocalDate.now(), (LocalTime)value, ZoneId.systemDefault()).toInstant();
			DateTime dt = context.getParentDatabase().getParent().createDateTime(Date.from(inst));
			dt.setAnyDate();
			return dt;
		} else if(value instanceof TemporalAccessor) {
			Instant inst = Instant.from((TemporalAccessor)value);
			DateTime dt = context.getParentDatabase().getParent().createDateTime(Date.from(inst));
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
	private static Object toJavaFriendly(lotus.domino.Document context, Object value) {
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
					context.replaceItemValue(ITEM_TEMPTIME, dt);
					String iso = (String)dt.getParent().evaluate(FORMULA_TOISOTIME, context).get(0);
					Instant inst = dt.toJavaDate().toInstant();
					int nano = inst.getNano();
					iso += "." + nano; //$NON-NLS-1$
					return LocalTime.from(DateTimeFormatter.ISO_LOCAL_TIME.parse(iso));
				} else if(timePart == null || timePart.isEmpty()) {
					context.replaceItemValue(ITEM_TEMPTIME, dt);
					String iso = (String)dt.getParent().evaluate(FORMULA_TOISODATE, context).get(0);
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
				Temporal start = (Temporal)toDominoFriendly(context, dr.getStartDateTime());
				Temporal end = (Temporal)toDominoFriendly(context, dr.getEndDateTime());
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
