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
package org.openntf.xsp.jakartaee.servlet;

import java.util.Enumeration;
import java.util.Map;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;

class OldServletConfigWrapper implements ServletConfig {
	final javax.servlet.ServletConfig delegate;

	public OldServletConfigWrapper(final javax.servlet.ServletConfig delegate) {
		this.delegate = delegate;
	}

	@Override
	public String getServletName() {
		return delegate.getServletName();
	}

	@Override
	public ServletContext getServletContext() {
		return ServletUtil.oldToNew(null, delegate.getServletContext());
	}

	@Override
	public String getInitParameter(final String name) {
		ServletContext context = getServletContext();
		if(context instanceof OldServletContextWrapper) {
			Map<String, String> params = ((OldServletContextWrapper)context).getExtraInitParameters();
			if(params.containsKey(name)) {
				return params.get(name);
			}
		}
		return delegate.getInitParameter(name);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Enumeration<String> getInitParameterNames() {
		return delegate.getInitParameterNames();
	}

}
