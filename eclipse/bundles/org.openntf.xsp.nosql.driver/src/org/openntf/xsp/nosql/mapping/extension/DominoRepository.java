/**
 * Copyright (c) 2018-2023 Contributors to the XPages Jakarta EE Support Project
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

import java.util.Optional;
import java.util.stream.Stream;

import jakarta.nosql.mapping.Pagination;
import jakarta.nosql.mapping.Repository;
import jakarta.nosql.mapping.Sorts;

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
	
	/**
     * Saves entity, optionally computing the document with its form when saved
     *
     * @param <S>    the entity type
     * @param entity entity to be saved
     * @param computeWithForm whether the document should be computed with its form
     *        before saving
     * @return the entity saved
     * @throws NullPointerException when {@code entity} is null
     * @since 2.6.0
     */
    <S extends T> S save(S entity, boolean computeWithForm);
    
    /**
     * Finds an entity given its note ID
     *
     * @param noteId the note ID
     * @return the entity given the note ID
     * @throws NullPointerException when noteId is null
     * @since 2.8.0
     */
    Optional<T> findByNoteId(String noteId);
    
    /**
     * Finds an entity given its note ID
     * 
     * @param noteId the note ID
     * @return the entity given the note ID
     * @since 2.9.0
     */
    Optional<T> findByNoteId(int noteId);
    
    /**
     * Enumerates all views and folders in the database referenced
     * by this repository.
     * 
     * @return a {@link Stream} of view/folder information
     * @since 2.12.0
     */
    Stream<Object> listViews();
    
    /**
     * Retrieves view entries from a named view or folder. This is similar
     * to using the {@link ViewEntries} annotation but allows for the use
     * of arbitrary view names.
     * 
     * @param viewName the name of the view to read
     * @param maxLevel the maximum entry level to process
     * @param documentsOnly sets whether view reading should only process document-type entries
	 * @param viewQuery {@link ViewQuery} options to apply to reading the view;
	 *        may be {@code null}
	 * @param sorts {@link Sorts} values to apply to reading the view; may be
	 *        {@code null}
	 * @param pagination {@link Pagination} rules to apply to reading the view;
	 *        may be {@code null}
     * @return a {@link Stream} of {@code <T>} entities
     * @since 2.12.0
     */
    Stream<T> readViewEntries(String viewName, int maxLevel, boolean documentsOnly, ViewQuery viewQuery, Sorts sorts, Pagination pagination);
    
    /**
     * Retrieves documents from a named view or folder. This is similar
     * to using the {@link ViewDocuments} annotation but allows for the use
     * of arbitrary view names.
     * 
     * @param viewName the name of the view to read
     * @param maxLevel the maximum entry level to process, or {@code -1} to read
     *        all levels
     * @param documentsOnly sets whether view reading should only process document-type entries
     * @param distinct whether only distinct documents should
	 *        be returned, regardless of how often they appear in the view
	 * @param viewQuery {@link ViewQuery} options to apply to reading the view;
	 *        may be {@code null}
	 * @param sorts {@link Sorts} values to apply to reading the view; may be
	 *        {@code null}
	 * @param pagination {@link Pagination} rules to apply to reading the view;
	 *        may be {@code null}
     * @return a {@link Stream} of {@code <T>} entities
     * @since 2.12.0
     */
    Stream<T> readViewDocuments(String viewName, int maxLevel, boolean distinct, ViewQuery viewQuery, Sorts sorts, Pagination pagination);
}
