package org.openntf.xsp.nosql.mapping.extension;

/**
 * These options can be used when executing full-text searches to
 * refine the search behavior.
 * 
 * @author Jesse Gallagher
 * @since 2.11.0
 */
public enum FTSearchOption {
	/**
	 * Update the full-text index before executing the search.
	 */
	UPDATE_INDEX,
	/**
	 * Apply the EXACTCASE filter
	 */
	EXACT,
	/**
	 * Return word variants
	 */
	VARIANTS,
	/**
	 * Return misspelled words
	 */
	FUZZY
}
