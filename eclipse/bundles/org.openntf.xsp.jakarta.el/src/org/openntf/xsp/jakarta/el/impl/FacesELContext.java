/**
 * Copyright (c) 2018-2024 Contributors to the XPages Jakarta EE Support Project
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
package org.openntf.xsp.jakarta.el.impl;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Collection;
import java.util.List;

import jakarta.el.BeanNameELResolver;
import jakarta.el.ELResolver;
import jakarta.el.ExpressionFactory;
import jakarta.el.StandardELContext;

import org.openntf.xsp.jakarta.el.ext.ELResolverProvider;
import org.openntf.xsp.jakartaee.util.LibraryUtil;

/**
 * A subclass of {@link StandardELContext} that adds a resolver for an
 * active Faces environment.
 * 
 * @author Jesse Gallagher
 * @since 1.0.0
 */
public class FacesELContext extends StandardELContext {
	public FacesELContext(ExpressionFactory factory) {
		super(factory);
		
		// Add any other available resolvers
		List<ELResolverProvider> providers = LibraryUtil.findExtensions(ELResolverProvider.class);
		
		if(providers != null) {
			for(ELResolverProvider provider : providers) {
				Collection<ELResolver> resolvers = provider.provide();
				if(resolvers != null) {
					resolvers.forEach(this::addELResolver);
				}
			}
		}
		
		addELResolver(new BeanNameELResolver(new FacesBeanNameResolver()));
		addELResolver(new XSPELResolver());
		addELResolver(new RecordPropertyELResolver());
	}
	
	@Override
	public <T> T convertToType(Object obj, Class<T> targetType) {
		return AccessController.doPrivileged((PrivilegedAction<T>)() -> super.convertToType(obj, targetType));
	}
}
