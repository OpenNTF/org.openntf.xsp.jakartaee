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

import java.util.List;

import org.openntf.xsp.jakarta.nosql.mapping.extension.DominoRepository;
import org.openntf.xsp.jakarta.nosql.mapping.extension.ItemStorage;

import jakarta.json.JsonArray;
import jakarta.nosql.Column;
import jakarta.nosql.Entity;
import jakarta.nosql.Id;

@Entity("CustomPropertyListEntity")
public class CustomPropertyListEntity {
	public interface Repository extends DominoRepository<CustomPropertyListEntity, String> {
		
	}
	
	@Id
	private String id;
	
	@Column("CustomPropertyList")
	@ItemStorage(type=ItemStorage.Type.JSON)
	List<CustomPropertyType> customPropertyList;
	
	@Column("JsonArrayStorage")
	@ItemStorage(type=ItemStorage.Type.JSON)
	JsonArray jsonArrayStorage;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	
	public List<CustomPropertyType> getCustomPropertyList() {
		return customPropertyList;
	}
	public void setCustomPropertyList(List<CustomPropertyType> customPropertyList) {
		this.customPropertyList = customPropertyList;
	}
	
	public JsonArray getJsonArrayStorage() {
		return jsonArrayStorage;
	}
	public void setJsonArrayStorage(JsonArray jsonArrayStorage) {
		this.jsonArrayStorage = jsonArrayStorage;
	}
}
