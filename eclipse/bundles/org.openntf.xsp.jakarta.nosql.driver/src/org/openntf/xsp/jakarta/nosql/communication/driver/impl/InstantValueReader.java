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

import java.time.Instant;
import java.time.temporal.TemporalAccessor;
import java.util.Date;

import org.eclipse.jnosql.communication.ValueReader;

public class InstantValueReader implements ValueReader {

	@Override
	public boolean test(final Class<?> t) {
		return Instant.class.equals(t);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T read(final Class<T> clazz, final Object value) {
		if(value == null) {
			return null;
		} else if(value instanceof TemporalAccessor t) {
			return (T)Instant.from(t);
		} else if(value instanceof String s) {
			if(s.isEmpty()) {
				return null;
			} else {
				return (T)Instant.parse(s);
			}
		} else if(value instanceof Date d) {
			return (T)d.toInstant();
		}
		throw new UnsupportedOperationException("Unable to convert value of type " + value.getClass().getName() + " to Instant");
	}

}
