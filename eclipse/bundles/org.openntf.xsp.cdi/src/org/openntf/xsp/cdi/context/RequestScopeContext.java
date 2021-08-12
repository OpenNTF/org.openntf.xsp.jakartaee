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
package org.openntf.xsp.cdi.context;

import java.lang.annotation.Annotation;

import javax.enterprise.context.RequestScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

/**
 * @author Jesse Gallagher
 * @since 1.2.0
 */
public class RequestScopeContext extends AbstractProxyingContext {
	private static final long serialVersionUID = 1L;

	@Override
	public Class<? extends Annotation> getScope() {
		return RequestScoped.class;
	}
	
	@Override
	public BasicScopeContextHolder getHolder() {
		HttpServletRequest req = getHttpServletRequest();
		if(req != null) {
			String key = generateKey();
			
			BasicScopeContextHolder holder = (BasicScopeContextHolder)req.getAttribute(key);
			if(holder == null) {
				holder = new BasicScopeContextHolder();
				req.setAttribute(key, holder);
			}
			return holder;
		} else {
			// Must be in a non-HTTP task - just spin up a discardable one
			return new BasicScopeContextHolder();
		}
	}
	
	@Override
	public boolean isActive() {
		// JAX-RS requests are handled in that module
		return FacesContext.getCurrentInstance() != null;
	}
}
