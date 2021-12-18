/**
 * Copyright © 2018-2021 Jesse Gallagher
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
package org.openntf.xsp.jsonapi.test.jsonp;

import static org.junit.Assert.assertEquals;

import jakarta.json.Json;

import org.junit.Test;

@SuppressWarnings("nls")
public class TestJsonp {
	@Test
	public void testJsonpBasics() {
		String json = Json.createObjectBuilder()
			.add("bar", "world")
			.add("foo", "hello")
			.build().toString();
		assertEquals("json should match expected", "{\"bar\":\"world\",\"foo\":\"hello\"}", json);
	}
}
