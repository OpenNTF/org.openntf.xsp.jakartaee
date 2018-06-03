/**
 * Copyright © 2018 Jesse Gallagher
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

import javax.el.BeanNameELResolver;
import javax.el.ExpressionFactory;
import javax.el.StandardELContext;

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
	}
}
