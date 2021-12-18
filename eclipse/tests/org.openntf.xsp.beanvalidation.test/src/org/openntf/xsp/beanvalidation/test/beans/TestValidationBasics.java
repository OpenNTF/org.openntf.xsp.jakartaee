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
package org.openntf.xsp.beanvalidation.test.beans;

import static org.junit.Assert.assertEquals;

import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import org.junit.Test;
import org.openntf.xsp.beanvalidation.XPagesValidationUtil;

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
