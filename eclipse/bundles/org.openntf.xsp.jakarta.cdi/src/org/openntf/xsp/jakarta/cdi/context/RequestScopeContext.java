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
package org.openntf.xsp.jakarta.cdi.context;

import java.lang.annotation.Annotation;
import java.util.Optional;

import org.openntf.xsp.jakarta.cdi.ext.CDIConstants;

import jakarta.enterprise.context.RequestScoped;

import jakarta.servlet.http.HttpServletRequest;

/**
 * @author Jesse Gallagher
 * @since 1.2.0
 */
public class RequestScopeContext extends AbstractProxyingContext {
	private static final long serialVersionUID = 1L;
	
	public static final ThreadLocal<Boolean> FORCE_ACTIVE = ThreadLocal.withInitial(() -> Boolean.FALSE);

	@Override
	public Class<? extends Annotation> getScope() {
		return RequestScoped.class;
	}
	
	@Override
	public BasicScopeContextHolder getHolder() {
		Optional<HttpServletRequest> req = getHttpServletRequest();
		if(req.isPresent()) {
			String key = generateKey();
			
			BasicScopeContextHolder holder = (BasicScopeContextHolder)req.get().getAttribute(key);
			if(holder == null) {
				holder = new BasicScopeContextHolder();
				req.get().setAttribute(key, holder);
			}
			return holder;
		} else {
			// Must be in a non-HTTP task - just spin up a discardable one
			return new BasicScopeContextHolder();
		}
	}
	
	@Override
	public boolean isActive() {
		if(FORCE_ACTIVE.get() == Boolean.TRUE) {
			return true;
		}
		Object jaxrsFlag = getHttpServletRequest()
			.map(req -> req.getAttribute(CDIConstants.CDI_JAXRS_REQUEST))
			.orElse(null);
		return !"true".equals(jaxrsFlag); //$NON-NLS-1$
	}
}
