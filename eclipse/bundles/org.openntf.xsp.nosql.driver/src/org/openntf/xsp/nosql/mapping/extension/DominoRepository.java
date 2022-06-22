/**
 * Copyright © 2018-2022 Contributors to the XPages Jakarta EE Support Project
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

import jakarta.nosql.mapping.Repository;

/**
 * This sub-interface of {@link Repository} allows for the specification
 * of Domino-specific behavior.
 * 
 * @author Jesse Gallagher
 *
 * @param <T> the model-object type
 * @param <ID> the ID-field type, generally {@link String}
 * @since 2.5.0
 * 
 * @see {@link ViewEntries}
 */
public interface DominoRepository<T, ID> extends Repository<T, ID> {
	/**
	 * Adds the entity to the named folder, creating the folder if it doesn't
	 * exist.
	 * 
	 * @param entity the entity to add to a folder
	 * @param folderName the folder to add the entity to
	 * @throws IllegalStateException if the document has not yet been saved
	 * @throws IllegalArgumentException if {@code folderName} is empty
	 * @since 2.6.0
	 */
	void putInFolder(T entity, String folderName);
	
	/**
	 * Removes the entity from the named folder.
	 * 
	 * @param entity the entity to remove from a folder
	 * @param folderName the folder to remove the entity from
	 * @throws IllegalArgumentException if {@code folderName} is empty
	 * @since 2.6.0
	 */
	void removeFromFolder(T entity, String folderName);
}
