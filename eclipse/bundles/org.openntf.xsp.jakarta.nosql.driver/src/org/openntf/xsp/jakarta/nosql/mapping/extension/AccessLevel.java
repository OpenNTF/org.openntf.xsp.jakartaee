package org.openntf.xsp.jakarta.nosql.mapping.extension;

/**
 * ACL access levels for the underlying database.
 * 
 * @since 3.6.0
 */
public enum AccessLevel {
	NOACCESS(0),
	DEPOSITOR(1),
	READER(2),
	AUTHOR(3),
	EDITOR(4),
	DESIGNER(5),
	MANAGER(6);
	
	private int value;
	
	private AccessLevel(int value) {
		this.value = value;
	}
	
	/**
	 * Retrieves the underlying value index of this level,
	 * which can be used to determine relative permissions.
	 * 
	 * @return a numeric access level value
	 */
	public int getValue() {
		return value;
	}
}