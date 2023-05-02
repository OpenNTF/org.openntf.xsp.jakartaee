package org.openntf.xsp.nosql.communication.driver;

import java.util.Collection;

/**
 * Represents design information about a column within an instance
 * of {@link ViewInfo}.
 *
 * @since 2.12.0
 */
public interface ViewColumnInfo {
	/**
	 * Represents the sort order of a column
	 */
	enum SortOrder {
		ASCENDING, DESCENDING, NONE
	}
	
	/**
	 * Retrieves the human-visible column title.
	 * 
	 * @return the column title, never {@code null}
	 */
	String getTitle();
	
	/**
	 * Retrieves the programmatic (item) name of the column.
	 * 
	 * @return the column programmatic name, never {@code null}
	 */
	String getProgrammaticName();
	
	/**
	 * Retrieves the sort order of the column.
	 * 
	 * @return the sorting order of the column, or {@link SortOrder#NONE}
	 *         if it is not sorted
	 */
	SortOrder getSortOrder();
	
	/**
	 * Retrieves the available directions that a column may be sorted by.
	 * 
	 * @return a {@link Collection} of {@link SortOrder} values; may be
	 *         empty if the column cannot be resorted
	 */
	Collection<SortOrder> getResortOrders();
	
	/**
	 * Determines whether the column is categorized.
	 * 
	 * @return {@code true} if the column is categorized; {@code false} otherwise
	 */
	boolean isCategorized();
	
}
