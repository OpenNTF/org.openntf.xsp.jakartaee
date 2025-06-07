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
package org.openntf.xsp.jakarta.concurrency;

import java.util.HashMap;
import java.util.Map;

import org.glassfish.concurro.spi.ContextHandle;

/**
 * This {@link ContextHandle} instance provides access to arbitrary
 * contextual attributes.
 *
 * @author Jesse Gallagher
 * @since 2.7.0
 */
public class AttributedContextHandle implements ContextHandle {
	private static final long serialVersionUID = 1L;

	// TODO figure out what to do about Serialization
	private final Map<String, Object> attributes;

	public AttributedContextHandle() {
		this.attributes = new HashMap<>();
	}

	public void setAttribute(final String attrName, final Object value) {
		this.attributes.put(attrName, value);
	}

	@SuppressWarnings("unchecked")
	public <T> T getAttribute(final String attrName) {
		return (T)this.attributes.get(attrName);
	}
}