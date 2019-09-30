package org.openntf.xsp.jakartaee.test.jsonb;

import static org.junit.Assert.assertEquals;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;

import org.junit.Test;
import org.openntf.xsp.jakartaee.test.ExampleBean;
import org.openntf.xsp.jsonapi.JSONBindUtil;

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
