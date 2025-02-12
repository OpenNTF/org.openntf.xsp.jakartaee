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
package org.openntf.xsp.jakarta.pages.el;

import jakarta.el.BeanNameELResolver;
import jakarta.el.BeanNameResolver;
import jakarta.el.CompositeELResolver;
import jakarta.el.OptionalELResolver;
import jakarta.el.RecordELResolver;
import jakarta.enterprise.inject.literal.NamedLiteral;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.servlet.jsp.el.ImplicitObjectELResolver;

public class NSFELResolver extends CompositeELResolver {
	public static final NSFELResolver instance = new NSFELResolver();

	public NSFELResolver() {
		add(new ImplicitObjectELResolver());
		add(new BeanNameELResolver(new CDIBeanResolver()));
		add(new RecordELResolver());
		add(new OptionalELResolver());
	}

	public static class CDIBeanResolver extends BeanNameResolver {
		@Override
		public boolean isNameResolved(final String beanName) {
			return CDI.current().select(NamedLiteral.of(beanName)).isResolvable();
		}

		@Override
		public Object getBean(final String beanName) {
			return CDI.current().select(NamedLiteral.of(beanName)).get();
		}
	}
}
