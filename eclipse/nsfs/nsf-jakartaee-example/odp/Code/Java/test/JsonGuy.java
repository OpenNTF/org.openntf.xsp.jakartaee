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
