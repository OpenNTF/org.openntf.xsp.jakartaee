/**
 * Copyright (c) 2018-2025 Contributors to the XPages Jakarta EE Support Project
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

import org.openntf.xsp.jakarta.nosql.communication.driver.DominoConstants;
import org.openntf.xsp.jakarta.nosql.mapping.extension.BooleanStorage;
import org.openntf.xsp.jakarta.nosql.mapping.extension.DXLExport;
import org.openntf.xsp.jakarta.nosql.mapping.extension.EntryType;
import org.openntf.xsp.jakarta.nosql.mapping.extension.ItemFlags;
import org.openntf.xsp.jakarta.nosql.mapping.extension.ItemStorage;

import jakarta.json.JsonObject;
import jakarta.json.bind.annotation.JsonbTransient;
import jakarta.nosql.AttributeConverter;
import jakarta.nosql.Column;
import jakarta.nosql.Convert;
import jakarta.nosql.Entity;
import jakarta.nosql.Id;

@Entity
@SuppressWarnings("nls")
public class ExampleDoc {
	public static class SpecialBooleanConverter implements AttributeConverter<Boolean, Object> {
		@Override
		public String convertToDatabaseColumn(Boolean attribute) {
			return attribute == null ? "no way" : attribute ? "totally" : "no way";
		}

		@Override
		public Boolean convertToEntityAttribute(Object dbData) {
			return "totally".equals(dbData) || Boolean.TRUE.equals(dbData);
		}
	}
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
	@ItemFlags(summary=true)
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
	@Column("JSONPGuy")
	@ItemStorage(type=ItemStorage.Type.JSON)
	@ItemFlags(summary=true)
	private JsonObject jsonpGuy;
	@Column("BooleanStorage")
	private boolean booleanStorage;
	@Column("StringBooleanStorage")
	@BooleanStorage(type=BooleanStorage.Type.STRING, stringTrue="true", stringFalse="false")
	private boolean stringBooleanStorage;
	@Column("DoubleBooleanStorage")
	@BooleanStorage(type=BooleanStorage.Type.DOUBLE, doubleTrue=0, doubleFalse=1)
	private boolean doubleBooleanStorage;
	@Column("JsonTransientField")
	private List<String> jsonTransientField;
	@Column("JsonTransientField2")
	private List<String> jsonTransientField2;
	@Column("$CustomSort")
	@ItemStorage(updatable = false)
	private String customSort;
	@Column("StringBooleanStorage2")
	@BooleanStorage(type=BooleanStorage.Type.STRING, stringTrue="yep", stringFalse="nah")
	private boolean stringBooleanStorage2;
	@Column("ConvertBooleanStorage")
	@Convert(SpecialBooleanConverter.class)
	private boolean convertBooleanStorage;
	
	@Column(DominoConstants.FIELD_DXL)
	@DXLExport(forceNoteFormat=true, encapsulateRichText=false, outputDOCTYPE=false)
	private String dxl;
	
	@Column(DominoConstants.FIELD_ETAG)
	private String etag;

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
	
	public JsonObject getJsonpGuy() {
		return jsonpGuy;
	}
	public void setJsonpGuy(JsonObject jsonpGuy) {
		this.jsonpGuy = jsonpGuy;
	}
	
	public boolean isBooleanStorage() {
		return booleanStorage;
	}
	public void setBooleanStorage(boolean booleanStorage) {
		this.booleanStorage = booleanStorage;
	}
	
	public boolean isDoubleBooleanStorage() {
		return doubleBooleanStorage;
	}
	public void setDoubleBooleanStorage(boolean doubleBooleanStorage) {
		this.doubleBooleanStorage = doubleBooleanStorage;
	}
	
	public boolean isStringBooleanStorage() {
		return stringBooleanStorage;
	}
	public void setStringBooleanStorage(boolean stringBooleanStorage) {
		this.stringBooleanStorage = stringBooleanStorage;
	}
	
	public boolean isStringBooleanStorage2() {
		return stringBooleanStorage2;
	}
	public void setStringBooleanStorage2(boolean stringBooleanStorage2) {
		this.stringBooleanStorage2 = stringBooleanStorage2;
	}
	
	public boolean isConvertBooleanStorage() {
		return convertBooleanStorage;
	}
	public void setConvertBooleanStorage(boolean convertBooleanStorage) {
		this.convertBooleanStorage = convertBooleanStorage;
	}
	
	@JsonbTransient
	public List<String> getJsonTransientField() {
		return jsonTransientField;
	}
	public void setJsonTransientField(List<String> jsonTransientField) {
		this.jsonTransientField = jsonTransientField;
	}
	public List<String> getAlternateMethodStorage() {
		return jsonTransientField;
	}
	
	@JsonbTransient
	public List<String> getJsonTransientField2() {
		return jsonTransientField2;
	}
	public void setJsonTransientField2(List<String> jsonTransientField2) {
		this.jsonTransientField2 = jsonTransientField2;
	}
	public List<String> getAlternateMethodStorage2() {
		return jsonTransientField2;
	}
	
	public String getCustomSort() {
		return customSort;
	}
	public void setCustomSort(String customSort) {
		this.customSort = customSort;
	}
	
	public String getEtag() {
		return etag;
	}
}
