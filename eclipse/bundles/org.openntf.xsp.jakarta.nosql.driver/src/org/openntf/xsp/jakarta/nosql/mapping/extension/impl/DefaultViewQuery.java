/**
 * Copyright (c) 2018-2026 Contributors to the XPages Jakarta EE Support Project
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
package org.openntf.xsp.jakarta.nosql.mapping.extension.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

import org.openntf.xsp.jakarta.nosql.mapping.extension.FTSearchOption;
import org.openntf.xsp.jakarta.nosql.mapping.extension.ViewQuery;

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
	/**
	 * @since 2.11.0
	 */
	private List<String> ftSearch;
	/**
	 * @since 2.11.0
	 */
	private Collection<FTSearchOption> ftSearchOptions;

	@Override
	public ViewQuery category(final String category) {
		this.category = category;
		return this;
	}

	@Override
	public String getCategory() {
		return category;
	}

	@Override
	public ViewQuery key(final Object key, final boolean exact) {
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

	@Override
	public ViewQuery ftSearch(final String query, final Collection<FTSearchOption> options) {
		return ftSearch(query == null || query.isEmpty() ? null : Collections.singletonList(query), options);
	}

	@Override
	public ViewQuery ftSearch(final Collection<String> queries, final Collection<FTSearchOption> options) {
		this.ftSearch = queries == null ? null : new ArrayList<>(queries);
		this.ftSearchOptions = options == null ? null : EnumSet.copyOf(options);
		return this;
	}

	@Override
	public List<String> getFtSearch() {
		List<String> ftSearch = this.ftSearch;
		if(ftSearch == null || ftSearch.isEmpty()) {
			return Collections.emptyList();
		} else {
			return ftSearch.stream()
				.filter(search -> search != null && !search.isEmpty())
				.collect(Collectors.toList());
		}
	}

	@Override
	public Collection<FTSearchOption> getFtSearchOptions() {
		Collection<FTSearchOption> options = this.ftSearchOptions;
		return options == null ? EnumSet.noneOf(FTSearchOption.class) : EnumSet.copyOf(this.ftSearchOptions);
	}

}
