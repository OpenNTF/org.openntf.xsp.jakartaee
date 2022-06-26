package org.openntf.xsp.nosql.mapping.extension;

import org.openntf.xsp.nosql.communication.driver.DominoConstants;

/**
 * This enum can be used in combination with {@link DominoConstants#FIELD_ENTRY_TYPE}
 * to identify the type of view entry when using the {@link ViewEntries @ViewEntries}
 * annotation.
 * 
 * @author Jesse Gallagher
 * @since 2.6.0
 */
public enum EntryType {
	DOCUMENT, CATEGORY, TOTAL
}
