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
package org.openntf.xsp.nosql.mapping.extension.impl;

import java.util.Collection;

/**
 * This wrapper class combines derived parameters from a method invocation
 * relating to view key queries.
 * 
 * @author Jesse Gallagher
 * @since 2.8.0
 */
public class ViewKeyQuery {
	private final Collection<Object> keys;
	private final boolean exact;
	private final boolean singleResult;
	
	public ViewKeyQuery(Collection<Object> keys, boolean exact, boolean singleResult) {
		this.keys = keys;
		this.exact = exact;
		this.singleResult = singleResult;
	}

	public Collection<Object> getKeys() {
		return keys;
	}
	
	public boolean hasKeys() {
		return keys != null && !keys.isEmpty();
	}
	
	public boolean isExact() {
		return exact;
	}
	
	public boolean isSingleResult() {
		return singleResult;
	}
}
