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

import java.time.temporal.TemporalAccessor;
import java.util.Optional;
import java.util.stream.Stream;

import org.eclipse.jnosql.mapping.document.JNoSQLDocumentTemplate;
import org.openntf.xsp.nosql.communication.driver.ViewInfo;
import org.openntf.xsp.nosql.mapping.extension.DominoRepository.CalendarModScope;

import jakarta.data.repository.Pageable;
import jakarta.data.repository.Sort;

/**
 * {@link DocumentTemplate} sub-interface to provide access to
 * Domino-specific extensions.
 * 
 * @author Jesse Gallagher
 * @since 2.5.0
 */
public interface DominoTemplate extends JNoSQLDocumentTemplate {
	/**
	 * Reads entries from the provided view, restricted to the named category.
	 * 
	 * @param <T> the class of object returned
	 * @param entityName the effective entity name returned by this type
	 * @param viewName the name of the view to query
	 * @param pagination the pagination settings to use, or {@code null} to skip pagination
	 * @param sorts the sorting settings to use, or {@code null} to use existing sorting
	 * @param maxLevel the maximum view entry level to process
	 * @param docsOnly whether to process only document entries
	 * @param viewQuery a {@link ViewQuery} object defining the behavior of a key-based view lookup,
	 *                 or {@code null} to not query by key
	 * @param singleResult whether a query performed by {@link ViewQuery} should return a single value
	 * @return a {@link Stream} of entities
	 * @sine 2.6.0
	 */
	<T> Stream<T> viewEntryQuery(String entityName, String viewName, Pageable pagination, Sort sorts, int maxLevel, boolean docsOnly, ViewQuery viewQuery, boolean singleResult);

	/**
	 * Reads documents from the provided view, restricted to the named category.
	 * 
	 * @param <T> the class of object returned
	 * @param entityName the effective entity name returned by this type
	 * @param viewName the name of the view to query
	 * @param pagination the pagination settings to use, or {@code null} to skip pagination
	 * @param sorts the sorting settings to use, or {@code null} to use existing sorting
	 * @param maxLevel the maximum view entry level to process
	 * @param viewQuery a {@link ViewQuery} object defining the behavior of a key-based view lookup,
	 *                 or {@code null} to not query by key
	 * @param singleResult whether a query performed by {@link ViewQuery} should return a single value
	 * @param distinct whether the returned {@link Stream} should only contain distinct documents
	 * @return a {@link Stream} of entities
	 * @sine 2.6.0
	 */
	<T> Stream<T> viewDocumentQuery(String entityName, String viewName, Pageable pagination, Sort sorts, int maxLevel, ViewQuery viewQuery, boolean singleResult, boolean distinct);

	/**
	 * Adds the entity to the named folder, creating the folder if it doesn't
	 * exist.
	 * 
	 * @param entityId the UNID of the entity to add to a folder
	 * @param folderName the folder to add the entity to
	 * @throws IllegalStateException if the document has not yet been saved
	 * @throws IllegalArgumentException if {@code folderName} is empty
	 * @since 2.6.0
	 */
	void putInFolder(String entityId, String folderName);
	
	/**
	 * Removes the entity from the named folder.
	 * 
	 * @param entityId the UNID of the entity to remove from a folder
	 * @param folderName the folder to remove the entity from
	 * @throws IllegalArgumentException if {@code folderName} is empty
	 * @since 2.6.0
	 */
	void removeFromFolder(String entityId, String folderName);
	
	/**
     * Inserts entity, optionally computing with the document's form.
     *
     * @param entity entity to insert
     * @param <T>    the instance type
     * @param computeWithForm whether to compute the document with its form
     * @return the entity saved
     * @throws NullPointerException when entity is null
     * @since 2.6.0
     */
    <T> T insert(T entity, boolean computeWithForm);

	
	/**
     * Update entity, optionally computing with the document's form.
     *
     * @param entity entity to update
     * @param <T>    the instance type
     * @param computeWithForm whether to compute the document with its form
     * @return the entity saved
     * @throws NullPointerException when entity is null
     * @since 2.6.0
     */
    <T> T update(T entity, boolean computeWithForm);
    
    /**
     * Determines whether a document exists with the provided UNID.
     * 
     * @param unid the UNID to check
     * @return {@code true} if a document exists with that UNID; {@code false} otherwise
     * @since 2.7.0
     */
    boolean existsById(String unid);
    
    /**
     * Retrieves a document by its note ID.
     * 
     * @param <T> the type of document managed by this repository
	 * @param entityName the effective entity name returned by this type
     * @param noteId the note ID
     * @return an {@link Optional} describing the entity, or an empty one if no document
     *         by that ID is found
     * @since 2.8.0
     */
    <T> Optional<T> getByNoteId(String entityName, String noteId);
    
    /**
     * Retrieves a list of the views and folders in the underlying database.
     * 
     * @return a {@link Stream} of {@link ViewInfo} objects
     * @since 2.12.0
     */
    Stream<ViewInfo> getViewInfo();
    
    /**
     * Finds an entity given its note name
     * 
     * @param <T> the type of document managed by this repository
	 * @param entityName the effective entity name returned by this type
     * @param name the note name
     * @param userName the qualifying user name of the note; may be
     *        {@code null}
     * @return the entity given the note name
     * @since 2.13.0
     */
    <T> Optional<T> getByName(String entityName, String name, String userName);

    /**
     * Finds an entity given its profile name
     * 
     * @param <T> the type of document managed by this repository
	 * @param entityName the effective entity name returned by this type
     * @param profileName the profile name
     * @param userName the qualifying user name of the profile; may be
     *        {@code null}
     * @return the entity given the profile name
     * @since 2.13.0
     */
    <T> Optional<T> getProfileDocument(String entityName, String profileName, String userName);
    
    /**
     * Reads event information in iCalendar format from the backing database.
     * 
     * <p>The backing database must contain calendar data, expected to be entries
     * in a view named {@code "($Calendar)"} in the same format as a normal mail
     * database.</p>
     * 
     * @param start the start of the query range; must be convertible to
     *        {@link java.time.LocalDate LocalDate}, {@link java.time.LocalTime},
     *        or {@link java.time.Instant}
     * @param end the end of the query range; must be convertible to
     *        {@link java.time.LocalDate LocalDate}, {@link java.time.LocalTime},
     *        or {@link java.time.Instant}
     * @param pagination skip and result size; may be {@code null}
     * @return the query result in iCalendar format
     * @see <a href="https://help.hcltechsw.com/dom_designer/11.0.1/basic/H_NOTESCALENDAR_CLASS_JAVA.html">lotus.domino.NotesCalendar documentation</a>
     * @since 2.15.0
     */
    String readCalendarRange(TemporalAccessor start, TemporalAccessor end, Pageable pagination);

    /**
     * Retrieves a calendar entry in iCalendar format.
     * 
     * @param uid the UID of the entry to retrieve
     * @return an {@link Optional} describing the event data, or an
     *         empty one if no event by that UID is found
     * @since 2.15.0
     */
    Optional<String> readCalendarEntry(String uid);
    
    /**
     * Creates a new calendar entry using the provided iCalendar-format data.
     * 
     * @param icalData the iCalendar data to import
     * @param sendInvitations {@code true} to send invitations to participants;
     *                        {@code false} otherwise
     * @return the UID of the entry
     * @since 2.15.0
     */
    String createCalendarEntry(String icalData, boolean sendInvitations);
    
    /**
     * Updates an existing calendar entry by UID.
     * 
     * @param uid the UID of the entry to update
     * @param icalData the new iCalendar data to write
     * @param comment comments regarding the meeting change; may be {@code null}
     * @param sendInvitations {@code true} to send notices to participants;
     *                        {@code false} otherwise
     * @param overwrite {@code true} to fully overwrite the original entry;
     *                  {@code false} to preserve attachments and custom fields
     * @param recurId the recurrence identifier for the entry; may be {@code null}
     * @since 2.15.0
     */
    void updateCalendarEntry(String uid, String icalData, String comment, boolean sendInvitations, boolean overwrite, String recurId);
    
    /**
     * Removes a calendar entry by UID.
     * 
     * @param uid the UID of the entry to delete
     * @param scope the scope of entries to remove for repeating events
     * @param recurId the recurrence identifier for the entry; may be {@code null}
     * @since 2.15.0
     */
    void removeCalendarEntry(String uid, CalendarModScope scope, String recurId);
}
