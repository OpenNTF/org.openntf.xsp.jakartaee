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
package org.openntf.xsp.jakarta.cdi.impl;

import java.security.AccessController;
import java.security.PrivilegedAction;

import javax.faces.context.FacesContext;
import javax.faces.el.EvaluationException;
import javax.faces.el.VariableResolver;

import com.ibm.commons.util.StringUtil;
import com.ibm.xsp.application.ApplicationEx;

import org.openntf.xsp.jakartaee.util.LibraryUtil;

import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.literal.NamedLiteral;
import jakarta.enterprise.inject.spi.CDI;

public class CDIResolver extends VariableResolver {

	private final VariableResolver delegate;

	public CDIResolver(final VariableResolver delegate) {
		this.delegate = delegate;
	}

	@Override
	public Object resolveVariable(final FacesContext facesContext, final String name) throws EvaluationException {
		if(StringUtil.isEmpty(name)) {
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
		ApplicationEx app = ApplicationEx.getInstance(facesContext);
		if(LibraryUtil.usesLibrary(LibraryUtil.LIBRARY_CORE, app)) {
			CDI<Object> container = CDI.current();
			if(container != null) {
				CDI<Object> cdi = container;
				return AccessController.doPrivileged((PrivilegedAction<Object>)() -> {
					Instance<Object> instance = cdi.select(NamedLiteral.of(name));
					if(instance.isResolvable()) {
						return instance.get();
					} else {
						return null;
					}
				});
			}
		}

		return null;
	}

}
