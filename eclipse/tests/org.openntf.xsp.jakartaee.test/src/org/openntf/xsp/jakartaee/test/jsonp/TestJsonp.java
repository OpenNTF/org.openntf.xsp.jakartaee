package org.openntf.xsp.jakartaee.test.jsonp;

import static org.junit.Assert.assertEquals;

import javax.json.Json;

import org.junit.Test;

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
