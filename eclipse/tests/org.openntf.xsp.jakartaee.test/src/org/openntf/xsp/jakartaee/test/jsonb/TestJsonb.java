/**
 * Copyright © 2018-2022 Jesse Gallagher
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
package org.openntf.xsp.jakartaee.test.jsonb;

import static org.junit.Assert.assertEquals;

import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;

import org.junit.Test;
import org.openntf.xsp.jakartaee.test.ExampleBean;
import org.openntf.xsp.jsonapi.JSONBindUtil;

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
}
