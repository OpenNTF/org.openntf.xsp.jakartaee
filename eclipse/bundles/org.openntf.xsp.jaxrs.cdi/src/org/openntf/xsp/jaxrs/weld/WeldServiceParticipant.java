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
package org.openntf.xsp.jaxrs.weld;

import java.io.IOException;
import java.util.HashMap;

import jakarta.enterprise.inject.spi.CDI;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.jboss.weld.context.RequestContext;
import org.jboss.weld.context.bound.BoundLiteral;
import org.jboss.weld.context.bound.BoundRequestContext;
import org.openntf.xsp.jaxrs.ServiceParticipant;

public class WeldServiceParticipant implements ServiceParticipant {

	@Override
	public void doBeforeService(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		BoundRequestContext context = (BoundRequestContext)CDI.current().select(RequestContext.class, BoundLiteral.INSTANCE).get();
		context.associate(new HashMap<>());
		context.activate();
	}

	@Override
	public void doAfterService(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		CDI.current().select(RequestContext.class, BoundLiteral.INSTANCE).get().deactivate();
	}

}