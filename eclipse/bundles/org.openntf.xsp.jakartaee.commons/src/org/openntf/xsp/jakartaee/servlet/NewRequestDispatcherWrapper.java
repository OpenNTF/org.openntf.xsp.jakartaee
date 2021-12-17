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

import java.io.IOException;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;

class NewRequestDispatcherWrapper implements javax.servlet.RequestDispatcher {
	final RequestDispatcher delegate;
	
	public NewRequestDispatcherWrapper(RequestDispatcher delegate) {
		this.delegate = delegate;
	}

	@Override
	public void forward(javax.servlet.ServletRequest arg0, javax.servlet.ServletResponse arg1) throws javax.servlet.ServletException, IOException {
		javax.servlet.http.HttpServletRequest req = (javax.servlet.http.HttpServletRequest)arg0;
		javax.servlet.http.HttpServletResponse resp = (javax.servlet.http.HttpServletResponse)arg1;
		try {
			delegate.forward(ServletUtil.oldToNew(null, req), ServletUtil.oldToNew(resp));
		} catch (ServletException e) {
			throw new javax.servlet.ServletException(e);
		}
	}

	@Override
	public void include(javax.servlet.ServletRequest arg0, javax.servlet.ServletResponse arg1) throws javax.servlet.ServletException, IOException {
		javax.servlet.http.HttpServletRequest req = (javax.servlet.http.HttpServletRequest)arg0;
		javax.servlet.http.HttpServletResponse resp = (javax.servlet.http.HttpServletResponse)arg1;
		try {
			delegate.include(ServletUtil.oldToNew(null, req), ServletUtil.oldToNew(resp));
		} catch (ServletException e) {
			throw new javax.servlet.ServletException(e);
		}
	}
}
