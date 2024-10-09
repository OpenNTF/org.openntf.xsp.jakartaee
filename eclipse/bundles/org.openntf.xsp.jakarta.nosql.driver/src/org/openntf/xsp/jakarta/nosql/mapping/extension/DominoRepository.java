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
package org.openntf.xsp.jakarta.nosql.mapping.extension;

import java.time.temporal.TemporalAccessor;
import java.util.Optional;
import java.util.stream.Stream;

import org.eclipse.jnosql.mapping.NoSQLRepository;
import org.openntf.xsp.jakarta.nosql.communication.driver.ViewInfo;

import jakarta.data.Sort;
import jakarta.data.page.PageRequest;

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
public interface DominoRepository<T, ID> extends NoSQLRepository<T, ID> {
	/**
	 * Scope for removal of calendar entries.
	 *
	 * @since 2.15.0
	 * @see {@link DominoRepository#removeCalendarEntry(String, CalendarModScope, String)}
	 */
	enum CalendarModScope {
		ALL, CURRENT, FUTURE, PREV
	}

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
	 * @param pagination {@link Page} rules to apply to reading the view;
	 *        may be {@code null}
     * @return a {@link Stream} of {@code <T>} entities
     * @since 2.12.0
     */
    Stream<T> readViewEntries(String viewName, int maxLevel, boolean documentsOnly, ViewQuery viewQuery, Sort<T> sorts, PageRequest pagination);

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
	 * @param pagination {@link Page} rules to apply to reading the view;
	 *        may be {@code null}
     * @return a {@link Stream} of {@code <T>} entities
     * @since 2.12.0
     */
    Stream<T> readViewDocuments(String viewName, int maxLevel, boolean distinct, ViewQuery viewQuery, Sort<T> sorts, PageRequest pagination);

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
     * @param name the document name
     * @param userName the qualifying user name of the document; may be
     *        {@code null}
     * @return the entity given the document name
     * @since 2.13.0
     */
    Optional<T> findNamedDocument(String name, String userName);

    /**
     * Finds an entity given its note name
     *
     * @param profileName the profile document name
     * @param userName the qualifying user name of the document; may be
     *        {@code null}
     * @return the entity given the profile name
     * @since 2.13.0
     */
    Optional<T> findProfileDocument(String profileName, String userName);

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