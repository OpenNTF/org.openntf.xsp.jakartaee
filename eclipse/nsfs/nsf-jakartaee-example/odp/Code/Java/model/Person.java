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
import java.time.Instant;

import jakarta.nosql.mapping.Column;
import jakarta.nosql.mapping.Entity;
import jakarta.nosql.mapping.Id;

@Entity
public class Person {
	@Id
	private String unid;
	
	@Column("FirstName")
	private String firstName;
	
	@Column("LastName")
	private String lastName;
	
	@Column("Birthday")
	private LocalDate birthday;
	
	@Column("FavoriteTime")
	private LocalTime favoriteTime;
	
	@Column("Added")
	private Instant added;
	
	@Column("CustomProperty")
	private CustomPropertyType customProperty;

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
}