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
package org.openntf.xsp.jakarta.mvc.impl;

import java.io.IOException;

import jakarta.mvc.Models;
import jakarta.mvc.engine.ViewEngine;
import jakarta.mvc.engine.ViewEngineContext;
import jakarta.mvc.engine.ViewEngineException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.StreamingOutput;

/**
 * @author Jesse Gallagher
 * @since 2.3.0
 */
public class DelegatingExceptionViewEngine implements ViewEngine {
	@Override
	public boolean supports(final String view) {
		return getClass().getName().equals(view);
	}

	@Override
	public void processView(final ViewEngineContext context) throws ViewEngineException {
		Models models = context.getModels();
		Response sup = (Response)models.get("response"); //$NON-NLS-1$
		Object entity = sup.getEntity();
		if(entity instanceof StreamingOutput so) {
			try {
				so.write(context.getOutputStream());
			} catch (WebApplicationException | IOException e) {
				throw new ViewEngineException(e);
			}
		} else {
			throw new UnsupportedOperationException("Unexpected entity type: " + (entity == null ? "null" : entity.getClass().getName())); //$NON-NLS-2$
		}
	}

}
