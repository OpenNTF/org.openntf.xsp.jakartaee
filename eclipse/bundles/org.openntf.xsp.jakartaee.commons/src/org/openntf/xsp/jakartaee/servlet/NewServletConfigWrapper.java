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
package org.openntf.xsp.jakartaee.servlet;

import java.util.Enumeration;

import jakarta.servlet.ServletConfig;

@SuppressWarnings("rawtypes")
class NewServletConfigWrapper implements javax.servlet.ServletConfig {
	final ServletConfig delegate;
	
	public NewServletConfigWrapper(ServletConfig delegate) {
		this.delegate = delegate;
	}
	
	public ServletConfig getDelegate() {
		return delegate;
	}
	
	@Override
	public String getInitParameter(String arg0) {
		return delegate.getInitParameter(arg0);
	}

	@Override
	public Enumeration getInitParameterNames() {
		return delegate.getInitParameterNames();
	}

	@Override
	public javax.servlet.ServletContext getServletContext() {
		return ServletUtil.newToOld(delegate.getServletContext());
	}

	@Override
	public String getServletName() {
		return delegate.getServletName();
	}

}
