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
package org.openntf.xsp.jakarta.rest.exceptions;

import com.ibm.xsp.acl.NoAccessSignal;

import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;

/**
 * @param <T> the type of exception handled by this class
 * @since 2.14.0
 */
public abstract class AbstractForbiddenExceptionHandler<T extends Exception> implements ExceptionMapper<T> {
	@Context
	private UriInfo uriInfo;

	@Override
	public Response toResponse(final T exception) {
		throw new NoAccessSignal(exception, uriInfo.getRequestUri().toString());
	}
}
