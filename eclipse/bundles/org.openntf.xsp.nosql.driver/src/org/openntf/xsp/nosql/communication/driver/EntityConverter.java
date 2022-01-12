package org.openntf.xsp.nosql.communication.driver;


import static java.util.Objects.requireNonNull;
import static java.util.stream.StreamSupport.stream;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObjectBuilder;
import jakarta.nosql.document.Document;
import jakarta.nosql.document.DocumentEntity;
import lotus.domino.Database;
import lotus.domino.NotesException;

public class EntityConverter {
	/**
	 * The field used to store the UNID of the document during JSON
	 * serialization, currently "_id"
	 */
	public static final String ID_FIELD = "_id"; //$NON-NLS-1$
	/**
	 * The expected field containing the collection name of the document in
	 * Domino, currently "form"
	 */
	// TODO consider making this the store ID
	public static final String NAME_FIELD = "form"; //$NON-NLS-1$

	private EntityConverter() {
	}

	static Stream<DocumentEntity> convert(Collection<String> keys, Database database) {
		// TODO create a lazy-loading list
		return keys.stream().map(t -> {
			try {
				return database.getDocumentByUNID(t);
			} catch (NotesException e) {
				throw new RuntimeException(e);
			}
		}).filter(Objects::nonNull).map(doc -> {
			try {
				List<Document> documents = toDocuments(doc);
				String name = doc.getItemValueString(NAME_FIELD);
				return DocumentEntity.of(name, documents);
			} catch (NotesException e) {
				throw new RuntimeException(e);
			}
		});
	}

	public static List<Document> toDocuments(lotus.domino.Document doc) throws NotesException {
		List<Document> result = new ArrayList<>();
		result.add(Document.of(ID_FIELD, doc.getUniversalID()));
//		Object json = doc.getJson();
//		fac.removeProperty(json, NAME_FIELD);
//		result.addAll(toDocuments(JsonUtil.toJsonObject(json, fac)));
		// TODO iterate over items

		result.add(Document.of("_cdate", doc.getCreated().toJavaDate().getTime())); //$NON-NLS-1$
		result.add(Document.of("_mdate", doc.getCreated().toJavaDate().getTime())); //$NON-NLS-1$
		
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

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static List<Document> toDocuments(Map<String, Object> map) {
		List<Document> documents = new ArrayList<>();
		for (String key : map.keySet()) {
			if(key == null || key.isEmpty()) {
				continue;
			}
			
			Object value = map.get(key);
			if (value instanceof Map) {
				documents.add(Document.of(key, toDocuments((Map) value)));
			} else if (isADocumentIterable(value)) {
				List<List<Document>> subDocuments = new ArrayList<>();
				stream(((Iterable) value).spliterator(), false)
					.map(m -> toDocuments((Map) m))
					.forEach(e -> subDocuments.add((List<Document>)e));
				documents.add(Document.of(key, subDocuments));
			} else if(value != null) {
				documents.add(Document.of(key, value));
			}
		}
		return documents;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static boolean isADocumentIterable(Object value) {
		return value instanceof Iterable && stream(((Iterable) value).spliterator(), false).allMatch(Map.class::isInstance);
	}

	/**
	 * Converts the provided {@link DocumentEntity} instance into a Domino
	 * JSON object.
	 * 
	 * <p>This is equivalent to calling {@link #convert(DocumentEntity, boolean)} with
	 * <code>false</code> as the second parameter.</p>
	 * 
	 * @param entity the entity instance to convert
	 * @return the converted JSON object
	 */
	public static Object convert(DocumentEntity entity) throws NotesException {
		return convert(entity, false);
	}
	
	/**
	 * Converts the provided {@link DocumentEntity} instance into a Domino
	 * JSON object.
	 * 
	 * @param entity the entity instance to convert
	 * @param retainId whether or not to remove the {@link #ID_FIELD} field during conversion
	 * @return the converted JSON object
	 */
	public static Object convert(DocumentEntity entity, boolean retainId) throws NotesException {
		requireNonNull(entity, "entity is required"); //$NON-NLS-1$

		JsonObjectBuilder jsonObject = Json.createObjectBuilder();
		for(Document doc : entity.getDocuments()) {
			if(!"$FILE".equalsIgnoreCase(doc.getName())) { //$NON-NLS-1$
				jsonObject = toJsonObject(doc, jsonObject);
			}
		}
		jsonObject = jsonObject.add(NAME_FIELD, entity.getName());
		if(!retainId) {
			jsonObject = jsonObject.remove(ID_FIELD);
		}
		return jsonObject;
	}

	private static JsonObjectBuilder toJsonObject(Document d, JsonObjectBuilder json) {
		// Swap out sensitive names
		Object value = ValueUtil.convert(d.getValue());
		JsonObjectBuilder jsonObject = json;

		if (value instanceof Document) {
			jsonObject = convertDocument(jsonObject, d, value);
		} else if (value instanceof Iterable) {
			jsonObject = convertIterable(jsonObject, d, value);
		} else {
			jsonObject = add(jsonObject, d.getName(), value);
		}
		return jsonObject;
    }

	private static JsonObjectBuilder convertDocument(JsonObjectBuilder jsonObject, Document d, Object value) {
		Document document = (Document) value;
		jsonObject = jsonObject.add(d.getName(), add(Json.createObjectBuilder(), document.getName(), document.get()));
		return jsonObject;
	}

	private static JsonObjectBuilder convertIterable(JsonObjectBuilder jsonObject, Document document, Object value) {
		JsonObjectBuilder map = Json.createObjectBuilder();
		JsonArrayBuilder array = Json.createArrayBuilder();
		int count = 0;
		for(Object element : (Iterable<?>)value) {
			if (element instanceof Document) {
				Document subDocument = (Document) element;
				map = add(map, subDocument.getName(), subDocument.get());
			} else if (isSubDocument(element)) {
				JsonObjectBuilder subJson = Json.createObjectBuilder();
				for(Object e : (Iterable<?>)element) {
					subJson = getSubDocument((Document)e, subJson);
				}
				array = array.add(subJson);
				count++;
			} else {
				if(value instanceof Number) {
					array = array.add(((Number)value).doubleValue());
				} else if(value == null) {
					array = array.addNull();
				} else {
					array = array.add(value.toString());
				}
				count++;
			}
		}
		if(count == 0) {
			jsonObject = jsonObject.add(document.getName(), map);
		} else {
			jsonObject = jsonObject.add(document.getName(), array);
		}
		return jsonObject;
	}
	
	private static JsonObjectBuilder getSubDocument(Document d, JsonObjectBuilder subJson) {
		return toJsonObject(d, subJson);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static boolean isSubDocument(Object value) {
		return value instanceof Iterable && stream(((Iterable) value).spliterator(), false)
				.allMatch(Document.class::isInstance);
	}
	
	private static JsonObjectBuilder add(JsonObjectBuilder jsonObject, String key, Object value) {
		if(value instanceof Number) {
			return jsonObject.add(key, ((Number)value).doubleValue());
		} else if(value == null) {
			return jsonObject.addNull(key);
		} else {
			return jsonObject.add(key, value.toString());
		}
	}
}
