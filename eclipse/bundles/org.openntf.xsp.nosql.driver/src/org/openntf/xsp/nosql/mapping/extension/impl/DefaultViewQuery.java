/**
 * Copyright (c) 2018-2023 Contributors to the XPages Jakarta EE Support Project
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

import org.openntf.xsp.nosql.mapping.extension.ViewQuery;

/**
 * Default implementation of {@link ViewQuery}.
 * 
 * @author Jesse Gallagher
 * @since 2.8.0
 */
public class DefaultViewQuery implements ViewQuery {
	private String category;
	private Object viewKey;
	private boolean exact = false;

	@Override
	public ViewQuery category(String category) {
		this.category = category;
		return this;
	}
	
	@Override
	public String getCategory() {
		return category;
	}

	@Override
	public ViewQuery key(Object key, boolean exact) {
		this.viewKey = key;
		this.exact = exact;
		return this;
	}
	
	@Override
	public Object getKey() {
		return viewKey;
	}
	
	@Override
	public boolean isExact() {
		return exact;
	}

}
