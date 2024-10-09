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

import java.util.Collections;

import javax.faces.context.FacesContext;
import javax.faces.el.VariableResolver;

import jakarta.el.BeanNameResolver;

/**
 * Provides bean resolution in a Faces context.
 *
 * @author Jesse Gallagher
 * @since 1.0.0
 */
public class FacesBeanNameResolver extends BeanNameResolver {

	@Override
	public boolean isNameResolved(final String beanName) {
		return getBean(beanName) != null;
	}

	@Override
	public Object getBean(final String beanName) {
		FacesContext facesContext = FacesContext.getCurrentInstance();
		VariableResolver vr = facesContext.getApplication().getVariableResolver();
		Object result = vr.resolveVariable(facesContext, beanName);

		// Check for known "environmental" beans that may not exist in edge cases
		if(result == null) {
			switch(beanName) {
			case "compositeData": //$NON-NLS-1$
				return Collections.emptyMap();
			default:
				break;
			}
		}

		return result;
	}

}
