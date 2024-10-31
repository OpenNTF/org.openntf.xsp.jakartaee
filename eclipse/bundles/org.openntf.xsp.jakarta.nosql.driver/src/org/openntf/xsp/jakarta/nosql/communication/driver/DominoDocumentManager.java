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
package org.openntf.xsp.jakarta.nosql.communication.driver;

import java.time.temporal.TemporalAccessor;
import java.util.Optional;
import java.util.stream.Stream;

import org.eclipse.jnosql.communication.semistructured.CommunicationEntity;
import org.eclipse.jnosql.communication.semistructured.DatabaseManager;
import org.openntf.xsp.jakarta.nosql.mapping.extension.DominoRepository.CalendarModScope;
import org.openntf.xsp.jakarta.nosql.mapping.extension.ViewQuery;

import jakarta.data.Sort;
import jakarta.data.page.PageRequest;

public interface DominoDocumentManager extends DatabaseManager {
	Stream<CommunicationEntity> viewEntryQuery(String entityName, String viewName, PageRequest pagination, Sort<?> sorts, int maxLevel, boolean docsOnly, ViewQuery viewQuery, boolean singleResult);

	Stream<CommunicationEntity> viewDocumentQuery(String entityName, String viewName, PageRequest pagination, Sort<?> sorts, int maxLevel, ViewQuery viewQuery, boolean singleResult, boolean distinct);

	void putInFolder(String entityId, String folderName);

	void removeFromFolder(String entityId, String folderName);

	/**
     * Saves document collection entity
     *
     * @param entity entity to be saved
     * @param computeWithForm whether to compute the document with its form
     * @return the entity saved
     * @throws NullPointerException when document is null
     * @since 2.6.0
     */
	CommunicationEntity insert(CommunicationEntity entity, boolean computeWithForm);

	/**
     * Saves document collection entity
     *
     * @param entity entity to be saved
     * @param computeWithForm whether to compute the document with its form
     * @return the entity saved
     * @throws NullPointerException when document is null
     * @since 2.6.0
     */
	CommunicationEntity update(CommunicationEntity entity, boolean computeWithForm);

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
     * @param entityName the entity type to find
     * @param noteId the note ID
     * @return an {@link Optional} describing the entity, or an empty one if no document
     *         by that ID is found
     * @since 2.8.0
     */
    Optional<CommunicationEntity> getByNoteId(String entityName, String noteId);

    /**
     * Retrieves a document by its UNID.
     *
     * @param entityName the entity type to find
     * @param id the document UNID
     * @return an {@link Optional} describing the entity, or an empty one if no document
     *         by that ID is found
     * @since 2.8.0
     */
    Optional<CommunicationEntity> getById(String entityName, String id);

    /**
     * Retrieves a list of the views and folders in the underlying database.
     *
     * @return a {@link Stream} of {@link ViewInfo} objects
     * @since 2.12.0
     */
    Stream<ViewInfo> getViewInfo();

    /**
     * Finds a document given its note name
     *
	 * @param entityName the effective entity name returned by this type
     * @param name the note name
     * @param userName the qualifying user name of the note; may be
     *        {@code null}
     * @return an {@link Optional} describing the document, or an empty one if
     *         there is no document by that name
     * @since 2.13.0
     */
    Optional<CommunicationEntity> getByName(String entityName, String name, String userName);

    /**
     * Finds a document given its note name
     *
	 * @param entityName the effective entity name returned by this type
     * @param profileName the profile name
     * @param userName the qualifying user name of the document; may be
     *        {@code null}
     * @return an {@link Optional} describing the document, or an empty one if
     *         there is no document by that name
     * @since 2.13.0
     */
    Optional<CommunicationEntity> getProfileDocument(String entityName, String profileName, String userName);

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
    String readCalendarRange(TemporalAccessor start, TemporalAccessor end, PageRequest pagination);

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
