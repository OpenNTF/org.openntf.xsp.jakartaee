/**
 * Copyright (c) 2018-2026 Contributors to the XPages Jakarta EE Support Project
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

import java.util.Collection;
import java.util.List;

import com.ibm.designer.runtime.domino.adapter.ComponentModule;

import org.openntf.xsp.jakarta.el.ext.ELResolverProvider;
import org.openntf.xsp.jakartaee.util.LibraryUtil;

import jakarta.el.BeanNameELResolver;
import jakarta.el.BeanNameResolver;
import jakarta.el.CompositeELResolver;
import jakarta.el.ELResolver;
import jakarta.el.OptionalELResolver;
import jakarta.el.RecordELResolver;
import jakarta.enterprise.inject.literal.NamedLiteral;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.servlet.jsp.el.ImplicitObjectELResolver;

public class NSFELResolver extends CompositeELResolver {
	public NSFELResolver(final ComponentModule module) {

		// Add any other available resolvers
		List<ELResolverProvider> providers = LibraryUtil.findExtensions(ELResolverProvider.class, module);

		for(ELResolverProvider provider : providers) {
			Collection<ELResolver> resolvers = provider.provide();
			if(resolvers != null) {
				resolvers.forEach(this::add);
			}
		}

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
