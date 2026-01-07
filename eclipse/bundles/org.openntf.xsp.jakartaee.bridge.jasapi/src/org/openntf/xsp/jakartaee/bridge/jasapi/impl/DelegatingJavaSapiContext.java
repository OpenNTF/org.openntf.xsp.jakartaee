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
package org.openntf.xsp.jakartaee.bridge.jasapi.impl;

import com.ibm.domino.bridge.http.jasapi.IJavaSapiHttpContextAdapter;

import org.openntf.xsp.jakartaee.jasapi.JavaSapiContext;
import org.openntf.xsp.jakartaee.jasapi.JavaSapiRequest;
import org.openntf.xsp.jakartaee.jasapi.JavaSapiResponse;

/**
 *
 * @author Jesse Gallagher
 * @since 2.13.0
 */
public class DelegatingJavaSapiContext implements JavaSapiContext {
	private final IJavaSapiHttpContextAdapter delegate;

	public DelegatingJavaSapiContext(final IJavaSapiHttpContextAdapter delegate) {
		this.delegate = delegate;
	}

	@Override
	public void addContext(final String var1, final Object var2) {
		delegate.addContext(var1, var2);
	}

	@Override
	public void removeContext(final String var1) {
		delegate.removeContext(var1);
	}

	@Override
	public Object getContext(final String var1) {
		return delegate.getContext(var1);
	}

	@Override
	public long getContextID() {
		return delegate.getContextID();
	}

	@Override
	public JavaSapiRequest getRequest() {
		return new DelegatingJavaSapiRequest(delegate.getRequest());
	}

	@Override
	public JavaSapiResponse getResponse() {
		return new DelegatingJavaSapiResponse(delegate.getResponse());
	}

	@Override
	public String createContextName(final String var1) {
		return delegate.createContextName(var1);
	}

}
