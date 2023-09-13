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
package model;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

import org.openntf.xsp.nosql.communication.driver.DominoConstants;
import org.openntf.xsp.nosql.mapping.extension.DXLExport;
import org.openntf.xsp.nosql.mapping.extension.EntryType;
import org.openntf.xsp.nosql.mapping.extension.ItemFlags;
import org.openntf.xsp.nosql.mapping.extension.ItemStorage;

import jakarta.nosql.mapping.Column;
import jakarta.nosql.mapping.Entity;
import jakarta.nosql.mapping.Id;

@Entity
public class ExampleDoc {
	public static class JsonStorage {
		private String firstName;
		private String lastName;
		
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
	}
	public static class MimeStorage implements Serializable {
		private static final long serialVersionUID = 1L;
		
		private String title;
		private String address;
		
		public String getTitle() {
			return title;
		}
		public void setTitle(String title) {
			this.title = title;
		}
		public String getAddress() {
			return address;
		}
		public void setAddress(String address) {
			this.address = address;
		}
	}
	
	@Id
	private String unid;
	@Column("$$Title")
	private String title;
	@Column("$$Categories")
	private List<String> categories;
	@Column("Authors")
	@ItemFlags(authors=true)
	private List<String> authors;
	@Column("Body")
	@ItemStorage(type=ItemStorage.Type.MIME)
	private String body;
	@Column("JSONGuy")
	@ItemStorage(type=ItemStorage.Type.JSON)
	private JsonStorage jsonGuy;
	@Column("MIMEGuy")
	@ItemStorage(type=ItemStorage.Type.MIMEBean)
	private MimeStorage mimeGuy;
	@Column("SkippedValue")
	@ItemFlags(saveToDisk=false)
	private String computedValue;
	@Column("Noninsertable")
	@ItemStorage(insertable=false)
	private String nonInsertable;
	@Column("Nonupdatable")
	@ItemStorage(updatable=false)
	private String nonUpdatable;
	@Column(DominoConstants.FIELD_ENTRY_TYPE)
	private EntryType entryType;
	@Column
	@ItemStorage(precision=2)
	private double numberGuy;
	@Column
	@ItemStorage(precision=2)
	private List<Double> numbersGuy;
	@Column
	private LocalDate dateGuy;
	
	@Column(DominoConstants.FIELD_DXL)
	@DXLExport(forceNoteFormat=true, encapsulateRichText=false, outputDOCTYPE=false)
	private String dxl;

	public String getUnid() {
		return unid;
	}
	public void setUnid(String unid) {
		this.unid = unid;
	}

	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}

	public List<String> getCategories() {
		return categories;
	}
	public void setCategories(List<String> categories) {
		this.categories = categories;
	}

	public List<String> getAuthors() {
		return authors;
	}
	public void setAuthors(List<String> authors) {
		this.authors = authors;
	}
	
	public String getBody() {
		return body;
	}
	public void setBody(String body) {
		this.body = body;
	}

	public String getDxl() {
		return dxl;
	}
	
	public JsonStorage getJsonGuy() {
		return jsonGuy;
	}
	public void setJsonGuy(JsonStorage jsonGuy) {
		this.jsonGuy = jsonGuy;
	}
	
	public MimeStorage getMimeGuy() {
		return mimeGuy;
	}
	public void setMimeGuy(MimeStorage mimeGuy) {
		this.mimeGuy = mimeGuy;
	}
	
	public String getComputedValue() {
		return computedValue;
	}
	public void setComputedValue(String computedValue) {
		this.computedValue = computedValue;
	}
	
	public String getNonInsertable() {
		return nonInsertable;
	}
	public void setNonInsertable(String nonInsertable) {
		this.nonInsertable = nonInsertable;
	}
	public String getNonUpdatable() {
		return nonUpdatable;
	}
	public void setNonUpdatable(String nonUpdatable) {
		this.nonUpdatable = nonUpdatable;
	}
	
	public EntryType getEntryType() {
		return entryType;
	}
	
	public double getNumberGuy() {
		return numberGuy;
	}
	public void setNumberGuy(double numberGuy) {
		this.numberGuy = numberGuy;
	}
	
	public List<Double> getNumbersGuy() {
		return numbersGuy;
	}
	public void setNumbersGuy(List<Double> numbersGuy) {
		this.numbersGuy = numbersGuy;
	}
	
	public LocalDate getDateGuy() {
		return dateGuy;
	}
	public void setDateGuy(LocalDate dateGuy) {
		this.dateGuy = dateGuy;
	}
}
