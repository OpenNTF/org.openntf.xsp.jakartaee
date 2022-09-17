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
package model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.eclipse.jnosql.communication.driver.attachment.EntityAttachment;
import org.openntf.xsp.nosql.communication.driver.DominoConstants;
import org.openntf.xsp.nosql.mapping.extension.EntryType;

import java.time.Instant;

import jakarta.nosql.mapping.Column;
import jakarta.nosql.mapping.Entity;
import jakarta.nosql.mapping.Id;
import jakarta.validation.constraints.NotEmpty;

@Entity("Person")
public class Person {
	@Id
	private String unid;
	
	@Column("FirstName") @NotEmpty
	private String firstName;
	
	@Column("LastName") @NotEmpty
	private String lastName;
	
	@Column("birthday")
	private LocalDate birthday;
	
	@Column("FavoriteTime")
	private LocalTime favoriteTime;
	
	@Column("Added")
	private Instant added;
	
	@Column("CustomProperty")
	private CustomPropertyType customProperty;
	
	@Column(DominoConstants.FIELD_ATTACHMENTS)
	private List<EntityAttachment> attachments;
	
	@Column(DominoConstants.FIELD_CDATE)
	private Instant created;
	
	@Column(DominoConstants.FIELD_MDATE)
	private Instant modified;
	
	@Column(DominoConstants.FIELD_ENTRY_TYPE)
	private EntryType entryType;
	
	@Column(DominoConstants.FIELD_READ)
	private boolean read;
	
	@Column(DominoConstants.FIELD_POSITION)
	private String position;
	
	@Column(DominoConstants.FIELD_SIZE)
	private int size;
	
	@Column(DominoConstants.FIELD_NOTEID)
	private int noteId;
	
	@Column(DominoConstants.FIELD_ADATE)
	private Instant accessed;
	
	@Column(DominoConstants.FIELD_ADDED)
	private Instant addedToFile;
	
	@Column(DominoConstants.FIELD_MODIFIED_IN_THIS_FILE)
	private Instant modifiedInFile;
	
	@Column("@siblingcount")
	private int siblingCount;
	
	@Column("@childcount")
	private int childCount;
	
	@Column("@descendantcount")
	private int descendantCount;

	public String getUnid() {
		return unid;
	}

	public void setUnid(String unid) {
		this.unid = unid;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	
	public LocalDate getBirthday() {
		return birthday;
	}
	
	public void setBirthday(LocalDate birthday) {
		this.birthday = birthday;
	}
	
	public LocalTime getFavoriteTime() {
		return favoriteTime;
	}
	public void setFavoriteTime(LocalTime favoriteTime) {
		this.favoriteTime = favoriteTime;
	}
	public Instant getAdded() {
		return added;
	}
	public void setAdded(Instant added) {
		this.added = added;
	}
	
	public CustomPropertyType getCustomProperty() {
		return customProperty;
	}
	public void setCustomProperty(CustomPropertyType customProperty) {
		this.customProperty = customProperty;
	}
	
	public List<EntityAttachment> getAttachments() {
		return attachments;
	}
	public void setAttachments(List<EntityAttachment> attachments) {
		this.attachments = attachments;
	}
	
	public Instant getCreated() {
		return created;
	}
	public Instant getModified() {
		return modified;
	}
	
	public EntryType getEntryType() {
		return entryType;
	}
	
	public String getPosition() {
		return position;
	}
	
	public boolean isRead() {
		return read;
	}
	
	public int getSize() {
		return size;
	}
	
	public Instant getAccessed() {
		return accessed;
	}
	
	public int getNoteId() {
		return noteId;
	}
	
	public Instant getAddedToFile() {
		return addedToFile;
	}
	public Instant getModifiedInFile() {
		return modifiedInFile;
	}
	
	public int getChildCount() {
		return childCount;
	}
	public int getSiblingCount() {
		return siblingCount;
	}
	public int getDescendantCount() {
		return descendantCount;
	}
}