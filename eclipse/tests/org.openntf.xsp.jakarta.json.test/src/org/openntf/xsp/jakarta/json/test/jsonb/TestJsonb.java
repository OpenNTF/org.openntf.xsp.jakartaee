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
package org.openntf.xsp.jakarta.json.test.jsonb;

import static org.junit.Assert.assertEquals;

import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;

import org.junit.Test;
import org.openntf.xsp.jakarta.json.JSONBindUtil;

@SuppressWarnings("nls")
public class TestJsonb {
	
	@Test
	public void testExampleBean() {
		Jsonb jsonb = JsonbBuilder.create();
		
		ExampleBean bean = new ExampleBean();
		bean.setFoo("hello");
		bean.setBar("world");
		String json = JSONBindUtil.toJson(bean, jsonb);
		assertEquals("Serialized bean should match expected", "{\"bar\":\"world\",\"foo\":\"hello\"}", json);
		
		ExampleBean bean2 = JSONBindUtil.fromJson(json, jsonb, ExampleBean.class);
		assertEquals("Beans should be equivalent", bean, bean2);
	}

	public static class ExampleBean {
		private String foo;
		private String bar;
		
		public String getFoo() {
			return foo;
		}
		public void setFoo(String foo) {
			this.foo = foo;
		}
		public String getBar() {
			return bar;
		}
		public void setBar(String bar) {
			this.bar = bar;
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((bar == null) ? 0 : bar.hashCode());
			result = prime * result + ((foo == null) ? 0 : foo.hashCode());
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ExampleBean other = (ExampleBean) obj;
			if (bar == null) {
				if (other.bar != null)
					return false;
			} else if (!bar.equals(other.bar))
				return false;
			if (foo == null) {
				if (other.foo != null)
					return false;
			} else if (!foo.equals(other.foo))
				return false;
			return true;
		}
	}
}
