package org.openntf.xsp.jakarta.nosql.mapping.extension;

/**
 * ACL access privileges for the underlying database.
 * 
 * @since 3.6.0
 */
public enum AccessPrivilege {
	CREATE_DOCS(1),
	DELETE_DOCS(2),
	CREATE_PRIV_AGENTS(4),
	CREATE_PRIV_FOLDERS_VIEWS(8),
	CREATE_SHARED_FOLDERS_VIEWS(16),
	CREATE_SCRIPT_AGENTS(32),
	READ_PUBLIC_DOCS(64),
	WRITE_PUBLIC_DOCS(128),
	REPLICATE_COPY_DOCS(256);
	
	private int value;
	
	private AccessPrivilege(int value) {
		this.value = value;
	}
	
	/**
	 * Retrieves the underlying value index of this privilege,
	 * which can be used to for bitwise operations.
	 * 
	 * @return a numeric access privilege value
	 */
	public int getValue() {
		return value;
	}
}