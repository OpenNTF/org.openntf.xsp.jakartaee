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
package org.openntf.xsp.jakarta.mvc.rest;

import org.eclipse.krazo.security.CsrfExceptionMapper;
import org.openntf.xsp.jakarta.rest.exceptions.GenericThrowableMapper;

import jakarta.annotation.Priority;
import jakarta.mvc.security.CsrfValidationException;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;

/**
 * This variant of the Krazo CsrfExceptionMapper takes higher priority than it
 * in order to provide a response to the client - that version leads Resteasy
 * to send no HTML in the response, making it very unclear what the problem
 * is.
 * 
 * @since 3.5.0
 */
@Priority(Priorities.USER + 5001 - 2)
public class DominoCsrfExceptionMapper implements ExceptionMapper<CsrfValidationException> {
	private final GenericThrowableMapper delegate = new GenericThrowableMapper();
	private final CsrfExceptionMapper krazoDelegate = new CsrfExceptionMapper();
	
	@Override
	public Response toResponse(CsrfValidationException exception) {
		WebApplicationException wrapper = new WebApplicationException(exception, krazoDelegate.toResponse(exception));
		return delegate.toResponse(wrapper);
	}

}
