package org.openntf.xsp.beanvalidation.test.beans;

import static org.junit.Assert.assertEquals;

import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

import org.junit.Test;
import org.openntf.xsp.beanvalidation.XPagesValidationUtil;

public class TestValidationBasics {

	@Test
	public void testExampleBean() {
		ExampleBean bean = new ExampleBean();
		
		Validator validator = XPagesValidationUtil.constructXPagesValidator();
		
		{
			Set<ConstraintViolation<ExampleBean>> violations = XPagesValidationUtil.validateBean(bean, validator);
			assertEquals("Bean should have two violations", 2, violations.size());
		}
		
		bean.setFoo("");
		{
			Set<ConstraintViolation<ExampleBean>> violations = XPagesValidationUtil.validateBean(bean, validator);
			assertEquals("Bean should have two violations", 2, violations.size());
		}
		
		bean.setFoo("hey");
		{
			Set<ConstraintViolation<ExampleBean>> violations = XPagesValidationUtil.validateBean(bean, validator);
			assertEquals("Bean should have one violation", 1, violations.size());
		}
		bean.setBar("h");
		{
			Set<ConstraintViolation<ExampleBean>> violations = XPagesValidationUtil.validateBean(bean, validator);
			assertEquals("Bean should have one violation", 1, violations.size());
		}
		bean.setBar("h12345678910");
		{
			Set<ConstraintViolation<ExampleBean>> violations = XPagesValidationUtil.validateBean(bean, validator);
			assertEquals("Bean should have one violation", 1, violations.size());
		}
		bean.setBar("hey there");
		{
			Set<ConstraintViolation<ExampleBean>> violations = XPagesValidationUtil.validateBean(bean, validator);
			assertEquals("Bean should have no violations", 0, violations.size());
		}
	}
	
	public static class ExampleBean {
		@NotEmpty
		private String foo;
		@NotEmpty @Size(min=3, max=10)
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
		
		
	}
}
