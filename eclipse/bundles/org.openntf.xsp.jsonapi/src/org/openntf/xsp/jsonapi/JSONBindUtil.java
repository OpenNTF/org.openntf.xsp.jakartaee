/**
 * Copyright © 2018 Jesse Gallagher
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
