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