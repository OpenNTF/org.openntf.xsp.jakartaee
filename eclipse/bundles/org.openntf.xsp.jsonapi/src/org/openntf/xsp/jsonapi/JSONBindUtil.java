package org.openntf.xsp.jsonapi;

import java.security.AccessController;
import java.security.PrivilegedAction;

import javax.json.bind.Jsonb;

/**
 * Utility methods for working with JSON-B in an XPages context.
 * 
 * @author Jesse Gallagher
 * @since 1.0.0
 */
public enum JSONBindUtil {
	;
	
	/**
	 * Converts the provided object to JSON using the provided {@link Jsonb} instance.
	 * 
	 * <p>This method performs conversion in an {@link AccessController#doPrivileged} block
	 * to avoid permissions issues in an XPages application.</p>
	 * 
	 * @param obj the object to convert to JSON
	 * @param jsonb the {@link Jsonb} instance to use for processing
	 * @return the object's JSON form
	 */
	public static String toJson(Object obj, Jsonb jsonb) {
		return AccessController.doPrivileged((PrivilegedAction<String>)() -> {
			return jsonb.toJson(obj);
		});
	}
	
	/**
	 * Converts the provided JSON string to an object of the given type using the provided
	 * {@link Jsonb} instance.
	 * 
	 * <p>This method performs conversion in an {@link AccessController#doPrivileged} block
	 * to avoid permissions issues in an XPages application.</p>
	 * 
	 * @param json the JSON string to parse
	 * @param jsonb the {@link Jsonb} instance to use for processing
	 * @param type the class to deserialize to
	 * @return a deserialized object
	 */
	public static <T> T fromJson(String json, Jsonb jsonb, Class<T> type) {
		return AccessController.doPrivileged((PrivilegedAction<T>)() -> {
			return jsonb.fromJson(json, type);
		});
	}
}
