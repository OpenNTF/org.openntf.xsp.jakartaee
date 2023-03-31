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
package org.openntf.xsp.nosql.mapping.extension;

import java.util.Collection;
import java.util.function.Supplier;

import jakarta.nosql.ServiceLoaderProvider;

/**
 * Allows for the specification of Domino view queries in a repository
 * method annotated with {@link ViewEntries} or {@link ViewDocuments}.
 * 
 * @author Jesse Gallagher
 * @since 2.8.0
 */
public interface ViewQuery {
	/**
	 * Creates a new {@link ViewQuery} instance.
	 * 
	 * @return a new query instance
	 */
	static ViewQuery query() {
		return ServiceLoaderProvider.get(ViewQueryProvider.class).get();
	}
	
	
	/**
	 * Specifies a category to restrict view navigation to.
	 * 
	 * <p>This option is exclusive with {@link #viewKey(Object, boolean)}.</p>
	 * 
	 * @param category the category to restrict to, or {@code null} to un-set
	 *        an existing category
	 * @return this query object
	 */
	ViewQuery category(String category);
	
	/**
	 * Retrieves the category this query is restricted to.
	 * 
	 * @return the category to restrict to, or {@code null} if no category is
	 *         specified
	 */
	String getCategory();
	
	/**
	 * Specifies a key to search for and whether the query should be restricted
	 * to a single return value.
	 * 
	 * <p>If {@code key} is an instance of {@link java.util.Collection Collection},
	 * then the query will match against multiple key columns.</p>
	 * 
	 * <p>This option is exclusive with {@link #category(String)}.</p>
	 * 
	 * @param key the key to search for, or {@code null} to un-set an existing
	 *        key
	 * @param exact {@code true} (default) if the key lookup should be exact;
	 *        {@code false} to allow substring matches
	 * @return this query object
	 */
	ViewQuery key(Object key, boolean exact);
	
	/**
	 * Retrieves the key to restrict the view query to.
	 * 
	 * @return the key to restrict to, or {@code null} if no key is specified
	 */
	Object getKey();
	
	/**
	 * Determines whether key queries set by {@link #viewKey(Object, boolean)}
	 * should perform exact matching.
	 * 
	 * @return {@code true} if key querying should be exact; {@code false} otherwise
	 */
	boolean isExact();
	
	/**
	 * Applies a full-text search to the view.
	 * 
	 * @param query the full-text search query
	 * @param options a collection of {@link FTSearchOption} values to control the
	 *        search
	 * @return this query object
	 * @since 2.11.0
	 */
	ViewQuery ftSearch(String query, Collection<FTSearchOption> options);
	
	/**
	 * Applies one or more full-text search queries to the view.
	 * 
	 * @param queries the full-text search queries
	 * @param options a collection of {@link FTSearchOption} values to control the
	 *        search
	 * @return this query object
	 * @since 2.11.0
	 */
	ViewQuery ftSearch(Collection<String> queries, Collection<FTSearchOption> options);
	
	/**
	 * Retrieves any applied full-text search queries for the view.
	 * 
	 * @return the FT search query, or {@code null} if none has been applied
	 * @since 2.11.0
	 */
	Collection<String> getFtSearch();
	
	/**
	 * Retrieves the options to apply when executing a full-text search.
	 * 
	 * @return a {@link Collection} of {@link FTSearchOption} values, never
	 *         {@code null}
	 * @since 2.11.0
	 */
	Collection<FTSearchOption> getFtSearchOptions();
	
	/**
     * A provider class of {@link ViewQuery}
     */
    interface ViewQueryProvider extends Supplier<ViewQuery> {

    }
}
