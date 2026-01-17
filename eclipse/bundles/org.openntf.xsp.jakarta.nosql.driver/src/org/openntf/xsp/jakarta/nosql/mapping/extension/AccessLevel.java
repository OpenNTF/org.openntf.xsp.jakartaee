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