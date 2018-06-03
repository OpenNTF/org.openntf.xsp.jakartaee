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
package org.openntf.xsp.cdi.impl;

import javax.faces.context.FacesContext;
import javax.faces.el.EvaluationException;
import javax.faces.el.VariableResolver;

import org.jboss.weld.environment.se.WeldContainer;
import org.jboss.weld.inject.WeldInstance;
import org.jboss.weld.literal.NamedLiteral;

import com.ibm.xsp.application.ApplicationEx;

public class CDIResolver extends VariableResolver {

	private final VariableResolver delegate;
	
	public CDIResolver(VariableResolver delegate) {
		this.delegate = delegate;
	}

	@Override
	public Object resolveVariable(FacesContext facesContext, String name) throws EvaluationException {
		if(name == null) {
			return null;
		}
		
		// Check the delegate first, since this adds the beans to the appropriate scope as needed
		if(delegate != null) {
			Object existing = delegate.resolveVariable(facesContext, name);
			if(existing != null) {
				return existing;
			}
		}
		
		// Finally, ask CDI for a named bean
		WeldContainer container = WeldApplicationListener.getContainer(ApplicationEx.getInstance(facesContext));
		WeldInstance<Object> instance = container.select(new NamedLiteral(name));
		if(instance.isResolvable()) {
			return instance.get();
		}
		
		return null;
	}

}
