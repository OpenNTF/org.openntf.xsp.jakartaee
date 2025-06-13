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

import org.eclipse.jnosql.communication.ValueReader;

import jakarta.annotation.Priority;

@Priority(1)
public class IntValueReader implements ValueReader {

	@Override
	public boolean test(final Class<?> t) {
		return Integer.class.equals(t) || int.class.equals(t);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T read(final Class<T> type, final Object value) {
		if(value instanceof Number v) {
			return (T)Integer.valueOf(v.intValue());
		} else if(int.class.equals(type)) {
			return (T)Integer.valueOf(0);
		} else {
			return null;
		}
	}

}
