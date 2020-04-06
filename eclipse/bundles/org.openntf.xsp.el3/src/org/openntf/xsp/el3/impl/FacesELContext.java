/**
 * Copyright © 2019 Jesse Gallagher
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
package org.openntf.xsp.el3.impl;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Collection;
import java.util.List;

import javax.el.BeanNameELResolver;
import javax.el.ELResolver;
import javax.el.ExpressionFactory;
import javax.el.StandardELContext;

import org.openntf.xsp.el3.ext.ELResolverProvider;

import com.ibm.commons.extension.ExtensionManager;

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
		addELResolver(new BeanNameELResolver(new FacesBeanNameResolver()));
		addELResolver(new XSPELResolver());
		
		// Add any other available resolvers
		List<ELResolverProvider> providers = AccessController.doPrivileged((PrivilegedAction<List<ELResolverProvider>>)() ->
			ExtensionManager.findServices(null, Thread.currentThread().getContextClassLoader(), ELResolverProvider.class.getName(), ELResolverProvider.class)
		);
		if(providers != null) {
			for(ELResolverProvider provider : providers) {
				Collection<ELResolver> resolvers = provider.provide();
				if(resolvers != null) {
					resolvers.forEach(this::addELResolver);
				}
			}
		}
	}
	
	@Override
	public Object convertToType(Object obj, Class<?> targetType) {
		return AccessController.doPrivileged((PrivilegedAction<Object>)() -> super.convertToType(obj, targetType));
	}
}
