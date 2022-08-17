package org.openntf.xsp.nosql.mapping.extension;

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
     * A provider class of {@link ViewQuery}
     */
    interface ViewQueryProvider extends Supplier<ViewQuery> {

    }
}
