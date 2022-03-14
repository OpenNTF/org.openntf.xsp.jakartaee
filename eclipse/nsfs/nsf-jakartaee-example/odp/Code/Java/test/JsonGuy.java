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
package test;

import java.io.Serializable;

import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;

import org.openntf.xsp.jsonapi.JSONBindUtil;

public class JsonGuy implements Serializable {
	private static final long serialVersionUID = 1L;

	public static class TestBean implements Serializable {
		private static final long serialVersionUID = 1L;
		
		private String firstName;
		private String lastName;
		
		public TestBean() {
			
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
		@Override
		public String toString() {
			return String.format("TestBean [firstName=%s, lastName=%s]", firstName, lastName);
		}
	}
	
	public String getJson() {
		TestBean foo = new TestBean();
		foo.setFirstName("foo");
		foo.setLastName("fooson");
		Jsonb jsonb = JsonbBuilder.create();
		return JSONBindUtil.toJson(foo, jsonb);
	}
	
	public Object getObject() {
		Jsonb jsonb = JsonbBuilder.create();
		String json = getJson();
		return JSONBindUtil.fromJson(json, jsonb, TestBean.class);
	}
}
