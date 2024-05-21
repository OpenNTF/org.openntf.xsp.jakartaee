/**
 * Copyright (c) 2018-2024 Contributors to the XPages Jakarta EE Support Project
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
