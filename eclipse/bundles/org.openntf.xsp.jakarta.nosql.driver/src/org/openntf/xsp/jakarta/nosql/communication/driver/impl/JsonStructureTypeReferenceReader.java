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
package org.openntf.xsp.jakarta.nosql.communication.driver.impl;

import java.util.List;

import org.eclipse.jnosql.communication.TypeReferenceReader;
import org.eclipse.jnosql.communication.TypeSupplier;

import jakarta.json.JsonArray;
import jakarta.json.JsonStructure;

/**
 * @since 2.14.0
 */
public class JsonStructureTypeReferenceReader implements TypeReferenceReader {

	@Override
	public boolean test(final TypeSupplier<?> t) {
		if(t.get() instanceof Class c) {
			return JsonStructure.class.isAssignableFrom(c);
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T convert(final TypeSupplier<T> typeReference, Object value) {
		if(value instanceof JsonArray) {
			return (T)value;
		}
		if(value instanceof List l && l.size() > 0) {
			value = l.get(0);
		}
		return (T)value;
	}

}
