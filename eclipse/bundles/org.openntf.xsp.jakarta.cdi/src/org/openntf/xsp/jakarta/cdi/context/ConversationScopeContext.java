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
package org.openntf.xsp.jakarta.cdi.context;

import java.lang.annotation.Annotation;

import javax.faces.context.FacesContext;

import jakarta.enterprise.context.ConversationScoped;

/**
 * @author Jesse Gallagher
 * @since 2.0.0
 */
public class ConversationScopeContext extends AbstractProxyingContext {
	private static final long serialVersionUID = 1L;

	@Override
	public Class<? extends Annotation> getScope() {
		return ConversationScoped.class;
	}

	@SuppressWarnings("unchecked")
	@Override
	public BasicScopeContextHolder getHolder() {
		FacesContext context = FacesContext.getCurrentInstance();
		if(context != null) {
			String key = generateKey();

			return (BasicScopeContextHolder)context.getViewRoot()
				.getViewMap()
				.computeIfAbsent(key, k -> new BasicScopeContextHolder());
		}
		// Must be in a non-HTTP task - just spin up a discardable one
		return new BasicScopeContextHolder();
	}

	@Override
	public boolean isActive() {
		return FacesContext.getCurrentInstance() != null;
	}
}
