package org.openntf.xsp.jakartaee.test.beanvalidation;

import static org.junit.Assert.assertEquals;

import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import org.junit.Test;
import org.openntf.xsp.beanvalidation.XPagesValidationUtil;
import org.openntf.xsp.jakartaee.test.ExampleBean;

@SuppressWarnings("nls")
public class TestValidationBasics {

	@Test
	public void testExampleBean() {
		ExampleBean bean = new ExampleBean();
		
		Validator validator = XPagesValidationUtil.constructGenericValidator();
		
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
}
