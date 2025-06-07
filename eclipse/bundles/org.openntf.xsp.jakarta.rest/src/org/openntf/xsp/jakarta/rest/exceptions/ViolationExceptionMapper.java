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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jboss.resteasy.api.validation.ResteasyViolationException;
import org.jboss.resteasy.plugins.validation.ResteasyViolationExceptionImpl;
import org.jboss.resteasy.plugins.validation.ResteasyViolationExceptionMapper;

import jakarta.annotation.Priority;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ValidationException;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.ext.Provider;

/**
 * This subclass of {@link ResteasyViolationExceptionMapper} uses the
 * {@link Produces @Produces} annotation of the target method, when present,
 * in preference to the client's {@code Accept} header.
 *
 * @author Jesse Gallagher
 * @since 2.9.0
 */
@Priority(Priorities.USER+1)
@Provider
public class ViolationExceptionMapper extends ResteasyViolationExceptionMapper {

	@Context
	private ResourceInfo resourceInfo;

	@Override
	public Response toResponse(final ValidationException exception) {
		if(exception instanceof ConstraintViolationException cve) {
			if(!(cve instanceof ResteasyViolationException)) {
				 return super.toResponse(new ResteasyViolationExceptionImpl(cve.getConstraintViolations()));
			}
		}
		return super.toResponse(exception);
	}

	@Override
	protected Response buildViolationReportResponse(final ResteasyViolationException exception, final Status status) {
		if(resourceInfo != null) {
			Produces produces = resourceInfo.getResourceMethod().getAnnotation(Produces.class);
			if(produces != null) {
				List<MediaType> accept = new ArrayList<>();
				Arrays.stream(produces.value())
					.map(MediaType::valueOf)
					.forEach(accept::add);
				accept.addAll(exception.getAccept());
				exception.setAccept(accept);
			}
		}
		return super.buildViolationReportResponse(exception, status);
	}
}
