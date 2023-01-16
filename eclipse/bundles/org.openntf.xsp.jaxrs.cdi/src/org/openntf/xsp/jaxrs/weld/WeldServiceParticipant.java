/**
 * Copyright (c) 2018-2023 Contributors to the XPages Jakarta EE Support Project
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
package org.openntf.xsp.jaxrs.weld;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import jakarta.enterprise.inject.spi.CDI;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.jboss.weld.context.RequestContext;
import org.jboss.weld.context.bound.BoundLiteral;
import org.jboss.weld.context.bound.BoundRequestContext;
import org.openntf.xsp.jaxrs.ServiceParticipant;

public class WeldServiceParticipant implements ServiceParticipant {
	public static final String KEY_STORAGE = WeldServiceParticipant.class.getName() + "_storage"; //$NON-NLS-1$

	@Override
	public void doBeforeService(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		BoundRequestContext context = (BoundRequestContext)CDI.current().select(RequestContext.class, BoundLiteral.INSTANCE).get();
		Map<String, Object> cdiScope = new HashMap<>();
		request.setAttribute(KEY_STORAGE, cdiScope);
		context.associate(cdiScope);
		context.activate();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void doAfterService(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		BoundRequestContext context = (BoundRequestContext)CDI.current().select(RequestContext.class, BoundLiteral.INSTANCE).get();
		context.invalidate();
		context.deactivate();
		context.dissociate((Map<String, Object>)request.getAttribute(KEY_STORAGE));
	}

}