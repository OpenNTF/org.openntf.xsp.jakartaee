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
package org.openntf.xsp.cdi.context;

import java.lang.annotation.Annotation;
import java.util.Optional;

import jakarta.enterprise.context.SessionScoped;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/**
 * @author Jesse Gallagher
 * @since 1.2.0
 */
public class SessionScopeContext extends AbstractProxyingContext {
	private static final long serialVersionUID = 1L;

	@Override
	public Class<? extends Annotation> getScope() {
		return SessionScoped.class;
	}
	
	@Override
	protected synchronized BasicScopeContextHolder getHolder() {
		Optional<HttpServletRequest> req = getHttpServletRequest();
		if(req.isPresent()) {
			HttpSession session = req.get().getSession(true);
			String key = generateKey();
			
			BasicScopeContextHolder holder = (BasicScopeContextHolder)session.getAttribute(key);
			if(holder == null) {
				holder = new BasicScopeContextHolder();
				session.setAttribute(key, holder);
			}
			return holder;
		} else {
			// Must be in a non-HTTP task - just spin up a discardable one
			return new BasicScopeContextHolder();
		}
	}
}
